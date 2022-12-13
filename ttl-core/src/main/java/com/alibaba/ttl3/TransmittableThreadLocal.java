package com.alibaba.ttl3;

import com.alibaba.ttl3.executor.TtlExecutors;
import com.alibaba.ttl3.transmitter.Transmittee;
import com.alibaba.ttl3.transmitter.TransmitteeRegistry;
import edu.umd.cs.findbugs.annotations.NonNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import static com.alibaba.ttl3.internal.util.Utils.newHashMap;

/**
 * {@link TransmittableThreadLocal}({@code TTL}) can transmit the value from the thread of submitting task
 * to the thread of executing task even using thread pooling components.
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
 * Disable inheritable for thread pooling components by wrapping thread factories using methods
 * {@link TtlExecutors#getDisableInheritableThreadFactory(java.util.concurrent.ThreadFactory) getDisableInheritableThreadFactory} /
 * {@link TtlExecutors#getDefaultDisableInheritableForkJoinWorkerThreadFactory() getDefaultDisableInheritableForkJoinWorkerThreadFactory}.
 * <br>
 * Or you can turn on "disable inheritable for thread pool" by {@code TTL Java Agent}
 * to wrap thread factories for thread pooling components automatically and transparently.
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
 * @see <a href="https://github.com/alibaba/transmittable-thread-local">user guide docs and code repo of TransmittableThreadLocal(TTL)</a>
 * @see TtlRunnable
 * @see TtlCallable
 * @see TtlExecutors
 * @see TtlExecutors#getTtlExecutor(java.util.concurrent.Executor)
 * @see TtlExecutors#getTtlExecutorService(java.util.concurrent.ExecutorService)
 * @see TtlExecutors#getTtlScheduledExecutorService(java.util.concurrent.ScheduledExecutorService)
 * @see TtlExecutors#getDefaultDisableInheritableThreadFactory()
 * @see TtlExecutors#getDisableInheritableThreadFactory(java.util.concurrent.ThreadFactory)
 * @see TtlExecutors#getDefaultDisableInheritableForkJoinWorkerThreadFactory()
 * @see TtlExecutors#getDisableInheritableForkJoinWorkerThreadFactory(java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory)
 */
public class TransmittableThreadLocal<T> extends InheritableThreadLocal<T> {
    private final boolean disableIgnoreNullValueSemantics;

    /**
     * Default constructor. Create a {@link TransmittableThreadLocal} instance with "Ignore-Null-Value Semantics".
     * <p>
     * About "Ignore-Null-Value Semantics":
     *
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
     */
    public TransmittableThreadLocal(boolean disableIgnoreNullValueSemantics) {
        this.disableIgnoreNullValueSemantics = disableIgnoreNullValueSemantics;
    }

    /**
     * Creates a transmittable thread local variable.
     * The initial value({@link #initialValue()}) of the variable is
     * determined by invoking the {@link #get()} method on the {@code Supplier}.
     *
     * @param <S>      the type of the thread local's value
     * @param supplier the supplier to be used to determine the initial value
     * @return a new transmittable thread local variable
     * @throws NullPointerException if the specified supplier is null
     * @see #withInitialAndCopier(Supplier, TtlCopier)
     */
    @NonNull
    @SuppressWarnings("ConstantConditions")
    public static <S> TransmittableThreadLocal<S> withInitial(@NonNull Supplier<? extends S> supplier) {
        if (supplier == null) throw new NullPointerException("supplier is null");

        return new SuppliedTransmittableThreadLocal<>(supplier, null, null);
    }

