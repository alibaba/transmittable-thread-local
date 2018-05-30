package com.alibaba.ttl.perf.tps;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.fail;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class TpsCounter {
    private final int threadCount;
    private final ExecutorService executorService;

    private final AtomicLong counter = new AtomicLong();
    private volatile boolean stopped = false;


    TpsCounter(int threadCount) {
        this.threadCount = threadCount;
        executorService = Executors.newFixedThreadPool(threadCount);
    }

    void run(final Runnable runnable) {
        Runnable r = () -> {
            while (!stopped) {
                runnable.run();
                counter.incrementAndGet();
            }
        };
        for (int i = 0; i < threadCount; ++i) {
            executorService.execute(r);
        }
    }

    public void stop() throws InterruptedException {
        stopped = true;

        executorService.shutdown();
        executorService.awaitTermination(100, TimeUnit.MILLISECONDS);
        if (!executorService.isTerminated()) fail("Fail to shutdown thread pool");
    }

    public long getCount() {
        return counter.get();
    }
}
