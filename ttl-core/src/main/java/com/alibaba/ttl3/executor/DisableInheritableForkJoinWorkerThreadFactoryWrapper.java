package com.alibaba.ttl3.executor;

import com.alibaba.crr.composite.Backup;
import com.alibaba.ttl3.spi.TtlWrapper;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinWorkerThread;

import static com.alibaba.ttl3.transmitter.Transmitter.clear;
import static com.alibaba.ttl3.transmitter.Transmitter.restore;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
final class DisableInheritableForkJoinWorkerThreadFactoryWrapper implements ForkJoinWorkerThreadFactory, TtlWrapper<ForkJoinWorkerThreadFactory> {
    private final ForkJoinWorkerThreadFactory threadFactory;

    DisableInheritableForkJoinWorkerThreadFactoryWrapper(@NonNull ForkJoinWorkerThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    @Override
    public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
        final Backup backup = clear();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DisableInheritableForkJoinWorkerThreadFactoryWrapper that = (DisableInheritableForkJoinWorkerThreadFactoryWrapper) o;

        return threadFactory.equals(that.threadFactory);
    }

    @Override
    public int hashCode() {
        return threadFactory.hashCode();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " - " + threadFactory;
    }
}
