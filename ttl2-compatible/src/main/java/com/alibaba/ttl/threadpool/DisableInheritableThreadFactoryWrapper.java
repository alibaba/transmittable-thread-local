package com.alibaba.ttl.threadpool;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.concurrent.ThreadFactory;

import static com.alibaba.ttl.TransmittableThreadLocal.Transmitter.clear;
import static com.alibaba.ttl.TransmittableThreadLocal.Transmitter.restore;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 2.10.0
 */
class DisableInheritableThreadFactoryWrapper implements DisableInheritableThreadFactory {
    private final ThreadFactory threadFactory;

    DisableInheritableThreadFactoryWrapper(@NonNull ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    @Override
    public Thread newThread(@NonNull Runnable r) {
        final Object backup = clear();
        try {
            return threadFactory.newThread(r);
        } finally {
            restore(backup);
        }
    }

    @NonNull
    @Override
    public ThreadFactory unwrap() {
        return threadFactory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DisableInheritableThreadFactoryWrapper that = (DisableInheritableThreadFactoryWrapper) o;

        return threadFactory.equals(that.threadFactory);
    }

    @Override
    public int hashCode() {
        return threadFactory.hashCode();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " - " + threadFactory.toString();
    }
}
