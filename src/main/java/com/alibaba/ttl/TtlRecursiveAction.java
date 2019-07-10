package com.alibaba.ttl;

import com.alibaba.ttl.spi.TtlEnhanced;

import java.util.concurrent.ForkJoinTask;

import static com.alibaba.ttl.TransmittableThreadLocal.Transmitter.*;

/**
 * A recursive resultless {@link ForkJoinTask} enhanced by {@link TransmittableThreadLocal}.
 *
 * @author LNAmp
 * @see java.util.concurrent.RecursiveAction
 * @since 2.4.0
 */
public abstract class TtlRecursiveAction extends ForkJoinTask<Void> implements TtlEnhanced {

    private static final long serialVersionUID = -5753568484583412377L;

    private final Object captured = capture();

    protected TtlRecursiveAction() {
    }

    /**
     * The main computation performed by this task.
     */
    protected abstract void compute();

    /**
     * see {@link ForkJoinTask#getRawResult()}
     */
    public final Void getRawResult() {
        return null;
    }

    /**
     * see {@link ForkJoinTask#setRawResult(Object)}
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
