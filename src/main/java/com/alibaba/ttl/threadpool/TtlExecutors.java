package com.alibaba.ttl.threadpool;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Factory Utils for getting TTL Wrapper of jdk executors.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 0.9.0
 * @see java.util.concurrent.Executor
 * @see java.util.concurrent.ExecutorService
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see java.util.concurrent.ScheduledThreadPoolExecutor
 * @see java.util.concurrent.Executors
 * @see java.util.concurrent.CompletionService
 * @see java.util.concurrent.ExecutorCompletionService
 */
public final class TtlExecutors {
    /**
     * {@link TransmittableThreadLocal} Wrapper of {@link Executor},
     * transmit the {@link TransmittableThreadLocal} from the task submit time of {@link Runnable}
     * to the execution time of {@link Runnable}.
     */
    public static Executor getTtlExecutor(Executor executor) {
        if (null == executor || executor instanceof ExecutorTtlWrapper) {
            return executor;
        }
        return new ExecutorTtlWrapper(executor);
    }

    /**
     * {@link TransmittableThreadLocal} Wrapper of {@link ExecutorService},
     * transmit the {@link TransmittableThreadLocal} from the task submit time of {@link Runnable} or {@link java.util.concurrent.Callable}
     * to the execution time of {@link Runnable} or {@link java.util.concurrent.Callable}.
     */
    public static ExecutorService getTtlExecutorService(ExecutorService executorService) {
        if (executorService == null || executorService instanceof ExecutorServiceTtlWrapper) {
            return executorService;
        }
        return new ExecutorServiceTtlWrapper(executorService);
    }

    /**
     * {@link TransmittableThreadLocal} Wrapper of {@link ScheduledExecutorService},
     * transmit the {@link TransmittableThreadLocal } from the task submit time of {@link Runnable} or {@link java.util.concurrent.Callable}
     * to the execution time of {@link Runnable} or {@link java.util.concurrent.Callable}.
     */
    public static ScheduledExecutorService getTtlScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
        if (scheduledExecutorService == null || scheduledExecutorService instanceof ScheduledExecutorServiceTtlWrapper) {
            return scheduledExecutorService;
        }
        return new ScheduledExecutorServiceTtlWrapper(scheduledExecutorService);
    }

    private TtlExecutors() {
    }
}
