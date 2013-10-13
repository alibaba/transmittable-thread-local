package com.oldratlee.mtc;

import java.util.Map;

/**
 * Wrapper input {@link Runnable} to {@link MtContextRunnable}.
 *
 * @author ding.lid
 */
public final class MtContextRunnable implements Runnable {
    private final Map<String, Object> context;
    private final Runnable runnable;

    private MtContextRunnable(Runnable runnable) {
        context = MtContext.getContext().get();
        this.runnable = runnable;
    }

    @Override
    public void run() {
        MtContext mtContext = MtContext.getContext();
        final Map<String, Object> old = mtContext.get();
        mtContext.set(context);
        runnable.run();
        mtContext.set(old);
    }

    public Runnable getRunnable() {
        return runnable;
    }

    /**
     * Factory method, wrapper input {@link Runnable} to {@link MtContextRunnable}.
     *
     * @param runnable input {@link Runnable}
     * @return Wrapped {@link Runnable}
     */
    public static MtContextRunnable get(Runnable runnable) {
        if (runnable instanceof MtContextRunnable) {
            return (MtContextRunnable) runnable;
        }
        return new MtContextRunnable(runnable);
    }
}
