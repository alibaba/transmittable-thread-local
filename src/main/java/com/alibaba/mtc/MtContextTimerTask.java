package com.alibaba.mtc;

import java.util.Map;
import java.util.TimerTask;

/**
 * {@link MtContextTimerTask} decorate {@link TimerTask}, so as to get @{@link MtContext}
 * and transmit it to the time of {@link Runnable} execution, needed when use {@link Runnable} to thread pool.
 * <p/>
 * Use factory method {@link #get(TimerTask)} to create instance.
 * <p/>
 * Use {@link java.util.concurrent.ScheduledThreadPoolExecutor} instead {@link java.util.Timer}.
 * <p/>
 * <b>NOTE:</b>
 * The {@link MtContextTimerTask} make the the method {@link TimerTask#scheduledExecutionTime()} of
 * the origin {@link TimerTask} lose effectiveness!
 *
 * @author ding.lid
 * @see java.util.Timer
 * @see TimerTask
 * @since 0.9.1
 */
@Deprecated
public final class MtContextTimerTask extends TimerTask {
    private final Map<String, Object> content;
    private final TimerTask timerTask;

    private MtContextTimerTask(TimerTask timerTask) {
        content = MtContext.getContext().getWithCopy();
        this.timerTask = timerTask;
    }

    /**
     * wrap method {@link TimerTask#run()}.
     */
    @Override
    public void run() {
        MtContext mtContext = MtContext.getContext();
        final Map<String, Object> old = mtContext.get0();
        try {
            mtContext.set0(content);
            timerTask.run();
        } finally {
            mtContext.set0(old); // restore MtContext
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
            throw new NullPointerException("runnable argument is null!");
        }

        if (timerTask instanceof MtContextTimerTask) { // avoid redundant decoration, and ensure idempotency
            return (MtContextTimerTask) timerTask;
        }
        return new MtContextTimerTask(timerTask);
    }
}
