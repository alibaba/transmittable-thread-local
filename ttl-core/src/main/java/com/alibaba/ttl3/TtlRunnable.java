package com.alibaba.ttl3;

import com.alibaba.crr.composite.Backup;
import com.alibaba.crr.composite.Capture;
import com.alibaba.ttl3.spi.TtlAttachments;
import com.alibaba.ttl3.spi.TtlAttachmentsDelegate;
import com.alibaba.ttl3.spi.TtlEnhanced;
import com.alibaba.ttl3.spi.TtlWrapper;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.alibaba.ttl3.transmitter.Transmitter.*;

/**
 * {@link TtlRunnable} decorate {@link Runnable} to get {@link TransmittableThreadLocal} value
 * and transmit it to the time of {@link Runnable} execution, needed when use {@link Runnable} to thread pool.
 * <p>
 * Use factory methods {@link #get} / {@link #gets} to create instance.
 * <p>
 * Other TTL Wrapper for common {@code Functional Interface} see {@link TtlWrappers}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see com.alibaba.ttl3.executor.TtlExecutors
 * @see TtlWrappers
 * @see java.util.concurrent.Executor
 * @see java.util.concurrent.ExecutorService
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see java.util.concurrent.ScheduledThreadPoolExecutor
 * @see java.util.concurrent.Executors
 */
public final class TtlRunnable implements Runnable, TtlWrapper<Runnable>, TtlEnhanced, TtlAttachments {
    private final AtomicReference<Capture> capturedRef;
    private final Runnable runnable;
    private final boolean releaseTtlValueReferenceAfterRun;

    private TtlRunnable(@NonNull Runnable runnable, boolean releaseTtlValueReferenceAfterRun) {
        this.capturedRef = new AtomicReference<>(capture());
        this.runnable = runnable;
        this.releaseTtlValueReferenceAfterRun = releaseTtlValueReferenceAfterRun;
    }

    /**
     * wrap method {@link Runnable#run()}.
     */
    @Override
    public void run() {
        final Capture captured = capturedRef.get();
        if (captured == null || releaseTtlValueReferenceAfterRun && !capturedRef.compareAndSet(captured, null)) {
            throw new IllegalStateException("TTL value reference is released after run!");
        }

        final Backup backup = replay(captured);
        try {
            runnable.run();
        } finally {
            restore(backup);
        }
    }

    /**
     * return original/unwrapped {@link Runnable}.
     */
    @NonNull
    public Runnable getRunnable() {
        return unwrap();
    }

    /**
     * unwrap to original/unwrapped {@link Runnable}.
     *
     * @see TtlWrappers#unwrap(Object)
     */
    @NonNull
    @Override
    public Runnable unwrap() {
        return runnable;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " - " + runnable.toString();
    }

    /**
     * Factory method, wrap input {@link Runnable} to {@link TtlRunnable}.
     *
     * @param runnable input {@link Runnable}. if input is {@code null}, return {@code null}.
     * @return Wrapped {@link Runnable}
     * @throws IllegalStateException when input is {@link TtlRunnable} already.
     */
    @Nullable
    @Contract(value = "null -> null; !null -> !null", pure = true)
    public static TtlRunnable get(@Nullable Runnable runnable) {
        return get(runnable, false, false);
    }

    /**
     * Factory method, wrap input {@link Runnable} to {@link TtlRunnable}.
     *
     * @param runnable                         input {@link Runnable}. if input is {@code null}, return {@code null}.
     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlRunnable} is referred.
     * @return Wrapped {@link Runnable}
     * @throws IllegalStateException when input is {@link TtlRunnable} already.
     */
    @Nullable
    @Contract(value = "null, _ -> null; !null, _ -> !null", pure = true)
    public static TtlRunnable get(@Nullable Runnable runnable, boolean releaseTtlValueReferenceAfterRun) {
        return get(runnable, releaseTtlValueReferenceAfterRun, false);
    }

    /**
     * Factory method, wrap input {@link Runnable} to {@link TtlRunnable}.
     *
     * @param runnable                         input {@link Runnable}. if input is {@code null}, return {@code null}.
     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlRunnable} is referred.
     * @param idempotent                       is idempotent mode or not. if {@code true}, just return input {@link Runnable} when it's {@link TtlRunnable},
     *                                         otherwise throw {@link IllegalStateException}.
     *                                         <B><I>Caution</I></B>: {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
     * @return Wrapped {@link Runnable}
     * @throws IllegalStateException when input is {@link TtlRunnable} already and not idempotent.
     */
    @Nullable
    @Contract(value = "null, _, _ -> null; !null, _, _ -> !null", pure = true)
    public static TtlRunnable get(@Nullable Runnable runnable, boolean releaseTtlValueReferenceAfterRun, boolean idempotent) {
        if (runnable == null) return null;

        if (runnable instanceof TtlEnhanced) {
            // avoid redundant decoration, and ensure idempotency
            if (idempotent) return (TtlRunnable) runnable;
            else throw new IllegalStateException("Already TtlRunnable!");
        }
        return new TtlRunnable(runnable, releaseTtlValueReferenceAfterRun);
    }

