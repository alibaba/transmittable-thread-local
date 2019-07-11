package com.alibaba.ttl.threadpool;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadFactory;

import static com.alibaba.ttl.TransmittableThreadLocal.Transmitter.clear;
import static com.alibaba.ttl.TransmittableThreadLocal.Transmitter.restore;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 2.10.0
 */
class DisableInheritableThreadFactoryWrapper implements DisableInheritableThreadFactory {
    private final ThreadFactory threadFactory;

    DisableInheritableThreadFactoryWrapper(@Nonnull ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    @Override
    public Thread newThread(@Nonnull Runnable r) {
        final Object backup = clear();
        try {
            return threadFactory.newThread(r);
        } finally {
            restore(backup);
        }
    }

    @Nonnull
    @Override
    public ThreadFactory unwrap() {
        return threadFactory;
    }
}
