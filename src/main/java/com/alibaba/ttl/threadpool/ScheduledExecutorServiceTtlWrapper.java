package com.alibaba.ttl.threadpool;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.TtlCallable;
import com.alibaba.ttl.spi.TtlEnhanced;
import com.alibaba.ttl.TtlRunnable;

import javax.annotation.Nonnull;
import java.util.concurrent.*;

/**
 * {@link TransmittableThreadLocal} Wrapper of {@link ScheduledExecutorService},
 * transmit the {@link TransmittableThreadLocal} from the task submit time of {@link Runnable} or {@link Callable}
 * to the execution time of {@link Runnable} or {@link Callable}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 0.9.0
 */
class ScheduledExecutorServiceTtlWrapper extends ExecutorServiceTtlWrapper implements ScheduledExecutorService, TtlEnhanced {
    final ScheduledExecutorService scheduledExecutorService;

    public ScheduledExecutorServiceTtlWrapper(@Nonnull ScheduledExecutorService scheduledExecutorService) {
        super(scheduledExecutorService);
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Nonnull
    @Override
    public ScheduledFuture<?> schedule(@Nonnull Runnable command, long delay, @Nonnull TimeUnit unit) {
        return scheduledExecutorService.schedule(TtlRunnable.get(command), delay, unit);
    }

    @Nonnull
    @Override
    public <V> ScheduledFuture<V> schedule(@Nonnull Callable<V> callable, long delay, @Nonnull TimeUnit unit) {
        return scheduledExecutorService.schedule(TtlCallable.get(callable), delay, unit);
    }

    @Nonnull
    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(@Nonnull Runnable command, long initialDelay, long period, @Nonnull TimeUnit unit) {
        return scheduledExecutorService.scheduleAtFixedRate(TtlRunnable.get(command), initialDelay, period, unit);
    }

    @Nonnull
    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(@Nonnull Runnable command, long initialDelay, long delay, @Nonnull TimeUnit unit) {
        return scheduledExecutorService.scheduleWithFixedDelay(TtlRunnable.get(command), initialDelay, delay, unit);
    }

    @Override
    @Nonnull
    public ScheduledExecutorService unwrap() {
        return scheduledExecutorService;
    }
}
