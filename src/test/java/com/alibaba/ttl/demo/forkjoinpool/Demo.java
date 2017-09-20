package com.alibaba.ttl.demo.forkjoinpool;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.LongStream;

/**
 * ForkJoinPool use demo.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class Demo {
    public static void main(String[] args) {
        final ForkJoinPool pool = new ForkJoinPool();

        long[] numbers = LongStream.rangeClosed(1, 100000).toArray();
        final Long result = pool.invoke(new SumTask(numbers));

        System.out.println(result); // result is 5000050000
    }
}

class SumTask extends RecursiveTask<Long> {
    private final long[] numbers;
    private final int from;
    private final int to;

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
