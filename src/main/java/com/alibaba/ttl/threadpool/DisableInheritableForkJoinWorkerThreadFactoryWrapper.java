package com.alibaba.ttl.threadpool;

import com.alibaba.ttl.TransmittableThreadLocal;

import javax.annotation.Nonnull;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinWorkerThread;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 2.10.1
 */
class DisableInheritableForkJoinWorkerThreadFactoryWrapper implements DisableInheritableForkJoinWorkerThreadFactory {
    final ForkJoinWorkerThreadFactory threadFactory;

    public DisableInheritableForkJoinWorkerThreadFactoryWrapper(@Nonnull ForkJoinWorkerThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    @Override
    public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
        final Object backup = TransmittableThreadLocal.Transmitter.clear();
        try {
            return threadFactory.newThread(pool);
        } finally {
            TransmittableThreadLocal.Transmitter.restore(backup);
        }
    }

    @Nonnull
    @Override
    public ForkJoinWorkerThreadFactory unwrap() {
        return threadFactory;
    }
}
