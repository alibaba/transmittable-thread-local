package com.oldratlee.mtc.threadpool;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author ding.lid
 */
public class MtContextExecutors {
    public static Executor getMtcExecutor(Executor executor) {
        if (null == executor || executor instanceof ExecutorMtcWrapper) {
            return executor;
        }
        return new ExecutorMtcWrapper(executor);
    }

    public static ExecutorService getMtcExecutorService(ExecutorService executorService) {
        if (executorService == null || executorService instanceof ExecutorServiceMtcWrapper) {
            return executorService;
        }
        return new ExecutorServiceMtcWrapper(executorService);
    }

    public static ScheduledExecutorService getMtcScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
        if (scheduledExecutorService == null || scheduledExecutorService instanceof ScheduledExecutorServiceMtcWrapper) {
            return scheduledExecutorService;
        }
        return new ScheduledExecutorServiceMtcWrapper(scheduledExecutorService);
    }

    private MtContextExecutors() {
    }
}
