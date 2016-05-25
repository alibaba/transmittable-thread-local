package com.alibaba.ttl;

import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

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
 * @since 0.9.1
 * @deprecated Use {@link TtlRunnable}, {@link java.util.concurrent.ScheduledExecutorService} instead of {@link java.util.Timer}, {@link java.util.TimerTask}.
 */
@Deprecated
public final class TtlTimerTask extends TimerTask {
    private final AtomicReference<Map<TransmittableThreadLocal<?>, Object>> copiedRef;
    private final TimerTask timerTask;
    private final boolean releaseTtlValueReferenceAfterRun;

    private TtlTimerTask(TimerTask timerTask, boolean releaseTtlValueReferenceAfterRun) {
        this.copiedRef = new AtomicReference<Map<TransmittableThreadLocal<?>, Object>>(TransmittableThreadLocal.copy());
        this.timerTask = timerTask;
        this.releaseTtlValueReferenceAfterRun = releaseTtlValueReferenceAfterRun;
    }

    /**
     * wrap method {@link TimerTask#run()}.
     */
    @Override
    public void run() {
        Map<TransmittableThreadLocal<?>, Object> copied = copiedRef.get();
        if (copied == null || releaseTtlValueReferenceAfterRun && !copiedRef.compareAndSet(copied, null)) {
            throw new IllegalStateException("TTL value reference is released after run!");
        }

        Map<TransmittableThreadLocal<?>, Object> backup = TransmittableThreadLocal.backupAndSetToCopied(copied);
        try {
            timerTask.run();
        } finally {
            TransmittableThreadLocal.restoreBackup(backup);
        }
    }

    @Override
    public boolean cancel() {
        timerTask.cancel();
        return super.cancel();
    }

    public TimerTask getTimerTask() {
        return timerTask;
    }

    /**
     * Factory method, wrapper input {@link Runnable} to {@link TtlTimerTask}.
     * <p>
     * This method is idempotent.
     *
     * @param timerTask input {@link TimerTask}
     * @return Wrapped {@link TimerTask}
     */
    public static TtlTimerTask get(TimerTask timerTask) {
        return get(timerTask, false, false);
    }

    /**
     * Factory method, wrapper input {@link Runnable} to {@link TtlTimerTask}.
     * <p>
     * This method is idempotent.
     *
     * @param timerTask                        input {@link TimerTask}
     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlRunnable} is referred.
     * @return Wrapped {@link TimerTask}
     */
    public static TtlTimerTask get(TimerTask timerTask, boolean releaseTtlValueReferenceAfterRun) {
        return get(timerTask, releaseTtlValueReferenceAfterRun, false);
    }

    /**
     * Factory method, wrapper input {@link Runnable} to {@link TtlTimerTask}.
     * <p>
     * This method is idempotent.
     *
     * @param timerTask                        input {@link TimerTask}
     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlRunnable} is referred.
     * @param idempotent                       is idempotent or not. {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
     * @return Wrapped {@link TimerTask}
     */
    public static TtlTimerTask get(TimerTask timerTask, boolean releaseTtlValueReferenceAfterRun, boolean idempotent) {
        if (null == timerTask) {
            return null;
        }

        if (timerTask instanceof TtlTimerTask) {
            if (idempotent) {
                // avoid redundant decoration, and ensure idempotency
                return (TtlTimerTask) timerTask;
            } else {
                throw new IllegalStateException("Already TtlTimerTask!");
            }
        }
        return new TtlTimerTask(timerTask, false);
    }
}
