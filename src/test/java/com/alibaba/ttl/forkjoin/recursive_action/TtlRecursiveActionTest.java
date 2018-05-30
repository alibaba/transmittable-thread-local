package com.alibaba.ttl.forkjoin.recursive_action;

import com.alibaba.ttl.TransmittableThreadLocal;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.alibaba.ttl.Utils.*;
import static org.junit.Assert.fail;

/**
 * TtlRecursiveAction test class
 *
 * @author LNAmp
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class TtlRecursiveActionTest {

    private static ForkJoinPool pool = new ForkJoinPool();
    private static ForkJoinPool singleThreadPool = new ForkJoinPool(1);

    private static int[] numbers = IntStream.rangeClosed(1, 100).toArray();

    @AfterClass
    public static void afterClass() throws Exception {
        pool.shutdown();
        if (!pool.awaitTermination(100, TimeUnit.MILLISECONDS)) fail("Fail to shutdown thread pool");

        singleThreadPool.shutdown();
        if (!singleThreadPool.awaitTermination(100, TimeUnit.MILLISECONDS)) fail("Fail to shutdown thread pool");
    }

    @Test
    public void test_TtlRecursiveTask_asyncWithForkJoinPool() throws Exception {
        run_test_with_pool(pool);
    }

    @Test
    public void test_TtlRecursiveTask_asyncWithSingleThreadForkJoinPool_changeValue() throws Exception {
        run_test_with_pool(singleThreadPool);
    }

    private static void run_test_with_pool(ForkJoinPool forkJoinPool) throws Exception {
        ConcurrentMap<String, TransmittableThreadLocal<String>> ttlInstances = createTestTtlValue();

        PrintAction printAction = new PrintAction(numbers, 0, numbers.length,
                ttlInstances, false);

        TransmittableThreadLocal<String> after = new TransmittableThreadLocal<>();
        after.set(PARENT_AFTER_CREATE_TTL_TASK);
        ttlInstances.put(PARENT_AFTER_CREATE_TTL_TASK, after);

        Future<Void> future = forkJoinPool.submit(printAction);
        future.get();

        // child Inheritable
        assertTtlInstances(printAction.copied,
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD /* Not change*/, PARENT_MODIFIED_IN_CHILD
        );

        // left grand Task Inheritable, changed value
        assertTtlInstances(printAction.leftSubAction.copied,
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD + PrintAction.CHANGE_POSTFIX /* CHANGED */, PARENT_MODIFIED_IN_CHILD
        );

        // right grand Task Inheritable, not change value
        assertTtlInstances(printAction.rightSubAction.copied,
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
