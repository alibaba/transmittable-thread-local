package java.util.concurrent;

import com.alibaba.ttl.spi.TtlEnhanced;
import com.alibaba.ttl.spi.TtlWrapper;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

import static com.alibaba.ttl.TransmittableThreadLocal.Transmitter.*;

public class TtlForkJoinTask<V> extends ForkJoinTask<V> implements TtlWrapper<ForkJoinTask<V>>, TtlEnhanced {

    private final AtomicReference<Object> capturedRef;
    private final ForkJoinTask<V> delegateTask;
    private final boolean releaseTtlValueReferenceAfterRun;

    public TtlForkJoinTask(ForkJoinTask<V> forkJoinTask, boolean releaseTtlValueReferenceAfterRun) {
        this.capturedRef = new AtomicReference<Object>(capture());
        this.delegateTask = forkJoinTask;
        this.releaseTtlValueReferenceAfterRun = releaseTtlValueReferenceAfterRun;
    }

    @NonNull
    @Override
    public ForkJoinTask<V> unwrap() {
        return delegateTask;
    }

    @Override
    public V getRawResult() {
        return delegateTask.getRawResult();
    }

    @Override
    protected void setRawResult(V value) {
        delegateTask.setRawResult(value);
    }

    @Override
    protected boolean exec() {
        final Object captured = capturedRef.get();
        if (captured == null || releaseTtlValueReferenceAfterRun && !capturedRef.compareAndSet(captured, null)) {
            throw new IllegalStateException("TTL value reference is released after call!");
        }

        final Object backup = replay(captured);
        try {
            // 执行delegate的exec过程，经过整个完整的状态机的过程
            delegateTask.doExec();
            return true;
        } finally {
            restore(backup);
        }
    }

    /**
     * Factory method, wrap input {@link ForkJoinTask} to {@link TtlForkJoinTask}.
     *
     * @param forkJoinTask input {@link ForkJoinTask}. if input is {@code null}, return {@code null}.
     * @return Wrapped {@link Runnable}
     * @throws IllegalStateException when input is {@link TtlForkJoinTask} already.
     */
    @Nullable
    public static <V> TtlForkJoinTask<V> get(@Nullable ForkJoinTask<V> forkJoinTask) {
        return get(forkJoinTask, false, false);
    }

    /**
     * Factory method, wrap input {@link ForkJoinTask} to {@link TtlForkJoinTask}.
     *
     * @param forkJoinTask                     input {@link ForkJoinTask}. if input is {@code null}, return {@code null}.
     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlForkJoinTask} is referred.
     * @return Wrapped {@link ForkJoinTask}
     * @throws IllegalStateException when input is {@link TtlForkJoinTask} already.
     */
    @Nullable
    public static <V> TtlForkJoinTask<V> get(@Nullable ForkJoinTask<V> forkJoinTask, boolean releaseTtlValueReferenceAfterRun) {
        return get(forkJoinTask, releaseTtlValueReferenceAfterRun, false);
    }

    /**
     * Factory method, wrap input {@link ForkJoinTask} to {@link TtlForkJoinTask}.
     *
     * @param forkJoinTask                     input {@link ForkJoinTask}. if input is {@code null}, return {@code null}.
     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlForkJoinTask} is referred.
     * @param idempotent                       is idempotent mode or not. if {@code true}, just return input {@link ForkJoinTask} when it's {@link TtlForkJoinTask},
     *                                         otherwise throw {@link IllegalStateException}.
     *                                         <B><I>Caution</I></B>: {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
     * @return Wrapped {@link ForkJoinTask}
     * @throws IllegalStateException when input is {@link TtlForkJoinTask} already and not idempotent.
     */
    @Nullable
    public static <V> TtlForkJoinTask<V> get(@Nullable ForkJoinTask<V> forkJoinTask, boolean releaseTtlValueReferenceAfterRun, boolean idempotent) {
        if (null == forkJoinTask) return null;

        if (forkJoinTask instanceof TtlEnhanced) {
            // avoid redundant decoration, and ensure idempotency
            if (idempotent) return (TtlForkJoinTask) forkJoinTask;
            else throw new IllegalStateException("Already ForkJoinTask!");
        }
        return new TtlForkJoinTask<V>(forkJoinTask, releaseTtlValueReferenceAfterRun);
    }
}
