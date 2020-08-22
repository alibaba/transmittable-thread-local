package com.alibaba.ttl;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link TransmittableThreadLocal} can transmit value from the thread of submitting task to the thread of executing task.
 * <p>
 * <b>Note</b>:<br>
 * {@link TransmittableThreadLocal} extends {@link InheritableThreadLocal},
 * so {@link TransmittableThreadLocal} first is a {@link InheritableThreadLocal}.<br>
 * If the <b>inheritable</b> ability from {@link InheritableThreadLocal} has <b>potential leaking problem</b>,
 * you can disable the <b>inheritable</b> ability:
 * <p>
 * ❶ For thread pooling components({@link java.util.concurrent.ThreadPoolExecutor},
 * {@link java.util.concurrent.ForkJoinPool}), Inheritable feature <b>should never</b> happen,
 * since threads in thread pooling components is pre-created and pooled, these threads is <b>neutral</b> to biz logic/data.
 * <br>
 * Disable inheritable for thread pooling components by wrapping thread factories using method
 * {@link com.alibaba.ttl.threadpool.TtlExecutors#getDisableInheritableThreadFactory(java.util.concurrent.ThreadFactory)} /
 * {@link com.alibaba.ttl.threadpool.TtlForkJoinPoolHelper#getDefaultDisableInheritableForkJoinWorkerThreadFactory()}.
 * <br>
 * Or you can turn on "disable inheritable for thread pool" by {@link com.alibaba.ttl.threadpool.agent.TtlAgent}
 * so as to wrap thread factories for thread pooling components automatically and transparently.
 * <p>
 * ❷ In other cases, disable inheritable by overriding method {@link #childValue(Object)}.
 * <br>
 * Whether the value should be inheritable or not can be controlled by the data owner,
 * disable it <b>carefully</b> when data owner have a clear idea.
 * <pre>{@code
 * TransmittableThreadLocal<String> transmittableThreadLocal = new TransmittableThreadLocal<>() {
 *     protected String childValue(String parentValue) {
 *         return initialValue();
 *     }
 * }}</pre>
 * <p>
 * More discussion about "disable the <b>inheritable</b> ability"
 * see <a href="https://github.com/alibaba/transmittable-thread-local/issues/100">
 * issue #100: disable Inheritable when it's not necessary and buggy</a>.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @author Yang Fang (snoop dot fy at gmail dot com)
 * @see TtlRunnable
 * @see TtlCallable
 * @see com.alibaba.ttl.threadpool.TtlExecutors
 * @see com.alibaba.ttl.threadpool.TtlExecutors#getTtlExecutor(java.util.concurrent.Executor)
 * @see com.alibaba.ttl.threadpool.TtlExecutors#getTtlExecutorService(java.util.concurrent.ExecutorService)
 * @see com.alibaba.ttl.threadpool.TtlExecutors#getTtlScheduledExecutorService(java.util.concurrent.ScheduledExecutorService)
 * @see com.alibaba.ttl.threadpool.TtlExecutors#getDefaultDisableInheritableThreadFactory()
 * @see com.alibaba.ttl.threadpool.TtlExecutors#getDisableInheritableThreadFactory(java.util.concurrent.ThreadFactory)
 * @see com.alibaba.ttl.threadpool.TtlForkJoinPoolHelper
 * @see com.alibaba.ttl.threadpool.TtlForkJoinPoolHelper#getDefaultDisableInheritableForkJoinWorkerThreadFactory()
 * @see com.alibaba.ttl.threadpool.TtlForkJoinPoolHelper#getDisableInheritableForkJoinWorkerThreadFactory(java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory)
 * @see com.alibaba.ttl.threadpool.agent.TtlAgent
 * @since 0.10.0
 */
public class TransmittableThreadLocal<T> extends InheritableThreadLocal<T> implements TtlCopier<T> {
    private static final Logger logger = Logger.getLogger(TransmittableThreadLocal.class.getName());

    private final boolean disableIgnoreNullValueSemantics;

    /**
     * Default constructor. Create a {@link TransmittableThreadLocal} instance with "Ignore-Null-Value Semantics".
     * <p>
     * About "Ignore-Null-Value Semantics":
     * <p>
     * <ol>
     *     <li>If value is {@code null}(check by {@link #get()} method), do NOT transmit this {@code ThreadLocal}.</li>
     *     <li>If set {@code null} value, also remove value(invoke {@link #remove()} method).</li>
     * </ol>
     * <p>
     * This is a pragmatic design decision:
     * <ol>
     * <li>use explicit value type rather than {@code null} value to express biz intent.</li>
     * <li>safer and more robust code(avoid {@code NPE} risk).</li>
     * </ol>
     * <p>
     * So it's strongly not recommended to use {@code null} value.
     * <p>
     * But the behavior of "Ignore-Null-Value Semantics" is NOT compatible with
     * {@link ThreadLocal} and {@link InheritableThreadLocal},
     * you can disable this behavior/semantics via using constructor {@link #TransmittableThreadLocal(boolean)}
     * and setting parameter {@code disableIgnoreNullValueSemantics} to {@code true}.
     * <p>
     * More discussion about "Ignore-Null-Value Semantics" see
     * <a href="https://github.com/alibaba/transmittable-thread-local/issues/157">Issue #157</a>.
     *
     * @see #TransmittableThreadLocal(boolean)
     */
    public TransmittableThreadLocal() {
        this(false);
    }

    /**
     * Constructor, create a {@link TransmittableThreadLocal} instance
     * with parameter {@code disableIgnoreNullValueSemantics} to control "Ignore-Null-Value Semantics".
     *
     * @param disableIgnoreNullValueSemantics disable "Ignore-Null-Value Semantics"
     * @see #TransmittableThreadLocal()
     * @since 2.11.3
     */
    public TransmittableThreadLocal(boolean disableIgnoreNullValueSemantics) {
        this.disableIgnoreNullValueSemantics = disableIgnoreNullValueSemantics;
    }

    /**
     * Computes the value for this transmittable thread-local variable
     * as a function of the source thread's value at the time the task
     * Object is created.
     * <p>
     * This method is called from {@link TtlRunnable} or
     * {@link TtlCallable} when it create, before the task is started.
     * <p>
     * This method merely returns reference of its source thread value(the shadow copy),
     * and should be overridden if a different behavior is desired.
     *
     * @since 1.0.0
     */
    public T copy(T parentValue) {
        return parentValue;
    }

    /**
     * Callback method before task object({@link TtlRunnable}/{@link TtlCallable}) execute.
     * <p>
     * Default behavior is to do nothing, and should be overridden
     * if a different behavior is desired.
     * <p>
     * Do not throw any exception, just ignored.
     *
     * @since 1.2.0
     */
    protected void beforeExecute() {
    }

    /**
     * Callback method after task object({@link TtlRunnable}/{@link TtlCallable}) execute.
     * <p>
     * Default behavior is to do nothing, and should be overridden
     * if a different behavior is desired.
     * <p>
     * Do not throw any exception, just ignored.
     *
     * @since 1.2.0
     */
    protected void afterExecute() {
    }

    /**
     * see {@link InheritableThreadLocal#get()}
     */
    @Override
    public final T get() {
        T value = super.get();
        if (disableIgnoreNullValueSemantics || null != value) addThisToHolder();
        return value;
    }

    /**
     * see {@link InheritableThreadLocal#set}
     */
    @Override
    public final void set(T value) {
        if (!disableIgnoreNullValueSemantics && null == value) {
            // may set null to remove value
            remove();
        } else {
            super.set(value);
            addThisToHolder();
        }
    }

    /**
     * see {@link InheritableThreadLocal#remove()}
     */
    @Override
    public final void remove() {
        removeThisFromHolder();
        super.remove();
    }

    private void superRemove() {
        super.remove();
    }

    private T copyValue() {
        return copy(get());
    }

    // Note about the holder:
    // 1. holder self is a InheritableThreadLocal(a *ThreadLocal*).
    // 2. The type of value in the holder is WeakHashMap<TransmittableThreadLocal<Object>, ?>.
    //    2.1 but the WeakHashMap is used as a *Set*:
    //        the value of WeakHashMap is *always* null, and never used.
    //    2.2 WeakHashMap support *null* value.
    private static final InheritableThreadLocal<WeakHashMap<TransmittableThreadLocal<Object>, ?>> holder =
            new InheritableThreadLocal<WeakHashMap<TransmittableThreadLocal<Object>, ?>>() {
                @Override
                protected WeakHashMap<TransmittableThreadLocal<Object>, ?> initialValue() {
                    return new WeakHashMap<TransmittableThreadLocal<Object>, Object>();
                }

                @Override
                protected WeakHashMap<TransmittableThreadLocal<Object>, ?> childValue(WeakHashMap<TransmittableThreadLocal<Object>, ?> parentValue) {
                    return new WeakHashMap<TransmittableThreadLocal<Object>, Object>(parentValue);
                }
            };

    @SuppressWarnings("unchecked")
    private void addThisToHolder() {
        if (!holder.get().containsKey(this)) {
            holder.get().put((TransmittableThreadLocal<Object>) this, null); // WeakHashMap supports null value.
        }
    }

    private void removeThisFromHolder() {
        holder.get().remove(this);
    }

    private static void doExecuteCallback(boolean isBefore) {
        for (TransmittableThreadLocal<Object> threadLocal : holder.get().keySet()) {
            try {
                if (isBefore) threadLocal.beforeExecute();
                else threadLocal.afterExecute();
            } catch (Throwable t) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "TTL exception when " + (isBefore ? "beforeExecute" : "afterExecute") + ", cause: " + t.toString(), t);
                }
            }
        }
    }

    /**
     * Debug only method!
     */
    static void dump(@Nullable String title) {
        if (title != null && title.length() > 0) {
            System.out.printf("Start TransmittableThreadLocal[%s] Dump...%n", title);
        } else {
            System.out.println("Start TransmittableThreadLocal Dump...");
        }

        for (TransmittableThreadLocal<Object> threadLocal : holder.get().keySet()) {
            System.out.println(threadLocal.get());
        }
        System.out.println("TransmittableThreadLocal Dump end!");
    }

    /**
     * Debug only method!
     */
    static void dump() {
        dump(null);
    }

    /**
     * {@link Transmitter} transmit all {@link TransmittableThreadLocal}
     * and registered {@link ThreadLocal}(registered by {@link Transmitter#registerThreadLocal})
     * values of the current thread to other thread by static methods
     * {@link #capture()} =&gt; {@link #replay(Object)} =&gt; {@link #restore(Object)} (aka {@code CRR} operation).
     * <p>
     * {@link Transmitter} is <b><i>internal</i></b> manipulation api for <b><i>framework/middleware integration</i></b>;
     * In general, you will <b><i>never</i></b> use it in the <i>biz/application code</i>!
     *
     * <h2>Framework/Middleware integration to TTL transmittance</h2>
     * Below is the example code:
     *
     * <pre>{@code
     * ///////////////////////////////////////////////////////////////////////////
     * // in thread A, capture all TransmittableThreadLocal values of thread A
     * ///////////////////////////////////////////////////////////////////////////
     *
     * Object captured = Transmitter.capture(); // (1)
     *
     * ///////////////////////////////////////////////////////////////////////////
     * // in thread B
     * ///////////////////////////////////////////////////////////////////////////
     *
     * // replay all TransmittableThreadLocal values from thread A
     * Object backup = Transmitter.replay(captured); // (2)
     * try {
     *     // your biz logic, run with the TransmittableThreadLocal values of thread B
     *     System.out.println("Hello");
     *     // ...
     *     return "World";
     * } finally {
     *     // restore the TransmittableThreadLocal of thread B when replay
     *     Transmitter.restore(backup); (3)
     * }}</pre>
     * <p>
     * see the implementation code of {@link TtlRunnable} and {@link TtlCallable} for more actual code sample.
     * <p>
     * Of course, {@link #replay(Object)} and {@link #restore(Object)} operation can be simplified by util methods
     * {@link #runCallableWithCaptured(Object, Callable)} or {@link #runSupplierWithCaptured(Object, Supplier)}
     * and the adorable {@code Java 8 lambda syntax}.
     * <p>
     * Below is the example code:
     *
     * <pre>{@code
     * ///////////////////////////////////////////////////////////////////////////
     * // in thread A, capture all TransmittableThreadLocal values of thread A
     * ///////////////////////////////////////////////////////////////////////////
     *
     * Object captured = Transmitter.capture(); // (1)
     *
     * ///////////////////////////////////////////////////////////////////////////
     * // in thread B
     * ///////////////////////////////////////////////////////////////////////////
     *
     * String result = runSupplierWithCaptured(captured, () -&gt; {
     *      // your biz logic, run with the TransmittableThreadLocal values of thread A
     *      System.out.println("Hello");
     *      ...
     *      return "World";
     * }); // (2) + (3)}</pre>
     * <p>
     * The reason of providing 2 util methods is the different {@code throws Exception} type
     * so as to satisfy your biz logic({@code lambda}):
     * <ol>
     * <li>{@link #runCallableWithCaptured(Object, Callable)}: {@code throws Exception}</li>
     * <li>{@link #runSupplierWithCaptured(Object, Supplier)}: No {@code throws}</li>
     * </ol>
     * <p>
     * If you need the different {@code throws Exception} type,
     * you can define your own util method(function interface({@code lambda}))
     * with your own {@code throws Exception} type.
     *
     * <h2>ThreadLocal Integration</h2>
     * If you can not rewrite the existed code which use {@link ThreadLocal} to {@link TransmittableThreadLocal},
     * register the {@link ThreadLocal} instances via the methods
     * {@link #registerThreadLocal(ThreadLocal, TtlCopier)}/{@link #registerThreadLocalWithShadowCopier(ThreadLocal)}
     * to enhance the <b>Transmittable</b> ability for the existed {@link ThreadLocal} instances.
     * <p>
     * Below is the example code:
     *
     * <pre>{@code
     * // the value of this ThreadLocal instance will be transmitted after registered
     * Transmitter.registerThreadLocal(aThreadLocal, copyLambda);
     *
     * // Then the value of this ThreadLocal instance will not be transmitted after unregistered
     * Transmitter.unregisterThreadLocal(aThreadLocal);}</pre>
     *
     * <B><I>Caution:</I></B><br>
     * If the registered {@link ThreadLocal} instance is not {@link InheritableThreadLocal},
     * the instance can NOT <B><I>{@code inherit}</I></B> value from parent thread(aka. the <b>inheritable</b> ability)!
     *
     * @author Yang Fang (snoop dot fy at gmail dot com)
     * @author Jerry Lee (oldratlee at gmail dot com)
     * @see TtlRunnable
     * @see TtlCallable
     * @since 2.3.0
     */
    public static class Transmitter {
        /**
         * Capture all {@link TransmittableThreadLocal} and registered {@link ThreadLocal} values in the current thread.
         *
         * @return the captured {@link TransmittableThreadLocal} values
         * @since 2.3.0
         */
        @NonNull
        public static Object capture() {
            return new Snapshot(captureTtlValues(), captureThreadLocalValues());
        }

        private static HashMap<TransmittableThreadLocal<Object>, Object> captureTtlValues() {
            HashMap<TransmittableThreadLocal<Object>, Object> ttl2Value = new HashMap<TransmittableThreadLocal<Object>, Object>();
            for (TransmittableThreadLocal<Object> threadLocal : holder.get().keySet()) {
                ttl2Value.put(threadLocal, threadLocal.copyValue());
            }
            return ttl2Value;
        }

        private static HashMap<ThreadLocal<Object>, Object> captureThreadLocalValues() {
            final HashMap<ThreadLocal<Object>, Object> threadLocal2Value = new HashMap<ThreadLocal<Object>, Object>();
            for (Map.Entry<ThreadLocal<Object>, TtlCopier<Object>> entry : threadLocalHolder.entrySet()) {
                final ThreadLocal<Object> threadLocal = entry.getKey();
                final TtlCopier<Object> copier = entry.getValue();

                threadLocal2Value.put(threadLocal, copier.copy(threadLocal.get()));
            }
            return threadLocal2Value;
        }

        /**
         * Replay the captured {@link TransmittableThreadLocal} and registered {@link ThreadLocal} values from {@link #capture()},
         * and return the backup {@link TransmittableThreadLocal} values in the current thread before replay.
         *
         * @param captured captured {@link TransmittableThreadLocal} values from other thread from {@link #capture()}
         * @return the backup {@link TransmittableThreadLocal} values before replay
         * @see #capture()
         * @since 2.3.0
         */
        @NonNull
        public static Object replay(@NonNull Object captured) {
            final Snapshot capturedSnapshot = (Snapshot) captured;
            return new Snapshot(replayTtlValues(capturedSnapshot.ttl2Value), replayThreadLocalValues(capturedSnapshot.threadLocal2Value));
        }

        @NonNull
        private static HashMap<TransmittableThreadLocal<Object>, Object> replayTtlValues(@NonNull HashMap<TransmittableThreadLocal<Object>, Object> captured) {
            HashMap<TransmittableThreadLocal<Object>, Object> backup = new HashMap<TransmittableThreadLocal<Object>, Object>();

            for (final Iterator<TransmittableThreadLocal<Object>> iterator = holder.get().keySet().iterator(); iterator.hasNext(); ) {
                TransmittableThreadLocal<Object> threadLocal = iterator.next();

                // backup
                backup.put(threadLocal, threadLocal.get());

                // clear the TTL values that is not in captured
                // avoid the extra TTL values after replay when run task
                if (!captured.containsKey(threadLocal)) {
                    iterator.remove();
                    threadLocal.superRemove();
                }
            }

            // set TTL values to captured
            setTtlValuesTo(captured);

            // call beforeExecute callback
            doExecuteCallback(true);

            return backup;
        }

        private static HashMap<ThreadLocal<Object>, Object> replayThreadLocalValues(@NonNull HashMap<ThreadLocal<Object>, Object> captured) {
            final HashMap<ThreadLocal<Object>, Object> backup = new HashMap<ThreadLocal<Object>, Object>();

            for (Map.Entry<ThreadLocal<Object>, Object> entry : captured.entrySet()) {
                final ThreadLocal<Object> threadLocal = entry.getKey();
                backup.put(threadLocal, threadLocal.get());

                final Object value = entry.getValue();
                if (value == threadLocalClearMark) threadLocal.remove();
                else threadLocal.set(value);
            }

            return backup;
        }

        /**
         * Clear all {@link TransmittableThreadLocal} and registered {@link ThreadLocal} values in the current thread,
         * and return the backup {@link TransmittableThreadLocal} values in the current thread before clear.
         *
         * @return the backup {@link TransmittableThreadLocal} values before clear
         * @since 2.9.0
         */
        @NonNull
        public static Object clear() {
            final HashMap<TransmittableThreadLocal<Object>, Object> ttl2Value = new HashMap<TransmittableThreadLocal<Object>, Object>();

            final HashMap<ThreadLocal<Object>, Object> threadLocal2Value = new HashMap<ThreadLocal<Object>, Object>();
            for (Map.Entry<ThreadLocal<Object>, TtlCopier<Object>> entry : threadLocalHolder.entrySet()) {
                final ThreadLocal<Object> threadLocal = entry.getKey();
                threadLocal2Value.put(threadLocal, threadLocalClearMark);
            }

            return replay(new Snapshot(ttl2Value, threadLocal2Value));
        }

        /**
         * Restore the backup {@link TransmittableThreadLocal} and
         * registered {@link ThreadLocal} values from {@link #replay(Object)}/{@link #clear()}.
         *
         * @param backup the backup {@link TransmittableThreadLocal} values from {@link #replay(Object)}/{@link #clear()}
         * @see #replay(Object)
         * @see #clear()
         * @since 2.3.0
         */
        public static void restore(@NonNull Object backup) {
            final Snapshot backupSnapshot = (Snapshot) backup;
            restoreTtlValues(backupSnapshot.ttl2Value);
            restoreThreadLocalValues(backupSnapshot.threadLocal2Value);
        }

        private static void restoreTtlValues(@NonNull HashMap<TransmittableThreadLocal<Object>, Object> backup) {
            // call afterExecute callback
            doExecuteCallback(false);

            for (final Iterator<TransmittableThreadLocal<Object>> iterator = holder.get().keySet().iterator(); iterator.hasNext(); ) {
                TransmittableThreadLocal<Object> threadLocal = iterator.next();

                // clear the TTL values that is not in backup
                // avoid the extra TTL values after restore
                if (!backup.containsKey(threadLocal)) {
                    iterator.remove();
                    threadLocal.superRemove();
                }
            }

            // restore TTL values
            setTtlValuesTo(backup);
        }

        private static void setTtlValuesTo(@NonNull HashMap<TransmittableThreadLocal<Object>, Object> ttlValues) {
            for (Map.Entry<TransmittableThreadLocal<Object>, Object> entry : ttlValues.entrySet()) {
                TransmittableThreadLocal<Object> threadLocal = entry.getKey();
                threadLocal.set(entry.getValue());
            }
        }

        private static void restoreThreadLocalValues(@NonNull HashMap<ThreadLocal<Object>, Object> backup) {
            for (Map.Entry<ThreadLocal<Object>, Object> entry : backup.entrySet()) {
                final ThreadLocal<Object> threadLocal = entry.getKey();
                threadLocal.set(entry.getValue());
            }
        }

        private static class Snapshot {
            final HashMap<TransmittableThreadLocal<Object>, Object> ttl2Value;
            final HashMap<ThreadLocal<Object>, Object> threadLocal2Value;

            private Snapshot(HashMap<TransmittableThreadLocal<Object>, Object> ttl2Value, HashMap<ThreadLocal<Object>, Object> threadLocal2Value) {
                this.ttl2Value = ttl2Value;
                this.threadLocal2Value = threadLocal2Value;
            }
        }

        /**
         * Util method for simplifying {@link #replay(Object)} and {@link #restore(Object)} operation.
         *
         * @param captured captured {@link TransmittableThreadLocal} values from other thread from {@link #capture()}
         * @param bizLogic biz logic
         * @param <R>      the return type of biz logic
         * @return the return value of biz logic
         * @see #capture()
         * @see #replay(Object)
         * @see #restore(Object)
         * @since 2.3.1
         */
        public static <R> R runSupplierWithCaptured(@NonNull Object captured, @NonNull Supplier<R> bizLogic) {
            final Object backup = replay(captured);
            try {
                return bizLogic.get();
            } finally {
                restore(backup);
            }
        }

        /**
         * Util method for simplifying {@link #clear()} and {@link #restore(Object)} operation.
         *
         * @param bizLogic biz logic
         * @param <R>      the return type of biz logic
         * @return the return value of biz logic
         * @see #clear()
         * @see #restore(Object)
         * @since 2.9.0
         */
        public static <R> R runSupplierWithClear(@NonNull Supplier<R> bizLogic) {
            final Object backup = clear();
            try {
                return bizLogic.get();
            } finally {
                restore(backup);
            }
        }

        /**
         * Util method for simplifying {@link #replay(Object)} and {@link #restore(Object)} operation.
         *
         * @param captured captured {@link TransmittableThreadLocal} values from other thread from {@link #capture()}
         * @param bizLogic biz logic
         * @param <R>      the return type of biz logic
         * @return the return value of biz logic
         * @throws Exception exception threw by biz logic
         * @see #capture()
         * @see #replay(Object)
         * @see #restore(Object)
         * @since 2.3.1
         */
        public static <R> R runCallableWithCaptured(@NonNull Object captured, @NonNull Callable<R> bizLogic) throws Exception {
            final Object backup = replay(captured);
            try {
                return bizLogic.call();
            } finally {
                restore(backup);
            }
        }

        /**
         * Util method for simplifying {@link #clear()} and {@link #restore(Object)} operation.
         *
         * @param bizLogic biz logic
         * @param <R>      the return type of biz logic
         * @return the return value of biz logic
         * @throws Exception exception threw by biz logic
         * @see #clear()
         * @see #restore(Object)
         * @since 2.9.0
         */
        public static <R> R runCallableWithClear(@NonNull Callable<R> bizLogic) throws Exception {
            final Object backup = clear();
            try {
                return bizLogic.call();
            } finally {
                restore(backup);
            }
        }

        private static volatile WeakHashMap<ThreadLocal<Object>, TtlCopier<Object>> threadLocalHolder = new WeakHashMap<ThreadLocal<Object>, TtlCopier<Object>>();
        private static final Object threadLocalHolderUpdateLock = new Object();
        private static final Object threadLocalClearMark = new Object();

        /**
         * Register the {@link ThreadLocal}(including subclass {@link InheritableThreadLocal}) instances
         * to enhance the <b>Transmittable</b> ability for the existed {@link ThreadLocal} instances.
         * <p>
         * If the registered {@link ThreadLocal} instance is {@link TransmittableThreadLocal} just ignores and return {@code true}.
         * since a {@link TransmittableThreadLocal} instance itself has the {@code Transmittable} ability,
         * it is unnecessary to register a {@link TransmittableThreadLocal} instance.
         *
         * @param threadLocal the {@link ThreadLocal} instance that to enhance the <b>Transmittable</b> ability
         * @param copier      the {@link TtlCopier}
         * @return {@code true} if register the {@link ThreadLocal} instance and set {@code copier}, otherwise {@code false}
         * @see #registerThreadLocal(ThreadLocal, TtlCopier, boolean)
         * @since 2.11.0
         */
        public static <T> boolean registerThreadLocal(@NonNull ThreadLocal<T> threadLocal, @NonNull TtlCopier<T> copier) {
            return registerThreadLocal(threadLocal, copier, false);
        }

        /**
         * Register the {@link ThreadLocal}(including subclass {@link InheritableThreadLocal}) instances
         * to enhance the <b>Transmittable</b> ability for the existed {@link ThreadLocal} instances.
         * <p>
         * Use the shadow copier(transmit the reference directly),
         * and should use {@link #registerThreadLocal(ThreadLocal, TtlCopier)} to pass a {@link TtlCopier} explicitly
         * if a different behavior is desired.
         * <p>
         * If the registered {@link ThreadLocal} instance is {@link TransmittableThreadLocal} just ignores and return {@code true}.
         * since a {@link TransmittableThreadLocal} instance itself has the {@code Transmittable} ability,
         * it is unnecessary to register a {@link TransmittableThreadLocal} instance.
         *
         * @param threadLocal the {@link ThreadLocal} instance that to enhance the <b>Transmittable</b> ability
         * @return {@code true} if register the {@link ThreadLocal} instance and set {@code copier}, otherwise {@code false}
         * @see #registerThreadLocal(ThreadLocal, TtlCopier)
         * @see #registerThreadLocal(ThreadLocal, TtlCopier, boolean)
         * @since 2.11.0
         */
        @SuppressWarnings("unchecked")
        public static <T> boolean registerThreadLocalWithShadowCopier(@NonNull ThreadLocal<T> threadLocal) {
            return registerThreadLocal(threadLocal, (TtlCopier<T>) shadowCopier, false);
        }

        /**
         * Register the {@link ThreadLocal}(including subclass {@link InheritableThreadLocal}) instances
         * to enhance the <b>Transmittable</b> ability for the existed {@link ThreadLocal} instances.
         * <p>
         * If the registered {@link ThreadLocal} instance is {@link TransmittableThreadLocal} just ignores and return {@code true}.
         * since a {@link TransmittableThreadLocal} instance itself has the {@code Transmittable} ability,
         * it is unnecessary to register a {@link TransmittableThreadLocal} instance.
         *
         * @param threadLocal the {@link ThreadLocal} instance that to enhance the <b>Transmittable</b> ability
         * @param copier      the {@link TtlCopier}
         * @param force       if {@code true}, update {@code copier} to {@link ThreadLocal} instance
         *                    when a {@link ThreadLocal} instance is already registered; otherwise, ignore.
         * @return {@code true} if register the {@link ThreadLocal} instance and set {@code copier}, otherwise {@code false}
         * @see #registerThreadLocal(ThreadLocal, TtlCopier)
         * @since 2.11.0
         */
        @SuppressWarnings("unchecked")
        public static <T> boolean registerThreadLocal(@NonNull ThreadLocal<T> threadLocal, @NonNull TtlCopier<T> copier, boolean force) {
            if (threadLocal instanceof TransmittableThreadLocal) {
                logger.warning("register a TransmittableThreadLocal instance, this is unnecessary!");
                return true;
            }

            synchronized (threadLocalHolderUpdateLock) {
                if (!force && threadLocalHolder.containsKey(threadLocal)) return false;

                WeakHashMap<ThreadLocal<Object>, TtlCopier<Object>> newHolder = new WeakHashMap<ThreadLocal<Object>, TtlCopier<Object>>(threadLocalHolder);
                newHolder.put((ThreadLocal<Object>) threadLocal, (TtlCopier<Object>) copier);
                threadLocalHolder = newHolder;
                return true;
            }
        }

        /**
         * Register the {@link ThreadLocal}(including subclass {@link InheritableThreadLocal}) instances
         * to enhance the <b>Transmittable</b> ability for the existed {@link ThreadLocal} instances.
         * <p>
         * Use the shadow copier(transmit the reference directly),
         * and should use {@link #registerThreadLocal(ThreadLocal, TtlCopier, boolean)} to pass a {@link TtlCopier} explicitly
         * if a different behavior is desired.
         * <p>
         * If the registered {@link ThreadLocal} instance is {@link TransmittableThreadLocal} just ignores and return {@code true}.
         * since a {@link TransmittableThreadLocal} instance itself has the {@code Transmittable} ability,
         * it is unnecessary to register a {@link TransmittableThreadLocal} instance.
         *
         * @param threadLocal the {@link ThreadLocal} instance that to enhance the <b>Transmittable</b> ability
         * @param force       if {@code true}, update {@code copier} to {@link ThreadLocal} instance
         *                    when a {@link ThreadLocal} instance is already registered; otherwise, ignore.
         * @return {@code true} if register the {@link ThreadLocal} instance and set {@code copier}, otherwise {@code false}
         * @see #registerThreadLocal(ThreadLocal, TtlCopier)
         * @see #registerThreadLocal(ThreadLocal, TtlCopier, boolean)
         * @since 2.11.0
         */
        @SuppressWarnings("unchecked")
        public static <T> boolean registerThreadLocalWithShadowCopier(@NonNull ThreadLocal<T> threadLocal, boolean force) {
            return registerThreadLocal(threadLocal, (TtlCopier<T>) shadowCopier, force);
        }

        /**
         * Unregister the {@link ThreadLocal} instances
         * to remove the <b>Transmittable</b> ability for the {@link ThreadLocal} instances.
         * <p>
         * If the {@link ThreadLocal} instance is {@link TransmittableThreadLocal} just ignores and return {@code true}.
         *
         * @see #registerThreadLocal(ThreadLocal, TtlCopier)
         * @see #registerThreadLocalWithShadowCopier(ThreadLocal)
         * @since 2.11.0
         */
        public static <T> boolean unregisterThreadLocal(@NonNull ThreadLocal<T> threadLocal) {
            if (threadLocal instanceof TransmittableThreadLocal) {
                logger.warning("unregister a TransmittableThreadLocal instance, this is unnecessary!");
                return true;
            }

            synchronized (threadLocalHolderUpdateLock) {
                if (!threadLocalHolder.containsKey(threadLocal)) return false;

                WeakHashMap<ThreadLocal<Object>, TtlCopier<Object>> newHolder = new WeakHashMap<ThreadLocal<Object>, TtlCopier<Object>>(threadLocalHolder);
                newHolder.remove(threadLocal);
                threadLocalHolder = newHolder;
                return true;
            }
        }

        private static final TtlCopier<Object> shadowCopier = new TtlCopier<Object>() {
            @Override
            public Object copy(Object parentValue) {
                return parentValue;
            }
        };

        private Transmitter() {
            throw new InstantiationError("Must not instantiate this class");
        }
    }
}
