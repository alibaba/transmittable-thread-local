package com.alibaba.ttl;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;

import static com.alibaba.ttl.Utils.CHILD;
import static com.alibaba.ttl.Utils.PARENT_AFTER_CREATE_TTL_TASK;
import static com.alibaba.ttl.Utils.PARENT_MODIFIED_IN_CHILD;
import static com.alibaba.ttl.Utils.PARENT_UNMODIFIED_IN_CHILD;
import static com.alibaba.ttl.Utils.assertTtlInstances;
import static com.alibaba.ttl.Utils.copied;
import static com.alibaba.ttl.Utils.createTestTtlValue;
import static com.alibaba.ttl.Utils.expandThreadPool;
import static org.junit.Assert.assertEquals;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class ForkJoinTest {
    private static final ForkJoinPool pool = new ForkJoinPool();

    static final Map<String, Map<String, Object>> tag2copied = Collections.synchronizedMap(new LinkedHashMap<>());


    static final AtomicLong tagCounter = new AtomicLong();

    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);

    static {
        expandThreadPool(pool);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        pool.shutdown();
    }

    @Test
    public void task() throws Exception {
        final ConcurrentMap<String, TransmittableThreadLocal<String>> ttlInstances = createTestTtlValue();

        long[] numbers = LongStream.rangeClosed(1, 100).toArray();
        final Long result = pool.invoke(new SumTask(numbers, ttlInstances));
        assertEquals(5050L, result.longValue());

        // create after new Task, won't see parent value in in task!
        TransmittableThreadLocal<String> after = new TransmittableThreadLocal<String>();
        after.set(PARENT_AFTER_CREATE_TTL_TASK);
        ttlInstances.put(PARENT_AFTER_CREATE_TTL_TASK, after);

        for (Map.Entry<String, Map<String, Object>> entry : tag2copied.entrySet()) {
            // child Inheritable
            final String tag = entry.getKey();
            final Map<String, Object> copied = entry.getValue();

            System.out.println(tag + " After Run:");
            for (Map.Entry<String, Object> copiedEntry : copied.entrySet()) {
                String key = copiedEntry.getKey();
                Object value = copiedEntry.getValue();
                System.out.printf("Key %s, value: %s%n", key, value);
            }
            System.out.println();
            System.out.flush();

            assertEquals(PARENT_UNMODIFIED_IN_CHILD, copied.get(PARENT_UNMODIFIED_IN_CHILD));
            assertEquals(PARENT_MODIFIED_IN_CHILD + tag, copied.get(PARENT_MODIFIED_IN_CHILD));
            assertEquals(CHILD + tag, copied.get(CHILD + tag));
            Assert.assertNull(copied.get(PARENT_AFTER_CREATE_TTL_TASK));
        }

        // child do not effect parent
        assertTtlInstances(copied(ttlInstances),
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD,
                PARENT_AFTER_CREATE_TTL_TASK, PARENT_AFTER_CREATE_TTL_TASK
        );
    }
}

class SumTask extends RecursiveTask<Long> {
    private final long[] numbers;
    private final int from;
    private final int to;

    public final String tag;
    private final ConcurrentMap<String, TransmittableThreadLocal<String>> ttlInstances;
    private final TransmittableThreadLocal.Capture capture;

    SumTask(long[] numbers, ConcurrentMap<String, TransmittableThreadLocal<String>> ttlInstances) {
        this(numbers, 0, numbers.length - 1, ttlInstances);
    }

    private SumTask(long[] numbers, int from, int to, ConcurrentMap<String, TransmittableThreadLocal<String>> ttlInstances) {
        this.numbers = numbers;
        this.from = from;
        this.to = to;

        this.ttlInstances = ttlInstances;
        tag = ForkJoinTest.tagCounter.getAndIncrement() + "";
        this.capture = TransmittableThreadLocal.capture();
    }

    @Override
    protected Long compute() {
        return TransmittableThreadLocal.restoreAndRun(capture, new TransmittableThreadLocal.Action<Long, RuntimeException>() {
            @Override
            public Long act() {
                // 1. Add new
                String newChildKey = CHILD + tag;
                TransmittableThreadLocal<String> child = new TransmittableThreadLocal<String>();
                child.set(newChildKey);

                TransmittableThreadLocal<String> old = ttlInstances.putIfAbsent(newChildKey, child);
                if (old != null) {
                    throw new IllegalStateException("already contains key " + newChildKey);
                }
                ttlInstances.put(newChildKey, child);

                // 2. modify the parent key
                String p = PARENT_MODIFIED_IN_CHILD + tag;
                ttlInstances.get(PARENT_MODIFIED_IN_CHILD).set(p);

                ForkJoinTest.tag2copied.put(tag, Utils.copied(ttlInstances));

                // ========================================================================

                final int delta = to - from;
                if (delta < 16) {
                    // compute directly
                    long total = 0;
                    for (int i = from; i <= to; i++) {
                        total += numbers[i];
                    }
                    return total;
                } else {
                    // split task
                    final int middle = from + delta / 2;

                    SumTask taskLeft = new SumTask(numbers, from, middle, ttlInstances);
                    SumTask taskRight = new SumTask(numbers, middle + 1, to, ttlInstances);

                    taskLeft.fork();
                    taskRight.fork();
                    return taskLeft.join() + taskRight.join();
                }
            }
        });
    }
}
