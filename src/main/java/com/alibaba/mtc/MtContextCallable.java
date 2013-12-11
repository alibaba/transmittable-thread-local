package com.alibaba.mtc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * {@link MtContextCallable} decorate {@link Callable}, so as to get @{@link MtContextThreadLocal}
 * and transmit it to the time of {@link Callable} execution, needed when use {@link Callable} to thread pool.
 * <p/>
 * Use factory method {@link #get(Callable)} to get decorated instance.
 *
 * @author ding.lid
 * @see java.util.concurrent.CompletionService
 * @see java.util.concurrent.ExecutorCompletionService
 * @see java.util.concurrent.Executors
 * @since 0.9.0
 */
public final class MtContextCallable<V> implements Callable<V> {
    private final Map<MtContextThreadLocal<?>, Object> copied;
    private final Callable<V> callable;

    private MtContextCallable(Callable<V> callable) {
        copied = MtContextThreadLocal.copy();
        this.callable = callable;
    }

    /**
     * wrap method {@link Callable#call()}.
     */
    @Override
    public V call() throws Exception {
        Map<MtContextThreadLocal<?>, Object> backup = MtContextThreadLocal.backupAndSet(copied);
        try {
            return callable.call();
        } finally {
            MtContextThreadLocal.restore(backup);
        }
        // FIXME add attribute so as to release copied after call
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
            return null;
        }

        if (callable instanceof MtContextCallable) { // avoid redundant decoration, and ensure idempotency
            return (MtContextCallable<T>) callable;
        }
        return new MtContextCallable<T>(callable);
    }

    /**
     * wrapper input {@link Callable} Collection to {@link MtContextCallable} Collection.
     */
    public static <T> List<MtContextCallable<T>> gets(Collection<? extends Callable<T>> tasks) {
        if (null == tasks) {
            return null;
        }
        List<MtContextCallable<T>> copy = new ArrayList<MtContextCallable<T>>();
        for (Callable<T> task : tasks) {
            copy.add(MtContextCallable.get(task));
        }
        return copy;
    }
}
