package com.oldratlee.mtc;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * {@link MtContextCallable} uses to get @{@link MtContext}
 * and transmit it to the time of {@link Callable} execution,
 * so as to use {@link Callable} to thread pool.
 * <p/>
 * Use factory method {@link #get(Callable)} to create instance.
 *
 * @author ding.lid
 * @see java.util.concurrent.CompletionService
 * @see java.util.concurrent.ExecutorCompletionService
 * @see java.util.concurrent.Executors
 * @since 0.9.0
 */
public final class MtContextCallable<V> implements Callable<V> {
    private final Map<String, Object> context;
    private final Callable<V> callable;

    private MtContextCallable(Callable<V> callable) {
        context = MtContext.getContext().get();
        this.callable = callable;
    }

    /**
     * wrap method {@link Callable#call()}.
     */
    @Override
    public V call() throws Exception {
        MtContext mtContext = MtContext.getContext();
        final Map<String, Object> old = mtContext.get();
        try {
            mtContext.set(context);
            return callable.call();
        } finally {
            mtContext.set(old); // restore MtContext
        }
    }

    public Callable<V> getCallable() {
        return callable;
    }

    /**
     * Factory method, wrapper input {@link Callable} to {@link MtContextCallable}.
     * <p/>
     * This method is idempotent.
     *
     * @param callable input {@link Callable}
     * @return Wrapped {@link Callable}
     */
    public static <T> MtContextCallable<T> get(Callable<T> callable) {
        if (null == callable) {
            throw new NullPointerException("input argument is null!");
        }

        if (callable instanceof MtContextCallable) { // avoid redundant decoration, and ensure idempotency
            return (MtContextCallable<T>) callable;
        }
        return new MtContextCallable<T>(callable);
    }
}
