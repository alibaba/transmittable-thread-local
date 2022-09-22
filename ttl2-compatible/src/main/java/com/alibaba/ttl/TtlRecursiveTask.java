package com.alibaba.ttl;

import com.alibaba.ttl.spi.TtlEnhanced;

import java.util.concurrent.ForkJoinTask;

import static com.alibaba.ttl.TransmittableThreadLocal.Transmitter.*;

/**
 * A recursive result-bearing {@link ForkJoinTask} enhanced by {@link TransmittableThreadLocal}.
 * <p>
 * Recommend to use {@link com.alibaba.ttl.threadpool.agent.TtlAgent};
 * Specially for {@code Java 8} {@link java.util.stream.Stream} and {@link java.util.concurrent.CompletableFuture},
 * these async task are executed by {@link java.util.concurrent.ForkJoinPool} via {@link ForkJoinTask} at the bottom.
 *
 * @author LNAmp
 * @see java.util.concurrent.RecursiveTask
 * @see com.alibaba.ttl.threadpool.TtlForkJoinPoolHelper
 * @see com.alibaba.ttl.threadpool.agent.TtlAgent
 * @since 2.4.0
 */
public abstract class TtlRecursiveTask<V> extends ForkJoinTask<V> implements TtlEnhanced {

    private static final long serialVersionUID = 1814679366926362436L;

    private final Object captured = capture();

    protected TtlRecursiveTask() {
    }

    /**
     * The result of the computation.
     */
    V result;

    /**
     * The main computation performed by this task.
     *
     * @return the result of the computation
     */
    protected abstract V compute();

    /**
     * see {@link ForkJoinTask#getRawResult()}
     */
    public final V getRawResult() {
        return result;
    }

    /**
     * see {@link ForkJoinTask#setRawResult(Object)}
     */
    protected final void setRawResult(V value) {
        result = value;
    }

    /**
     * Implements execution conventions for RecursiveTask.
     */
    protected final boolean exec() {
        final Object backup = replay(captured);
        try {
            result = compute();
            return true;
        } finally {
            restore(backup);
        }
    }

}