    /**
     * wrap input {@link Runnable} Collection to {@link TtlRunnable} Collection.
     *
     * @param tasks task to be wrapped. if input is {@code null}, return {@code null}.
     * @return wrapped tasks
     * @throws IllegalStateException when input is {@link TtlRunnable} already.
     */
    @NonNull
    public static List<TtlRunnable> gets(@Nullable Collection<? extends Runnable> tasks) {
        return gets(tasks, false, false);
    }

    /**
     * wrap input {@link Runnable} Collection to {@link TtlRunnable} Collection.
     *
     * @param tasks                            task to be wrapped. if input is {@code null}, return {@code null}.
     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlRunnable} is referred.
     * @return wrapped tasks
     * @throws IllegalStateException when input is {@link TtlRunnable} already.
     */
    @NonNull
    public static List<TtlRunnable> gets(@Nullable Collection<? extends Runnable> tasks, boolean releaseTtlValueReferenceAfterRun) {
        return gets(tasks, releaseTtlValueReferenceAfterRun, false);
    }

    /**
     * wrap input {@link Runnable} Collection to {@link TtlRunnable} Collection.
     *
     * @param tasks                            task to be wrapped. if input is {@code null}, return {@code null}.
     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlRunnable} is referred.
     * @param idempotent                       is idempotent mode or not. if {@code true}, just return input {@link Runnable} when it's {@link TtlRunnable},
     *                                         otherwise throw {@link IllegalStateException}.
     *                                         <B><I>Caution</I></B>: {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
     * @return wrapped tasks
     * @throws IllegalStateException when input is {@link TtlRunnable} already and not idempotent.
     */
    @NonNull
    public static List<TtlRunnable> gets(@Nullable Collection<? extends Runnable> tasks, boolean releaseTtlValueReferenceAfterRun, boolean idempotent) {
        if (tasks == null) return Collections.emptyList();

        List<TtlRunnable> copy = new ArrayList<>();
        for (Runnable task : tasks) {
            copy.add(TtlRunnable.get(task, releaseTtlValueReferenceAfterRun, idempotent));
        }
        return copy;
    }

    /**
     * Unwrap {@link TtlRunnable} to the original/underneath one.
     * <p>
     * this method is {@code null}-safe, when input {@code Runnable} parameter is {@code null}, return {@code null};
     * if input {@code Runnable} parameter is not a {@link TtlRunnable} just return input {@code Runnable}.
     * <p>
     * so {@code TtlRunnable.unwrap(TtlRunnable.get(runnable))} will always return the same input {@code runnable} object.
     *
     * @see #get(Runnable)
     * @see TtlWrappers#unwrap(Object)
     */
    @Nullable
    @Contract(value = "null -> null; !null -> !null", pure = true)
    public static Runnable unwrap(@Nullable Runnable runnable) {
        if (!(runnable instanceof TtlRunnable)) return runnable;
        else return ((TtlRunnable) runnable).getRunnable();
    }

    /**
     * Unwrap {@link TtlRunnable} to the original/underneath one for collection.
     * <p>
     * Invoke {@link #unwrap(Runnable)} for each element in input collection.
     * <p>
     * This method is {@code null}-safe, when input {@code Runnable} parameter collection is {@code null}, return a empty list.
     *
     * @see #gets(Collection)
     * @see #unwrap(Runnable)
     */
    @NonNull
    public static List<Runnable> unwraps(@Nullable Collection<? extends Runnable> tasks) {
        if (tasks == null) return Collections.emptyList();

        List<Runnable> copy = new ArrayList<>();
        for (Runnable task : tasks) {
            if (!(task instanceof TtlRunnable)) copy.add(task);
            else copy.add(((TtlRunnable) task).getRunnable());
        }
        return copy;
    }

    private final TtlAttachmentsDelegate ttlAttachment = new TtlAttachmentsDelegate();

    /**
     * see {@link TtlAttachments#setTtlAttachment(String, Object)}
     */
    @Override
    public void setTtlAttachment(@NonNull String key, Object value) {
        ttlAttachment.setTtlAttachment(key, value);
    }

    /**
     * see {@link TtlAttachments#getTtlAttachment(String)}
     */
    @Override
    public <T> T getTtlAttachment(@NonNull String key) {
        return ttlAttachment.getTtlAttachment(key);
    }
}
