package com.alibaba.mtc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link MtContextRunnable} decorate {@link Runnable}, so as to get @{@link MtContext}
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
    private final Map<MtContextThreadLocal<?>, Object> copied;
    private final Runnable runnable;

    private MtContextRunnable(Runnable runnable) {
        Map<MtContextThreadLocal<?>, Object> map = new HashMap<MtContextThreadLocal<?>, Object>(MtContextThreadLocal.holder.size());
        for (Map.Entry<MtContextThreadLocal<?>, Object> entry : MtContextThreadLocal.holder.entrySet()) {
            MtContextThreadLocal<?> threadLocal = entry.getKey();
            map.put(threadLocal, threadLocal.copiedMtContextValue());
        }
        copied = map;

        this.runnable = runnable;
    }

    /**
     * wrap method {@link Runnable#run()}.
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
            runnable.run();
        } finally {
            // restore MtContext
            for (Map.Entry<MtContextThreadLocal<?>, Object> entry : map.entrySet()) {
                @SuppressWarnings("unchecked")
                MtContextThreadLocal<Object> threadLocal = (MtContextThreadLocal<Object>) entry.getKey();
                threadLocal.set(entry.getValue());
            }
        }
        // FIXME add option so as to release copied after run 
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
        if (null == runnable) {
            throw new NullPointerException("runnable argument is null!");
        }

        if (runnable instanceof MtContextRunnable) { // avoid redundant decoration, and ensure idempotency
            return (MtContextRunnable) runnable;
        }
        return new MtContextRunnable(runnable);
    }

    /**
     * wrapper input {@link Runnable} Collection to {@link MtContextRunnable} Collection.
     */
    public static List<MtContextRunnable> gets(Collection<? extends Runnable> tasks) {
        if (null == tasks) {
            return null;
        }
        List<MtContextRunnable> copy = new ArrayList<MtContextRunnable>();
        for (Runnable task : tasks) {
            copy.add(MtContextRunnable.get(task));
        }
        return copy;
    }
}
