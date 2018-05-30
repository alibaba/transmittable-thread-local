package com.alibaba.ttl;

import java.util.Map;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A recursive resultless {@link ForkJoinTask} enhanced by {@link TransmittableThreadLocal}.
 * This class establishes conventions to parameterize resultless actions as
 * {@code Void} {@code ForkJoinTask}s. Because {@code null} is the
 * only valid value of type {@code Void}, methods such as {@code join}
 * always return {@code null} upon completion.
 *
 * @author LNAmp
 * @see java.util.concurrent.RecursiveAction
 * @since 2.3.0
 */
public abstract class TtlRecursiveAction extends ForkJoinTask<Void> {

    private static final long serialVersionUID = -5753568484583412377L;

    protected final boolean releaseTtlValueReferenceAfterCall;

    protected final AtomicReference<Map<TransmittableThreadLocal<?>, Object>> copiedRef
        = new AtomicReference<Map<TransmittableThreadLocal<?>, Object>>(TransmittableThreadLocal.copy());

    protected TtlRecursiveAction() {
        this(false);
    }

    protected TtlRecursiveAction(boolean releaseTtlValueReferenceAfterCall) {
        this.releaseTtlValueReferenceAfterCall = releaseTtlValueReferenceAfterCall;
    }

    /**
     * The main computation performed by this task.
     */
    protected abstract void compute();

    /**
     * Always returns {@code null}.
     *
     * @return {@code null} always
     */
    public final Void getRawResult() { return null; }

    /**
     * Requires null completion value.
     */
    protected final void setRawResult(Void mustBeNull) { }

    /**
     * Implements execution conventions for RecursiveActions.
     */
    protected final boolean exec() {
        Map<TransmittableThreadLocal<?>, Object> copied = copiedRef.get();
        if (copied == null || releaseTtlValueReferenceAfterCall && !copiedRef.compareAndSet(copied, null)) {
            throw new IllegalStateException("TTL value reference is released after call!");
        }

        Map<TransmittableThreadLocal<?>, Object> backup = TransmittableThreadLocal.backupAndSetToCopied(copied);
        try {
            compute();
            return true;
        } finally {
            TransmittableThreadLocal.restoreBackup(backup);
        }
    }
}
