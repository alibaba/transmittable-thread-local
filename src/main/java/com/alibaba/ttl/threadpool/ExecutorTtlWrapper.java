package com.alibaba.ttl.threadpool;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.TtlRunnable;
import com.alibaba.ttl.spi.TtlEnhanced;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.concurrent.Executor;

/**
 * {@link TransmittableThreadLocal} Wrapper of {@link Executor},
 * transmit the {@link TransmittableThreadLocal} from the task submit time of {@link Runnable}
 * to the execution time of {@link Runnable}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 0.9.0
 */
class ExecutorTtlWrapper implements Executor, TtlEnhanced {
    private final Executor executor;

    ExecutorTtlWrapper(@NonNull Executor executor) {
        this.executor = executor;
    }

    @Override
    public void execute(@NonNull Runnable command) {
        executor.execute(TtlRunnable.get(command));
    }

    @NonNull
    public Executor unwrap() {
        return executor;
    }
}
