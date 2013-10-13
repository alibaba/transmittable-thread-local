package com.oldratlee.mtc.threadpool;

import com.oldratlee.mtc.MtContextRunnable;

import java.util.concurrent.Executor;

/**
 * @author ding.lid
 */
public class ExecutorMtcWrapper implements Executor {
    final Executor executor;

    public ExecutorMtcWrapper(Executor executor) {
        this.executor = executor;
    }

    @Override
    public void execute(Runnable command) {
        executor.execute(MtContextRunnable.get(command));
    }
}
