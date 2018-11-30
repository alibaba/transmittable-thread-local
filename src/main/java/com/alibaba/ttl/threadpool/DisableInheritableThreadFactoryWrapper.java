package com.alibaba.ttl.threadpool;

import com.alibaba.ttl.TransmittableThreadLocal;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadFactory;

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
        final Object backup = TransmittableThreadLocal.Transmitter.clear();
        try {
            return threadFactory.newThread(r);
        } finally {
            TransmittableThreadLocal.Transmitter.restore(backup);
        }
    }

    @Nonnull
    @Override
    public ThreadFactory unwrap() {
        return threadFactory;
    }
}
