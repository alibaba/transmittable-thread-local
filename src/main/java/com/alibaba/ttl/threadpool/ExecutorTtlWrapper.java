package com.alibaba.ttl.threadpool;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.spi.TtlEnhanced;
import com.alibaba.ttl.TtlRunnable;

import javax.annotation.Nonnull;
import java.util.concurrent.Executor;

/**
 * {@link TransmittableThreadLocal} Wrapper of {@link Executor},
 * transmit the {@link TransmittableThreadLocal} from the task submit time of {@link Runnable}
 * to the execution time of {@link Runnable}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 0.9.0
 */
class ExecutorTtlWrapper implements Executor, TtlEnhanced {
    private final Executor executor;

    ExecutorTtlWrapper(@Nonnull Executor executor) {
        this.executor = executor;
    }

    @Override
    public void execute(@Nonnull Runnable command) {
        executor.execute(TtlRunnable.get(command));
    }

    @Nonnull
    public Executor unwrap() {
        return executor;
    }
}
