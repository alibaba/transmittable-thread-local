package com.alibaba.ttl;

import com.alibaba.ttl.spi.TtlEnhanced;

import java.util.concurrent.ForkJoinTask;

import static com.alibaba.ttl.TransmittableThreadLocal.Transmitter.*;

/**
 * A recursive result-bearing {@link ForkJoinTask} enhanced by {@link TransmittableThreadLocal}.
 *
 * @author LNAmp
 * @see java.util.concurrent.RecursiveTask
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
        Object backup = replay(captured);
        try {
            result = compute();
            return true;
        } finally {
            restore(backup);
        }
    }

}
