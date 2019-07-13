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
}
