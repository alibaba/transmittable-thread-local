package com.alibaba.mtc.threadpool;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author ding.lid
 * @since 0.9.0
 */
public class MtContextExecutors {
    /**
     * {@link com.alibaba.mtc.MtContext} Wrapper of {@link Executor},
     * transmit the {@link com.alibaba.mtc.MtContext} from the task submit time of {@link Runnable}
     * to the execution time of {@link Runnable}.
     */
    public static Executor getMtcExecutor(Executor executor) {
        if (null == executor || executor instanceof ExecutorMtcWrapper) {
            return executor;
        }
        return new ExecutorMtcWrapper(executor);
    }

    /**
     * {@link com.alibaba.mtc.MtContext} Wrapper of {@link ExecutorService},
     * transmit the {@link com.alibaba.mtc.MtContext} from the task submit time of {@link Runnable} or {@link java.util.concurrent.Callable}
     * to the execution time of {@link Runnable} or {@link java.util.concurrent.Callable}.
     */
    public static ExecutorService getMtcExecutorService(ExecutorService executorService) {
        if (executorService == null || executorService instanceof ExecutorServiceMtcWrapper) {
            return executorService;
        }
        return new ExecutorServiceMtcWrapper(executorService);
    }

    /**
     * {@link com.alibaba.mtc.MtContext} Wrapper of {@link ScheduledExecutorService},
     * transmit the {@link com.alibaba.mtc.MtContext} from the task submit time of {@link Runnable} or {@link Callable}
     * to the execution time of {@link Runnable} or {@link Callable}.
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
