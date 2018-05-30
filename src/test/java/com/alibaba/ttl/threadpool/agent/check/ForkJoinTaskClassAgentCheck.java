package com.alibaba.ttl.threadpool.agent.check;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.Utils;

import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static com.alibaba.ttl.Utils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * !! Quick and dirty: copy code from {@link com.alibaba.ttl.forkjoin.recursive_task.TtlRecursiveTaskTest} !!
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @author wuwen5 (wuwen.55 at aliyun dot com)
 * @see com.alibaba.ttl.threadpool.agent.TtlTransformer
 */
public class ForkJoinTaskClassAgentCheck {
    private static final ForkJoinPool pool = new ForkJoinPool();
    private static final ForkJoinPool singleThreadPool = new ForkJoinPool(1);

    private static final int[] numbers = IntStream.rangeClosed(1, 100).toArray();
    private static final Integer SUM = 5050;

    public static void main(String[] args) throws Exception {

        test_TtlRecursiveTask_asyncWith_ForkJoinPool();
        test_TtlRecursiveTask_asyncWith_SingleThreadForkJoinPool();


        pool.shutdown();
        if (!pool.awaitTermination(100, TimeUnit.MILLISECONDS)) fail("Fail to shutdown thread pool");

        singleThreadPool.shutdown();
        if (!singleThreadPool.awaitTermination(100, TimeUnit.MILLISECONDS)) fail("Fail to shutdown thread pool");


        System.out.println();
        System.out.println("====================================");
        System.out.println(ForkJoinTaskClassAgentCheck.class.getSimpleName() + " OK!");
        System.out.println("====================================");
    }


    private static void test_TtlRecursiveTask_asyncWith_ForkJoinPool() throws Exception {
        run_test_with_pool(pool);
    }

    private static void test_TtlRecursiveTask_asyncWith_SingleThreadForkJoinPool() throws Exception {
        run_test_with_pool(singleThreadPool);
    }

    private static void run_test_with_pool(ForkJoinPool forkJoinPool) throws Exception {
        ConcurrentMap<String, TransmittableThreadLocal<String>> ttlInstances = createTestTtlValue();

        SumTask sumTask = new SumTask(numbers, 0, numbers.length,
                ttlInstances, false);

        TransmittableThreadLocal<String> after = new TransmittableThreadLocal<>();
        after.set(PARENT_AFTER_CREATE_TTL_TASK);
        ttlInstances.put(PARENT_AFTER_CREATE_TTL_TASK, after);

        Future<Integer> future = forkJoinPool.submit(sumTask);
        assertEquals(SUM, future.get());

        // child Inheritable
        assertTtlInstances(sumTask.copied,
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD /* Not change*/, PARENT_MODIFIED_IN_CHILD
        );

        // left grand Task Inheritable, changed value
        assertTtlInstances(sumTask.leftSubTask.copied,
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD + SumTask.CHANGE_POSTFIX /* CHANGED */, PARENT_MODIFIED_IN_CHILD
        );

        // right grand Task Inheritable, not change value
        assertTtlInstances(sumTask.rightSubTask.copied,
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD /* Not change*/, PARENT_MODIFIED_IN_CHILD
        );

        // child do not effect parent
        assertTtlInstances(captured(ttlInstances),
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD,
                PARENT_AFTER_CREATE_TTL_TASK, PARENT_AFTER_CREATE_TTL_TASK
        );
    }
}

class SumTask extends RecursiveTask<Integer> {
    static final String CHANGE_POSTFIX = " + 1";

    private final int[] numbers;
    private final int start;
    private final int end;

    private final ConcurrentMap<String, TransmittableThreadLocal<String>> ttlMap;
    private final boolean changeTtlValue;

    volatile Map<String, Object> copied;
    volatile SumTask leftSubTask;
    volatile SumTask rightSubTask;

    public SumTask(int[] numbers, int start, int end,
                   ConcurrentMap<String, TransmittableThreadLocal<String>> ttlMap, boolean changeTtlValue) {
        this.numbers = numbers;
        this.start = start;
        this.end = end;

        this.ttlMap = ttlMap;
        this.changeTtlValue = changeTtlValue;
    }

    @Override
    protected Integer compute() {
        if (changeTtlValue) {
            Utils.modifyValuesExistInTtlInstances(CHANGE_POSTFIX, ttlMap);
        }

        try {
            int sum = 0;
            if (end - start < 5) {
                for (int i = start; i < end; i++) {
                    sum += numbers[i];
                }
                return sum;
            } else {
                int mid = (start + end) / 2;

                // left -> change! right -> not change.
                final SumTask left = new SumTask(numbers, start, mid, ttlMap, true);
                final SumTask right = new SumTask(numbers, mid, end, ttlMap, false);
                this.leftSubTask = left;
                this.rightSubTask = right;

                left.fork();
                right.fork();
                return left.join() + right.join();
            }
        } finally {
            this.copied = captured(this.ttlMap);
        }
    }
}
