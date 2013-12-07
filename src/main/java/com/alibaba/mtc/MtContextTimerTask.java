package com.alibaba.mtc;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

/**
 * {@link MtContextTimerTask} decorate {@link TimerTask}, so as to get @{@link MtContextThreadLocal}
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
 * @deprecated Use {@link MtContextRunnable} instead
 */
@Deprecated
public final class MtContextTimerTask extends TimerTask {
    private final Map<MtContextThreadLocal<?>, Object> copied;
    private final TimerTask timerTask;

    private MtContextTimerTask(TimerTask timerTask) {
        Map<MtContextThreadLocal<?>, Object> map = new HashMap<MtContextThreadLocal<?>, Object>(MtContextThreadLocal.holder.size());
        for (Map.Entry<MtContextThreadLocal<?>, Object> entry : MtContextThreadLocal.holder.entrySet()) {
            MtContextThreadLocal<?> threadLocal = entry.getKey();
            map.put(threadLocal, threadLocal.copiedMtContextValue());
        }
        copied = map;
        this.timerTask = timerTask;
    }

    /**
     * wrap method {@link TimerTask#run()}.
     */
    @Override
    public void run() {
        // backup MtContext
        Map<MtContextThreadLocal<?>, Object> map = new HashMap<MtContextThreadLocal<?>, Object>(MtContextThreadLocal.holder.size());
        for (Map.Entry<MtContextThreadLocal<?>, Object> entry : copied.entrySet()) {
            @SuppressWarnings("unchecked")
            MtContextThreadLocal<Object> threadLocal = (MtContextThreadLocal<Object>) entry.getKey();
            map.put(threadLocal, threadLocal.get());
            threadLocal.set(entry.getValue());
        }
        try {
            timerTask.run();
        } finally {
            // restore MtContext
            for (Map.Entry<MtContextThreadLocal<?>, Object> entry : map.entrySet()) {
                @SuppressWarnings("unchecked")
                MtContextThreadLocal<Object> threadLocal = (MtContextThreadLocal<Object>) entry.getKey();
                threadLocal.set(entry.getValue());
            }
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
