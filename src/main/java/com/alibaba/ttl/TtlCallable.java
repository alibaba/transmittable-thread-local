package com.alibaba.ttl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Collections;

/**
 * {@link TtlCallable} decorate {@link Callable}, so as to get {@link TransmittableThreadLocal}
 * and transmit it to the time of {@link Callable} execution, needed when use {@link Callable} to thread pool.
 * <p>
 * Use factory method {@link #get(Callable)} to get decorated instance.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see java.util.concurrent.Executor
 * @see java.util.concurrent.ExecutorService
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see java.util.concurrent.ScheduledThreadPoolExecutor
 * @see java.util.concurrent.Executors
 * @see java.util.concurrent.CompletionService
 * @see java.util.concurrent.ExecutorCompletionService
 * @since 0.9.0
 */
public final class TtlCallable<V> implements Callable<V> {
    private final AtomicReference<Map<TransmittableThreadLocal<?>, Object>> copiedRef;
    private final Callable<V> callable;
    private final boolean releaseTtlValueReferenceAfterCall;

    private TtlCallable(Callable<V> callable, boolean releaseTtlValueReferenceAfterCall) {
        this.copiedRef = new AtomicReference<Map<TransmittableThreadLocal<?>, Object>>(TransmittableThreadLocal.copy());
        this.callable = callable;
        this.releaseTtlValueReferenceAfterCall = releaseTtlValueReferenceAfterCall;
    }

    /**
     * wrap method {@link Callable#call()}.
     */
    @Override
    public V call() throws Exception {
        Map<TransmittableThreadLocal<?>, Object> copied = copiedRef.get();
        if (copied == null || releaseTtlValueReferenceAfterCall && !copiedRef.compareAndSet(copied, null)) {
            throw new IllegalStateException("TTL value reference is released after call!");
        }

        Map<TransmittableThreadLocal<?>, Object> backup = TransmittableThreadLocal.backupAndSetToCopied(copied);
        try {
            return callable.call();
        } finally {
            TransmittableThreadLocal.restoreBackup(backup);
        }
    }

    public Callable<V> getCallable() {
        return callable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TtlCallable<?> that = (TtlCallable<?>) o;

        return callable.equals(that.callable);
    }

    @Override
    public int hashCode() {
        return callable.hashCode();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " - " + callable.toString();
    }

    /**
     * Factory method, wrapper input {@link Callable} to {@link TtlCallable}.
     * <p>
     * This method is idempotent.
     *
     * @param callable input {@link Callable}
     * @return Wrapped {@link Callable}
     */
    public static <T> TtlCallable<T> get(Callable<T> callable) {
        return get(callable, false);
    }


    /**
     * Factory method, wrapper input {@link Callable} to {@link TtlCallable}.
     * <p>
     * This method is idempotent.
     *
     * @param callable                          input {@link Callable}
     * @param releaseTtlValueReferenceAfterCall release TTL value reference after run, avoid memory leak even if {@link TtlRunnable} is referred.
     * @return Wrapped {@link Callable}
     */
    public static <T> TtlCallable<T> get(Callable<T> callable, boolean releaseTtlValueReferenceAfterCall) {
        return get(callable, releaseTtlValueReferenceAfterCall, false);
    }

    /**
     * Factory method, wrapper input {@link Callable} to {@link TtlCallable}.
     * <p>
     * This method is idempotent.
     *
     * @param callable                          input {@link Callable}
     * @param releaseTtlValueReferenceAfterCall release TTL value reference after run, avoid memory leak even if {@link TtlRunnable} is referred.
     * @param idempotent                        is idempotent or not. {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
     * @return Wrapped {@link Callable}
     */
    public static <T> TtlCallable<T> get(Callable<T> callable, boolean releaseTtlValueReferenceAfterCall, boolean idempotent) {
        if (null == callable) {
            return null;
        }

        if (callable instanceof TtlCallable) {
            if (idempotent) {
                // avoid redundant decoration, and ensure idempotency
                return (TtlCallable<T>) callable;
            } else {
                throw new IllegalStateException("Already TtlCallable!");
            }
        }
        return new TtlCallable<T>(callable, releaseTtlValueReferenceAfterCall);
    }

    /**
     * wrapper input {@link Callable} Collection to {@link TtlCallable} Collection.
     *
     * @param tasks task to be wrapped
     * @return Wrapped {@link Callable}
     */
    public static <T> List<TtlCallable<T>> gets(Collection<? extends Callable<T>> tasks) {
        return gets(tasks, false, false);
    }

    /**
     * wrapper input {@link Callable} Collection to {@link TtlCallable} Collection.
     *
     * @param tasks                             task to be wrapped
     * @param releaseTtlValueReferenceAfterCall release TTL value reference after run, avoid memory leak even if {@link TtlRunnable} is referred.
     * @return Wrapped {@link Callable}
     */
    public static <T> List<TtlCallable<T>> gets(Collection<? extends Callable<T>> tasks, boolean releaseTtlValueReferenceAfterCall) {
        return gets(tasks, releaseTtlValueReferenceAfterCall, false);
    }

    /**
     * wrapper input {@link Callable} Collection to {@link TtlCallable} Collection.
     *
     * @param tasks                             task to be wrapped
     * @param releaseTtlValueReferenceAfterCall release TTL value reference after run, avoid memory leak even if {@link TtlRunnable} is referred.
     * @param idempotent                        is idempotent or not. {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
     * @return Wrapped {@link Callable}
     */
    public static <T> List<TtlCallable<T>> gets(Collection<? extends Callable<T>> tasks, boolean releaseTtlValueReferenceAfterCall, boolean idempotent) {
        if (null == tasks) {
            return Collections.emptyList();
        }
        List<TtlCallable<T>> copy = new ArrayList<TtlCallable<T>>();
        for (Callable<T> task : tasks) {
            copy.add(TtlCallable.get(task, releaseTtlValueReferenceAfterCall, idempotent));
        }
        return copy;
    }
}
