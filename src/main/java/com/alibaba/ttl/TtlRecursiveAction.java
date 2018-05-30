package com.alibaba.ttl;

import java.util.concurrent.ForkJoinTask;

import static com.alibaba.ttl.TransmittableThreadLocal.Transmitter.*;

/**
 * A recursive resultless {@link ForkJoinTask} enhanced by {@link TransmittableThreadLocal}.
 *
 * @author LNAmp
 * @see java.util.concurrent.RecursiveAction
 * @since 2.4.0
 */
public abstract class TtlRecursiveAction extends ForkJoinTask<Void> {

    private static final long serialVersionUID = -5753568484583412377L;

    private final Object captured = capture();

    protected TtlRecursiveAction() {
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
    public final Void getRawResult() {
        return null;
    }

    /**
     * Requires null completion value.
     */
    protected final void setRawResult(Void mustBeNull) {
    }

    /**
     * Implements execution conventions for RecursiveActions.
     */
    protected final boolean exec() {
        Object backup = replay(captured);
        try {
            compute();
            return true;
        } finally {
            restore(backup);
        }
    }
}