    /**
     * Creates a transmittable thread local variable.
     * The initial value({@link #initialValue()}) of the variable is
     * determined by invoking the {@link #get()} method on the {@code Supplier};
     * and the child value({@link #childValue(Object)}) and the transmittee value({@link #transmitteeValue(Object)}) of the variable is
     * determined by invoking the {@link  TtlCopier#copy(Object)} method on the {@code TtlCopier}.
     *
     * @param <S>                                    the type of the thread local's value
     * @param supplier                               the supplier to be used to determine the initial value
     * @param copierForChildValueAndTransmitteeValue the ttl copier to be used to determine the child value and the transmittee value
     * @return a new transmittable thread local variable
     * @throws NullPointerException if the specified supplier or copier is null
     * @see #withInitial(Supplier)
     */
    @NonNull
    @ParametersAreNonnullByDefault
    @SuppressWarnings("ConstantConditions")
    public static <S> TransmittableThreadLocal<S> withInitialAndCopier(Supplier<? extends S> supplier, TtlCopier<S> copierForChildValueAndTransmitteeValue) {
        if (supplier == null) throw new NullPointerException("supplier is null");
        if (copierForChildValueAndTransmitteeValue == null) throw new NullPointerException("ttl copier is null");

        return new SuppliedTransmittableThreadLocal<>(supplier, copierForChildValueAndTransmitteeValue, copierForChildValueAndTransmitteeValue);
    }

    /**
     * Creates a transmittable thread local variable.
     * The initial value({@link #initialValue()}) of the variable is
     * determined by invoking the {@link #get()} method on the {@code Supplier};
     * and the child value({@link #childValue(Object)})}) and the transmittee value({@link #transmitteeValue(Object)}) of the variable is
     * determined by invoking the {@link  TtlCopier#copy(Object)} method on the {@code TtlCopier}.
     * <p>
     * <B><I>NOTE:</I></B><br>
     * Recommend use {@link #withInitialAndCopier(Supplier, TtlCopier)} instead of this method.
     * In most cases, the logic of determining the child value({@link #childValue(Object)})
     * and the transmittee value({@link #transmitteeValue(Object)}) should be the same.
     *
     * @param <S>                       the type of the thread local's value
     * @param supplier                  the supplier to be used to determine the initial value
     * @param copierForChildValue       the ttl copier to be used to determine the child value
     * @param copierForTransmitteeValue the ttl copier to be used to determine the transmittee value
     * @return a new transmittable thread local variable
     * @throws NullPointerException if the specified supplier or copier is null
     * @see #withInitial(Supplier)
     * @see #withInitialAndCopier(Supplier, TtlCopier)
     */
    @NonNull
    @ParametersAreNonnullByDefault
    @SuppressWarnings("ConstantConditions")
    public static <S> TransmittableThreadLocal<S> withInitialAndCopier(Supplier<? extends S> supplier, TtlCopier<S> copierForChildValue, TtlCopier<S> copierForTransmitteeValue) {
        if (supplier == null) throw new NullPointerException("supplier is null");
        if (copierForChildValue == null) throw new NullPointerException("ttl copier for child value is null");
        if (copierForTransmitteeValue == null) throw new NullPointerException("ttl copier for copy value is null");

        return new SuppliedTransmittableThreadLocal<>(supplier, copierForChildValue, copierForTransmitteeValue);
    }

    /**
     * An extension of ThreadLocal that obtains its initial value from the specified {@code Supplier}
     * and obtains its child value and transmittee value from the specified ttl copier.
     */
    private static final class SuppliedTransmittableThreadLocal<T> extends TransmittableThreadLocal<T> {
        private final Supplier<? extends T> supplier;
        private final TtlCopier<T> copierForChildValue;
        private final TtlCopier<T> copierForTransmitteeValue;

        SuppliedTransmittableThreadLocal(Supplier<? extends T> supplier, TtlCopier<T> copierForChildValue, TtlCopier<T> copierForTransmitteeValue) {
            if (supplier == null) throw new NullPointerException("supplier is null");
            this.supplier = supplier;
            this.copierForChildValue = copierForChildValue;
            this.copierForTransmitteeValue = copierForTransmitteeValue;
        }

