package com.alibaba.ttl;

import com.alibaba.ttl.spi.TtlEnhanced;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.alibaba.ttl.TransmittableThreadLocal.Transmitter.*;

/**
 * {@link TtlTimerTask} decorate {@link TimerTask}, so as to get {@link TransmittableThreadLocal}
 * and transmit it to the time of {@link TtlTimerTask} execution, needed when use {@link TtlTimerTask} to {@link java.util.TimerTask}.
 * <p>
 * Use factory method {@link #get(TimerTask)} to create instance.
 * <p>
 * <b>NOTE:</b>
 * The {@link TtlTimerTask} make the the method {@link TimerTask#scheduledExecutionTime()} in
 * the origin {@link TimerTask} lose effectiveness!
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see java.util.Timer
 * @see TimerTask
 * @see <a href="https://alibaba.github.io/Alibaba-Java-Coding-Guidelines/#concurrency">Alibaba Java Coding Guidelines - Concurrency - Item 10: [Mandatory] Run multiple TimeTask by using ScheduledExecutorService rather than Timer because Timer will kill all running threads in case of failing to catch exceptions.</a>
 * @since 0.9.1
 * @deprecated Use {@link TtlRunnable}, {@link java.util.concurrent.ScheduledExecutorService} instead of {@link java.util.Timer}, {@link java.util.TimerTask}.
 */
@Deprecated
public final class TtlTimerTask extends TimerTask implements TtlEnhanced {
    private final AtomicReference<Object> capturedRef;
    private final TimerTask timerTask;
    private final boolean releaseTtlValueReferenceAfterRun;

    private TtlTimerTask(@NonNull TimerTask timerTask, boolean releaseTtlValueReferenceAfterRun) {
        this.capturedRef = new AtomicReference<Object>(capture());
        this.timerTask = timerTask;
        this.releaseTtlValueReferenceAfterRun = releaseTtlValueReferenceAfterRun;
    }

    /**
     * wrap method {@link TimerTask#run()}.
     */
    @Override
    public void run() {
        Object captured = capturedRef.get();
        if (captured == null || releaseTtlValueReferenceAfterRun && !capturedRef.compareAndSet(captured, null)) {
            throw new IllegalStateException("TTL value reference is released after run!");
        }

        Object backup = replay(captured);
        try {
            timerTask.run();
        } finally {
            restore(backup);
        }
    }

    @Override
    public boolean cancel() {
        timerTask.cancel();
        return super.cancel();
    }

    @NonNull
    public TimerTask getTimerTask() {
        return timerTask;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TtlTimerTask that = (TtlTimerTask) o;

        return timerTask != null ? timerTask.equals(that.timerTask) : that.timerTask == null;
    }

    @Override
    public int hashCode() {
        return timerTask != null ? timerTask.hashCode() : 0;
    }

    /**
     * Factory method, wrap input {@link TimerTask} to {@link TtlTimerTask}.
     * <p>
     * This method is idempotent.
     *
     * @param timerTask input {@link TimerTask}
     * @return Wrapped {@link TimerTask}
     */
    @Nullable
    public static TtlTimerTask get(@Nullable TimerTask timerTask) {
        return get(timerTask, false, false);
    }

    /**
     * Factory method, wrap input {@link TimerTask} to {@link TtlTimerTask}.
     * <p>
     * This method is idempotent.
     *
     * @param timerTask                        input {@link TimerTask}
     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlTimerTask} is referred.
     * @return Wrapped {@link TimerTask}
     */
    @Nullable
    public static TtlTimerTask get(@Nullable TimerTask timerTask, boolean releaseTtlValueReferenceAfterRun) {
        return get(timerTask, releaseTtlValueReferenceAfterRun, false);
    }

    /**
     * Factory method, wrap input {@link TimerTask} to {@link TtlTimerTask}.
     * <p>
     * This method is idempotent.
     *
     * @param timerTask                        input {@link TimerTask}
     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlTimerTask} is referred.
     * @param idempotent                       is idempotent or not. {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
     * @return Wrapped {@link TimerTask}
     */
    @Nullable
    public static TtlTimerTask get(@Nullable TimerTask timerTask, boolean releaseTtlValueReferenceAfterRun, boolean idempotent) {
        if (null == timerTask) return null;

        if (timerTask instanceof TtlEnhanced) {
            // avoid redundant decoration, and ensure idempotency
            if (idempotent) return (TtlTimerTask) timerTask;
            else throw new IllegalStateException("Already TtlTimerTask!");
        }
        return new TtlTimerTask(timerTask, releaseTtlValueReferenceAfterRun);
    }

    /**
     * Unwrap {@link TtlTimerTask} to the original/underneath one.
     * <p>
     * this method is {@code null}-safe, when input {@code TimerTask} parameter is {@code null}, return {@code null};
     * if input {@code TimerTask} parameter is not a {@link TtlTimerTask} just return input {@code TimerTask}.
     *
     * @see #get(TimerTask)
     * @since 2.10.2
     */
    @Nullable
    public static TimerTask unwrap(@Nullable TimerTask timerTask) {
        if (!(timerTask instanceof TtlTimerTask)) return timerTask;
        else return ((TtlTimerTask) timerTask).getTimerTask();
    }

    /**
     * Unwrap {@link TtlTimerTask} to the original/underneath one.
     * <p>
     * Invoke {@link #unwrap(TimerTask)} for each element in input collection.
     * <p>
     * This method is {@code null}-safe, when input {@code TimerTask} parameter is {@code null}, return a empty list.
     *
     * @see #unwrap(TimerTask)
     * @since 2.10.2
     */
    @NonNull
    public static List<TimerTask> unwraps(@Nullable Collection<? extends TimerTask> tasks) {
        if (null == tasks) return Collections.emptyList();

        List<TimerTask> copy = new ArrayList<TimerTask>();
        for (TimerTask task : tasks) {
            if (!(task instanceof TtlTimerTask)) copy.add(task);
            else copy.add(((TtlTimerTask) task).getTimerTask());
        }
        return copy;
    }
}
