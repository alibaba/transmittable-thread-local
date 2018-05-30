package com.alibaba.ttl;

import java.util.Map;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A recursive result-bearing {@link ForkJoinTask} enhanced by {@link TransmittableThreadLocal}.
 *
 *
 * @see java.util.concurrent.RecursiveTask
 * @author LNAmp
 * @since 2.3.0
 */
public abstract class TtlRecursiveTask<V> extends ForkJoinTask<V> {

    private static final long serialVersionUID = 1814679366926362436L;

    protected final boolean releaseTtlValueReferenceAfterCall;

    protected final AtomicReference<Map<TransmittableThreadLocal<?>, Object>> copiedRef
        = new AtomicReference<Map<TransmittableThreadLocal<?>, Object>>(TransmittableThreadLocal.copy());

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
        Map<TransmittableThreadLocal<?>, Object> copied = copiedRef.get();
        if (copied == null || releaseTtlValueReferenceAfterCall && !copiedRef.compareAndSet(copied, null)) {
            throw new IllegalStateException("TTL value reference is released after call!");
        }

        Map<TransmittableThreadLocal<?>, Object> backup = TransmittableThreadLocal.backupAndSetToCopied(copied);
        try {
            result = compute();
            return true;
        } finally {
            TransmittableThreadLocal.restoreBackup(backup);
        }
    }

}
