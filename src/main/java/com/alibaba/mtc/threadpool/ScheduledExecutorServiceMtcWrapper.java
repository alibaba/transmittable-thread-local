package com.alibaba.mtc.threadpool;

import com.alibaba.mtc.MtContextCallable;
import com.alibaba.mtc.MtContextRunnable;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * {@link com.alibaba.mtc.MtContextThreadLocal} Wrapper of {@link ScheduledExecutorService},
 * transmit the {@link com.alibaba.mtc.MtContextThreadLocal} from the task submit time of {@link Runnable} or {@link Callable}
 * to the execution time of {@link Runnable} or {@link Callable}.
 *
 * @author ding.lid
 * @since 0.9.0
 */
class ScheduledExecutorServiceMtcWrapper extends ExecutorServiceMtcWrapper implements ScheduledExecutorService {
    final ScheduledExecutorService scheduledExecutorService;

    public ScheduledExecutorServiceMtcWrapper(ScheduledExecutorService scheduledExecutorService) {
        super(scheduledExecutorService);
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return scheduledExecutorService.schedule(MtContextRunnable.get(command), delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return scheduledExecutorService.schedule(MtContextCallable.get(callable), delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return scheduledExecutorService.scheduleAtFixedRate(MtContextRunnable.get(command), initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return scheduledExecutorService.scheduleWithFixedDelay(MtContextRunnable.get(command), initialDelay, delay, unit);
    }
}
