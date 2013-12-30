package com.alibaba.mtc.threadpool;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Factory Utils for getting MtContext Wrapper of jdk executors.
 *
 * @author ding.lid
 * @since 0.9.0
 */
public final class MtContextExecutors {
    /**
     * {@link com.alibaba.mtc.MtContextThreadLocal} Wrapper of {@link Executor},
     * transmit the {@link com.alibaba.mtc.MtContextThreadLocal} from the task submit time of {@link Runnable}
     * to the execution time of {@link Runnable}.
     */
    public static Executor getMtcExecutor(Executor executor) {
        if (null == executor || executor instanceof ExecutorMtcWrapper) {
            return executor;
        }
        return new ExecutorMtcWrapper(executor);
    }

    /**
     * {@link com.alibaba.mtc.MtContextThreadLocal} Wrapper of {@link ExecutorService},
     * transmit the {@link com.alibaba.mtc.MtContextThreadLocal} from the task submit time of {@link Runnable} or {@link java.util.concurrent.Callable}
     * to the execution time of {@link Runnable} or {@link java.util.concurrent.Callable}.
     */
    public static ExecutorService getMtcExecutorService(ExecutorService executorService) {
        if (executorService == null || executorService instanceof ExecutorServiceMtcWrapper) {
            return executorService;
        }
        return new ExecutorServiceMtcWrapper(executorService);
    }

    /**
     * {@link com.alibaba.mtc.MtContextThreadLocal} Wrapper of {@link ScheduledExecutorService},
     * transmit the {@link com.alibaba.mtc.MtContextThreadLocal } from the task submit time of {@link Runnable} or {@link java.util.concurrent.Callable}
     * to the execution time of {@link Runnable} or {@link java.util.concurrent.Callable}.
     */
    public static ScheduledExecutorService getMtcScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
        if (scheduledExecutorService == null || scheduledExecutorService instanceof ScheduledExecutorServiceMtcWrapper) {
            return scheduledExecutorService;
        }
        return new ScheduledExecutorServiceMtcWrapper(scheduledExecutorService);
    }

    private MtContextExecutors() {
    }
}
