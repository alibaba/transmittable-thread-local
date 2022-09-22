package com.alibaba.ttl3.executor;

import com.alibaba.ttl3.TransmittableThreadLocal;
import com.alibaba.ttl3.TtlRunnable;
import com.alibaba.ttl3.spi.TtlEnhanced;
import com.alibaba.ttl3.spi.TtlWrapper;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.concurrent.Executor;

/**
 * {@link TransmittableThreadLocal} Wrapper of {@link Executor},
 * transmit the {@link TransmittableThreadLocal} from the task submit time of {@link Runnable}
 * to the execution time of {@link Runnable}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class ExecutorTtlWrapper implements Executor, TtlWrapper<Executor>, TtlEnhanced {
    private final Executor executor;
    protected final boolean idempotent;

    ExecutorTtlWrapper(@NonNull Executor executor, boolean idempotent) {
        this.executor = executor;
        this.idempotent = idempotent;
    }

    @Override
    public void execute(@NonNull Runnable command) {
        executor.execute(TtlRunnable.get(command, false, idempotent));
    }

    @NonNull
    @Override
    public Executor unwrap() {
        return executor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExecutorTtlWrapper that = (ExecutorTtlWrapper) o;

        if (idempotent != that.idempotent) return false;
        return executor.equals(that.executor);
    }

    @Override
    public int hashCode() {
        int result = executor.hashCode();
        result = 31 * result + (idempotent ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " - " + executor;
    }
}
