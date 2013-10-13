package com.oldratlee.mtc.threadpool;

import com.oldratlee.mtc.MtContextCallable;
import com.oldratlee.mtc.MtContextRunnable;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * {@link com.oldratlee.mtc.MtContext} Wrapper of {@link java.util.concurrent.ScheduledExecutorService},
 * transmit the {@link com.oldratlee.mtc.MtContext} from the task submit time of {@link Runnable} or {@link Callable}
 * to the execution time of {@link Runnable} or {@link Callable}.
 *
 * @author ding.lid
 */
public class ScheduledExecutorServiceMtcWrapper extends ExecutorServiceMtcWrapper implements ScheduledExecutorService {
    final ScheduledExecutorService scheduledExecutorService;

    public ScheduledExecutorServiceMtcWrapper(ScheduledExecutorService scheduledExecutorService) {
        super(scheduledExecutorService);
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return schedule(MtContextRunnable.get(command), delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return schedule(MtContextCallable.get(callable), delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return scheduleAtFixedRate(MtContextRunnable.get(command), initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return scheduleWithFixedDelay(MtContextRunnable.get(command), initialDelay, delay, unit);
    }
}