        @Override
        protected T initialValue() {
            return supplier.get();
        }

        @Override
        protected T childValue(T parentValue) {
            if (copierForChildValue != null) return copierForChildValue.copy(parentValue);
            else return super.childValue(parentValue);
        }

        @Override
        public T transmitteeValue(T parentValue) {
            if (copierForTransmitteeValue != null) return copierForTransmitteeValue.copy(parentValue);
            else return super.transmitteeValue(parentValue);
        }
    }

    /**
     * Computes the child's initial value for this transmittable thread-local
     * variable as a function of the parent's value at the time the child
     * thread is created. This method is called from within the parent
     * thread before the child is started.
     * <p>
     * <b>Note</b>:<br>
     * This method is overridden, and merely call {@link #transmitteeValue(Object)}.
     * In most cases, the logic of determining the child value({@link #childValue(Object)})
     * and the transmittee value({@link #transmitteeValue(Object)}) should be the same,
     * so it's NOT recommended to override this method in subclass.
     *
     * @param parentValue the parent thread's value
     * @return the child thread's initial value
     */
    @Override
    protected T childValue(T parentValue) {
        return transmitteeValue(parentValue);
    }

    /**
     * Computes the value for this transmittable thread-local variable
     * as a function of the source thread's value at the time the task
     * Object is created.
     * <p>
     * This method is called from {@link TtlRunnable} or
     * {@link TtlCallable} when it create, before the task is started.
     * <p>
     * <b>Note</b>:<br>
     * This method merely returns reference of its source thread value(the shadow copy),
     * and should be overridden if a different behavior is desired.
     * It's recommended to override this method in subclass.
     */
    protected T transmitteeValue(T parentValue) {
        return parentValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final T get() {
        T value = super.get();
        if (disableIgnoreNullValueSemantics || value != null) addThisToHolder();
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void set(T value) {
        if (!disableIgnoreNullValueSemantics && value == null) {
            // may set null to remove value
            remove();
        } else {
            super.set(value);
            addThisToHolder();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void remove() {
        removeThisFromHolder();
        super.remove();
    }

    private void superRemove() {
        super.remove();
    }

    private T getTransmitteeValue() {
        return transmitteeValue(get());
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
                    return new WeakHashMap<>();
                }

                @Override
                protected WeakHashMap<TransmittableThreadLocal<Object>, ?> childValue(WeakHashMap<TransmittableThreadLocal<Object>, ?> parentValue) {
                    return new WeakHashMap<>(parentValue);
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


    private static class TtlTransmittee implements Transmittee<HashMap<TransmittableThreadLocal<Object>, Object>, HashMap<TransmittableThreadLocal<Object>, Object>> {
        @NonNull
        @Override
        public HashMap<TransmittableThreadLocal<Object>, Object> capture() {
            final HashMap<TransmittableThreadLocal<Object>, Object> ttl2Value = newHashMap(holder.get().size());
            for (TransmittableThreadLocal<Object> threadLocal : holder.get().keySet()) {
                ttl2Value.put(threadLocal, threadLocal.getTransmitteeValue());
            }
            return ttl2Value;
        }

        @NonNull
        @Override
        public HashMap<TransmittableThreadLocal<Object>, Object> replay(@NonNull HashMap<TransmittableThreadLocal<Object>, Object> captured) {
            final HashMap<TransmittableThreadLocal<Object>, Object> backup = newHashMap(holder.get().size());

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

            return backup;
        }

        @NonNull
        @Override
        public HashMap<TransmittableThreadLocal<Object>, Object> clear() {
            return replay(newHashMap(0));
        }

        @Override
        public void restore(@NonNull HashMap<TransmittableThreadLocal<Object>, Object> backup) {
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
    }

    private static final TtlTransmittee ttlTransmittee = new TtlTransmittee();

    static {
        TransmitteeRegistry.registerTransmittee(ttlTransmittee);
    }
}
