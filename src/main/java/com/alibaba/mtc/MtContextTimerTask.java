package com.alibaba.mtc;

import java.util.Map;
import java.util.TimerTask;

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
    private final Map<MtContextThreadLocal<?>, Object> copied;
    private final TimerTask timerTask;

    private MtContextTimerTask(TimerTask timerTask) {
        copied = MtContextThreadLocal.copy();
        this.timerTask = timerTask;
    }

    /**
     * wrap method {@link TimerTask#run()}.
     */
    @Override
    public void run() {
        // backup MtContext
        Map<MtContextThreadLocal<?>, Object> backup = MtContextThreadLocal.backupAndSet(copied);
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
        if (null == timerTask) {
            return null;
        }

        if (timerTask instanceof MtContextTimerTask) { // avoid redundant decoration, and ensure idempotency
            return (MtContextTimerTask) timerTask;
        }
        return new MtContextTimerTask(timerTask);
    }
}
