package com.alibaba.ttl3;

import com.alibaba.crr.composite.Backup;
import com.alibaba.crr.composite.Capture;
import com.alibaba.ttl3.spi.TtlEnhanced;

import java.util.concurrent.ForkJoinTask;

import static com.alibaba.ttl3.transmitter.Transmitter.*;

/**
 * A recursive resultless {@link ForkJoinTask} enhanced by {@link TransmittableThreadLocal}.
 * <p>
 * Recommend to use {@code  TTL Java Agent};
 * Specially for {@code Java 8} {@link java.util.stream.Stream} and {@link java.util.concurrent.CompletableFuture},
 * these async task are executed by {@link java.util.concurrent.ForkJoinPool} via {@link ForkJoinTask} at the bottom.
 *
 * @author LNAmp
 * @see java.util.concurrent.RecursiveAction
 * @see com.alibaba.ttl3.executor.TtlExecutors
 */
public abstract class TtlRecursiveAction extends ForkJoinTask<Void> implements TtlEnhanced {

    private static final long serialVersionUID = -5753568484583412377L;

    private final Capture captured = capture();

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
        final Backup backup = replay(captured);
        try {
            compute();
            return true;
        } finally {
            restore(backup);
        }
    }
}
