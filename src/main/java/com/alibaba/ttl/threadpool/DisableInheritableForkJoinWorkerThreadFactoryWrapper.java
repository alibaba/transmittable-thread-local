package com.alibaba.ttl.threadpool;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinWorkerThread;

import static com.alibaba.ttl.TransmittableThreadLocal.Transmitter.clear;
import static com.alibaba.ttl.TransmittableThreadLocal.Transmitter.restore;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 2.10.1
 */
class DisableInheritableForkJoinWorkerThreadFactoryWrapper implements DisableInheritableForkJoinWorkerThreadFactory {
    private final ForkJoinWorkerThreadFactory threadFactory;

    DisableInheritableForkJoinWorkerThreadFactoryWrapper(@NonNull ForkJoinWorkerThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    @Override
    public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
        final Object backup = clear();
        try {
            return threadFactory.newThread(pool);
        } finally {
            restore(backup);
        }
    }

    @Override
    @NonNull
    public ForkJoinWorkerThreadFactory unwrap() {
        return threadFactory;
    }
}
