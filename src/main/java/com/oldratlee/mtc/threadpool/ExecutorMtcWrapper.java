package com.oldratlee.mtc.threadpool;

import com.oldratlee.mtc.MtContextRunnable;

import java.util.concurrent.Executor;

/**
 * {@link com.oldratlee.mtc.MtContext} Wrapper of {@link Executor},
 * transmit the {@link com.oldratlee.mtc.MtContext} from the task submit time of {@link Runnable}
 * to the execution time of {@link Runnable}.
 *
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
