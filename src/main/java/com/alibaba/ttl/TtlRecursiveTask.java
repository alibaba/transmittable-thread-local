package com.alibaba.ttl;

import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicReference;

import static com.alibaba.ttl.TransmittableThreadLocal.Transmitter.*;

/**
 * A recursive result-bearing {@link ForkJoinTask} enhanced by {@link TransmittableThreadLocal}.
 *
 * @author LNAmp
 * @see java.util.concurrent.RecursiveTask
 * @since 2.3.0
 */
public abstract class TtlRecursiveTask<V> extends ForkJoinTask<V> {

    private static final long serialVersionUID = 1814679366926362436L;

    protected final boolean releaseTtlValueReferenceAfterCall;

    protected final AtomicReference<Object> capturedRef = new AtomicReference<>(capture());

    protected TtlRecursiveTask() {
        this(false);
    }

    protected TtlRecursiveTask(boolean releaseTtlValueReferenceAfterCall) {
        this.releaseTtlValueReferenceAfterCall = releaseTtlValueReferenceAfterCall;
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

    public final V getRawResult() {
        return result;
    }

    protected final void setRawResult(V value) {
        result = value;
    }

    /**
     * Implements execution conventions for RecursiveTask.
     */
    protected final boolean exec() {
        Object captured = capturedRef.get();
        if (captured == null || releaseTtlValueReferenceAfterCall && !capturedRef.compareAndSet(captured, null)) {
            throw new IllegalStateException("TTL value reference is released after exec!");
        }

        Object backup = replay(captured);
        try {
            result = compute();
            return true;
        } finally {
            restore(backup);
        }
    }

}
