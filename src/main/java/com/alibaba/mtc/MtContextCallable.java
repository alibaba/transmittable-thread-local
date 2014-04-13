package com.alibaba.mtc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link MtContextCallable} decorate {@link Callable}, so as to get {@link MtContextThreadLocal}
 * and transmit it to the time of {@link Callable} execution, needed when use {@link Callable} to thread pool.
 * <p/>
 * Use factory method {@link #get(Callable)} to get decorated instance.
 *
 * @author ding.lid
 * @see java.util.concurrent.Executor
 * @see java.util.concurrent.ExecutorService
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see java.util.concurrent.ScheduledThreadPoolExecutor
 * @see java.util.concurrent.Executors
 * @see java.util.concurrent.CompletionService
 * @see java.util.concurrent.ExecutorCompletionService
 * @since 0.9.0
 */
public final class MtContextCallable<V> implements Callable<V> {
    private final AtomicReference<Map<MtContextThreadLocal<?>, Object>> copiedRef;
    private final Callable<V> callable;
    private final boolean releaseMtContextAfterCall;


    private MtContextCallable(Callable<V> callable, boolean releaseMtContextAfterCall) {
        this.copiedRef = new AtomicReference<Map<MtContextThreadLocal<?>, Object>>(MtContextThreadLocal.copy());
        this.callable = callable;
        this.releaseMtContextAfterCall = releaseMtContextAfterCall;
    }

    /**
     * wrap method {@link Callable#call()}.
     */
    @Override
    public V call() throws Exception {
        Map<MtContextThreadLocal<?>, Object> copied = copiedRef.get();
        if (copied == null || releaseMtContextAfterCall && !copiedRef.compareAndSet(copied, null)) {
            throw new IllegalStateException("MtContext is released!");
        }

        Map<MtContextThreadLocal<?>, Object> backup = MtContextThreadLocal.backupAndSet(copied);
        try {
            return callable.call();
        } finally {
            MtContextThreadLocal.restore(backup);
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
        return get(callable, false);
    }


    /**
     * Factory method, wrapper input {@link Callable} to {@link MtContextCallable}.
     * <p/>
     * This method is idempotent.
     *
     * @param callable                  input {@link Callable}
     * @param releaseMtContextAfterCall release MtContext after run, avoid memory leak even if {@link MtContextRunnable} is referred.
     * @return Wrapped {@link Callable}
     */
    public static <T> MtContextCallable<T> get(Callable<T> callable, boolean releaseMtContextAfterCall) {
        return get(callable, releaseMtContextAfterCall, false);
    }

    /**
     * Factory method, wrapper input {@link Callable} to {@link MtContextCallable}.
     * <p/>
     * This method is idempotent.
     *
     * @param callable                  input {@link Callable}
     * @param releaseMtContextAfterCall release MtContext after run, avoid memory leak even if {@link MtContextRunnable} is referred.
     * @param idempotent                is idempotent or not. {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
     * @return Wrapped {@link Callable}
     */
    public static <T> MtContextCallable<T> get(Callable<T> callable, boolean releaseMtContextAfterCall, boolean idempotent) {
        if (null == callable) {
            return null;
        }

        if (callable instanceof MtContextCallable) {
            if (idempotent) {
                // avoid redundant decoration, and ensure idempotency
                return (MtContextCallable<T>) callable;
            } else {
                throw new IllegalStateException("Already MtContextCallable!");
            }
        }
        return new MtContextCallable<T>(callable, releaseMtContextAfterCall);
    }

    /**
     * wrapper input {@link Callable} Collection to {@link MtContextCallable} Collection.
     *
     * @param tasks task to be wrapped
     * @return Wrapped {@link Callable}
     */
    public static <T> List<MtContextCallable<T>> gets(Collection<? extends Callable<T>> tasks) {
        return gets(tasks, false, false);
    }

    /**
     * wrapper input {@link Callable} Collection to {@link MtContextCallable} Collection.
     *
     * @param tasks                     task to be wrapped
     * @param releaseMtContextAfterCall release MtContext after run, avoid memory leak even if {@link MtContextRunnable} is referred.
     * @return Wrapped {@link Callable}
     */
    public static <T> List<MtContextCallable<T>> gets(Collection<? extends Callable<T>> tasks, boolean releaseMtContextAfterCall) {
        if (null == tasks) {
            return null;
        }
        List<MtContextCallable<T>> copy = new ArrayList<MtContextCallable<T>>();
        for (Callable<T> task : tasks) {
            copy.add(MtContextCallable.get(task, releaseMtContextAfterCall, false));
        }
        return copy;
    }

    /**
     * wrapper input {@link Callable} Collection to {@link MtContextCallable} Collection.
     *
     * @param tasks                     task to be wrapped
     * @param releaseMtContextAfterCall release MtContext after run, avoid memory leak even if {@link MtContextRunnable} is referred.
     * @param idempotent                is idempotent or not. {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
     * @return Wrapped {@link Callable}
     */
    public static <T> List<MtContextCallable<T>> gets(Collection<? extends Callable<T>> tasks, boolean releaseMtContextAfterCall, boolean idempotent) {
        if (null == tasks) {
            return null;
        }
        List<MtContextCallable<T>> copy = new ArrayList<MtContextCallable<T>>();
        for (Callable<T> task : tasks) {
            copy.add(MtContextCallable.get(task, releaseMtContextAfterCall, idempotent));
        }
        return copy;
    }
}
