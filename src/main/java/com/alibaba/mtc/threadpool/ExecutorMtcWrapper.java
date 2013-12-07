package com.alibaba.mtc.threadpool;

import com.alibaba.mtc.MtContextRunnable;

import java.util.concurrent.Executor;

/**
 * {@link com.alibaba.mtc.MtContextThreadLocal} Wrapper of {@link Executor},
 * transmit the {@link com.alibaba.mtc.MtContextThreadLocal} from the task submit time of {@link Runnable}
 * to the execution time of {@link Runnable}.
 *
 * @author ding.lid
 * @since 0.9.0
 */
class ExecutorMtcWrapper implements Executor {
    final Executor executor;

    public ExecutorMtcWrapper(Executor executor) {
        this.executor = executor;
    }

    @Override
    public void execute(Runnable command) {
        executor.execute(MtContextRunnable.get(command));
    }
}
