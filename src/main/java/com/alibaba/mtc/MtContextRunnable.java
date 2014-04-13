package com.alibaba.mtc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link MtContextRunnable} decorate {@link Runnable}, so as to get {@link MtContextThreadLocal}
 * and transmit it to the time of {@link Runnable} execution, needed when use {@link Runnable} to thread pool.
 * <p/>
 * Use factory method {@link #get(Runnable)} to create instance.
 *
 * @author ding.lid
 * @see java.util.concurrent.Executor
 * @see java.util.concurrent.ExecutorService
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see java.util.concurrent.ScheduledThreadPoolExecutor
 * @see java.util.concurrent.Executors
 * @since 0.9.0
 */
public final class MtContextRunnable implements Runnable {
    private final AtomicReference<Map<MtContextThreadLocal<?>, Object>> copiedRef;
    private final Runnable runnable;
    private final boolean releaseMtContextAfterRun;

    private MtContextRunnable(Runnable runnable, boolean releaseMtContextAfterRun) {
        this.copiedRef = new AtomicReference<Map<MtContextThreadLocal<?>, Object>>(MtContextThreadLocal.copy());
        this.runnable = runnable;
        this.releaseMtContextAfterRun = releaseMtContextAfterRun;
    }

    /**
     * wrap method {@link Runnable#run()}.
     */
    @Override
    public void run() {
        Map<MtContextThreadLocal<?>, Object> copied = copiedRef.get();
        if (copied == null || releaseMtContextAfterRun && !copiedRef.compareAndSet(copied, null)) {
            throw new IllegalStateException("MtContext is released!");
        }

        Map<MtContextThreadLocal<?>, Object> backup = MtContextThreadLocal.backupAndSet(copied);
        try {
            runnable.run();
        } finally {
            MtContextThreadLocal.restore(backup);
        }
    }

    public Runnable getRunnable() {
        return runnable;
    }

    /**
     * Factory method, wrapper input {@link Runnable} to {@link MtContextRunnable}.
     * <p/>
     * This method is idempotent.
     *
     * @param runnable input {@link Runnable}
     * @return Wrapped {@link Runnable}
     */
    public static MtContextRunnable get(Runnable runnable) {
        return get(runnable, false, false);
    }

    /**
     * Factory method, wrapper input {@link Runnable} to {@link MtContextRunnable}.
     * <p/>
     * This method is idempotent.
     *
     * @param runnable                 input {@link Runnable}
     * @param releaseMtContextAfterRun release MtContext after run, avoid memory leak even if {@link MtContextRunnable} is referred.
     * @return Wrapped {@link Runnable}
     */
    public static MtContextRunnable get(Runnable runnable, boolean releaseMtContextAfterRun) {
        return get(runnable, releaseMtContextAfterRun, false);
    }

    /**
     * Factory method, wrapper input {@link Runnable} to {@link MtContextRunnable}.
     * <p/>
     * This method is idempotent.
     *
     * @param runnable                 input {@link Runnable}
     * @param releaseMtContextAfterRun release MtContext after run, avoid memory leak even if {@link MtContextRunnable} is referred.
     * @param idempotent               is idempotent or not. {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
     * @return Wrapped {@link Runnable}
     */
    public static MtContextRunnable get(Runnable runnable, boolean releaseMtContextAfterRun, boolean idempotent) {
        if (null == runnable) {
            return null;
        }

        if (runnable instanceof MtContextRunnable) {
            if (idempotent) {
                // avoid redundant decoration, and ensure idempotency
                return (MtContextRunnable) runnable;
            } else {
                throw new IllegalStateException("Already MtContextRunnable!");
            }
        }
        return new MtContextRunnable(runnable, releaseMtContextAfterRun);
    }

    /**
     * wrapper input {@link Runnable} Collection to {@link MtContextRunnable} Collection.
     *
     * @param tasks task to be wrapped
     * @return wrapped tasks
     */
    public static List<MtContextRunnable> gets(Collection<? extends Runnable> tasks) {
        return gets(tasks, false, false);
    }

    /**
     * wrapper input {@link Runnable} Collection to {@link MtContextRunnable} Collection.
     *
     * @param tasks                    task to be wrapped
     * @param releaseMtContextAfterRun release MtContext after run, avoid memory leak even if {@link MtContextRunnable} is referred.
     * @return wrapped tasks
     */
    public static List<MtContextRunnable> gets(Collection<? extends Runnable> tasks, boolean releaseMtContextAfterRun) {
        if (null == tasks) {
            return null;
        }
        List<MtContextRunnable> copy = new ArrayList<MtContextRunnable>();
        for (Runnable task : tasks) {
            copy.add(MtContextRunnable.get(task, releaseMtContextAfterRun, false));
        }
        return copy;
    }

    /**
     * wrapper input {@link Runnable} Collection to {@link MtContextRunnable} Collection.
     *
     * @param tasks                    task to be wrapped
     * @param releaseMtContextAfterRun release MtContext after run, avoid memory leak even if {@link MtContextRunnable} is referred.
     * @param idempotent                is idempotent or not. {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
     * @return wrapped tasks
     */
    public static List<MtContextRunnable> gets(Collection<? extends Runnable> tasks, boolean releaseMtContextAfterRun, boolean idempotent) {
        if (null == tasks) {
            return null;
        }
        List<MtContextRunnable> copy = new ArrayList<MtContextRunnable>();
        for (Runnable task : tasks) {
            copy.add(MtContextRunnable.get(task, releaseMtContextAfterRun, idempotent));
        }
        return copy;
    }
}
