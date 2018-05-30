package com.alibaba.ttl.forkjoin;

import org.junit.AfterClass;
import org.junit.Test;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.LongStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ForkJoinPoolTest {
    private static final ForkJoinPool pool = new ForkJoinPool();

    @AfterClass
    public static void afterClass() throws Exception {
        pool.shutdown();
        if (!pool.awaitTermination(100, TimeUnit.MILLISECONDS)) fail("Fail to shutdown thread pool");
    }

    @Test
    public void test_sameTaskDirectReturn_onlyExec1Time_ifHaveRun() throws Exception {
        long[] numbers = LongStream.rangeClosed(1, 1000).toArray();
        final SumTask sumTask = new SumTask(numbers);

        // same task instance run 10 times
        for (int i = 0; i < 10; i++) {
            assertEquals(500500L, pool.invoke(sumTask).longValue());
        }

        assertEquals(1, sumTask.execCounter.get());
    }
}


class SumTask extends RecursiveTask<Long> {
    private final long[] numbers;
    private final int from;
    private final int to;

    AtomicInteger execCounter = new AtomicInteger(0);

    SumTask(long[] numbers) {
        this(numbers, 0, numbers.length - 1);
    }

    private SumTask(long[] numbers, int from, int to) {
        this.numbers = numbers;
        this.from = from;
        this.to = to;
    }

    @Override
    protected Long compute() {
        execCounter.incrementAndGet();

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

            SumTask taskLeft = new SumTask(numbers, from, middle);
            SumTask taskRight = new SumTask(numbers, middle + 1, to);

            taskLeft.fork();
            taskRight.fork();
            return taskLeft.join() + taskRight.join();
        }
    }
}
