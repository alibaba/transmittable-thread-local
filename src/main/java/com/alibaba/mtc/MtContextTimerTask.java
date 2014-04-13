package com.alibaba.mtc;

import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link MtContextTimerTask} decorate {@link TimerTask}, so as to get {@link MtContextThreadLocal}
 * and transmit it to the time of {@link MtContextTimerTask} execution, needed when use {@link MtContextTimerTask} to {@link java.util.TimerTask}.
 * <p/>
 * Use factory method {@link #get(TimerTask)} to create instance.
 * <p/>
 * <b>NOTE:</b>
 * The {@link MtContextTimerTask} make the the method {@link TimerTask#scheduledExecutionTime()} in
 * the origin {@link TimerTask} lose effectiveness!
 *
 * @author ding.lid
 * @see java.util.Timer
 * @see TimerTask
 * @since 0.9.1
 * @deprecated Use {@link MtContextRunnable}, {@link java.util.concurrent.ScheduledExecutorService} instead of {@link java.util.Timer}, {@link java.util.TimerTask}.
 */
@Deprecated
public final class MtContextTimerTask extends TimerTask {
    private final AtomicReference<Map<MtContextThreadLocal<?>, Object>> copiedRef;
    private final TimerTask timerTask;
    private final boolean releaseMtContextAfterRun;

    private MtContextTimerTask(TimerTask timerTask, boolean releaseMtContextAfterRun) {
        this.copiedRef = new AtomicReference<Map<MtContextThreadLocal<?>, Object>>(MtContextThreadLocal.copy());
        this.timerTask = timerTask;
        this.releaseMtContextAfterRun = releaseMtContextAfterRun;
    }

    /**
     * wrap method {@link TimerTask#run()}.
     */
    @Override
    public void run() {
        // backup MtContext
        Map<MtContextThreadLocal<?>, Object> copied = copiedRef.get();
        Map<MtContextThreadLocal<?>, Object> backup = MtContextThreadLocal.backupAndSet(copied);
        if (copied == null || releaseMtContextAfterRun && !copiedRef.compareAndSet(copied, null)) {
            throw new IllegalStateException("MtContext is released!");
        }
        try {
            timerTask.run();
        } finally {
            MtContextThreadLocal.restore(backup);
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
     * Factory method, wrapper input {@link Runnable} to {@link MtContextTimerTask}.
     * <p/>
     * This method is idempotent.
     *
     * @param timerTask input {@link TimerTask}
     * @return Wrapped {@link TimerTask}
     */
    public static MtContextTimerTask get(TimerTask timerTask) {
        return get(timerTask, false, false);
    }

    /**
     * Factory method, wrapper input {@link Runnable} to {@link MtContextTimerTask}.
     * <p/>
     * This method is idempotent.
     *
     * @param timerTask                input {@link TimerTask}
     * @param releaseMtContextAfterRun release MtContext after run, avoid memory leak even if {@link MtContextRunnable} is referred.
     * @return Wrapped {@link TimerTask}
     */
    public static MtContextTimerTask get(TimerTask timerTask, boolean releaseMtContextAfterRun) {
        return get(timerTask, releaseMtContextAfterRun, false);
    }

    /**
     * Factory method, wrapper input {@link Runnable} to {@link MtContextTimerTask}.
     * <p/>
     * This method is idempotent.
     *
     * @param timerTask                input {@link TimerTask}
     * @param releaseMtContextAfterRun release MtContext after run, avoid memory leak even if {@link MtContextRunnable} is referred.
     * @param idempotent               is idempotent or not. {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
     * @return Wrapped {@link TimerTask}
     */
    public static MtContextTimerTask get(TimerTask timerTask, boolean releaseMtContextAfterRun, boolean idempotent) {
        if (null == timerTask) {
            return null;
        }

        if (timerTask instanceof MtContextTimerTask) {
            if (idempotent) {
                // avoid redundant decoration, and ensure idempotency
                return (MtContextTimerTask) timerTask;
            } else {
                throw new IllegalStateException("Already MtContextTimerTask!");
            }
        }
        return new MtContextTimerTask(timerTask, false);
    }
}
