package com.alibaba.ttl3.transmitter;

import com.alibaba.crr.TransmitCallback;
import com.alibaba.crr.composite.Backup;
import com.alibaba.crr.composite.Capture;
import com.alibaba.crr.composite.CompositeTransmittable;
import com.alibaba.crr.composite.CompositeTransmitCallback;
import com.alibaba.ttl3.TransmittableThreadLocal;
import com.alibaba.ttl3.TtlCallable;
import com.alibaba.ttl3.TtlRunnable;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * {@link Transmitter} transmit all {@link TransmittableThreadLocal}
 * and other registered {@link ThreadLocal} values of the current thread to other thread.
 * <p>
 * Transmittance is completed by static methods {@link #capture()} =&gt;
 * {@link #replay(Capture)} =&gt; {@link #restore(Backup)} (aka {@code CRR} operations).
 * {@code JDK} {@link ThreadLocal} instances can be registered via {@link ThreadLocalTransmitRegistry}.
 * <p>
 * {@link Transmitter Transmitter} is <b><i>internal</i></b> manipulation api
 * for <b><i>framework/middleware integration</i></b>;
 * In general, you will <b><i>never</i></b> use it in the <i>biz/application codes</i>!
 *
 * <h2>Executor framework/middleware integration to TTL transmittance</h2>
 * Below is the example code:
 *
 * <pre>{@code
 * ///////////////////////////////////////////////////////////////////////////
 * // in thread A, capture all TransmittableThreadLocal values of thread A
 * ///////////////////////////////////////////////////////////////////////////
 *
 * Capture captured = Transmitter.capture(); // (1)
 *
 * ///////////////////////////////////////////////////////////////////////////
 * // in thread B
 * ///////////////////////////////////////////////////////////////////////////
 *
 * // replay all TransmittableThreadLocal values from thread A
 * Backup backup = Transmitter.replay(captured); // (2)
 * try {
 *     // your biz logic, run with the TransmittableThreadLocal values of thread B
 *     System.out.println("Hello");
 *     // ...
 *     return "World";
 * } finally {
 *     // restore the TransmittableThreadLocal of thread B when replay
 *     Transmitter.restore(backup); // (3)
 * }}</pre>
 * <p>
 * see the implementation code of {@link TtlRunnable} and {@link TtlCallable}
 * for more actual code samples.
 * <p>
 * Of course, {@link #replay(Capture)} and {@link #restore(Backup)} operations
 * can be simplified by util methods {@link #runCallableWithCaptured(Capture, Callable)}
 * or {@link #runSupplierWithCaptured(Capture, Supplier)}
 * and the adorable {@code Java 8 lambda syntax}.
 * <p>
 * Below is the example code:
 *
 * <pre>{@code
 * ///////////////////////////////////////////////////////////////////////////
 * // in thread A, capture all TransmittableThreadLocal values of thread A
 * ///////////////////////////////////////////////////////////////////////////
 *
 * Capture captured = Transmitter.capture(); // (1)
 *
 * ///////////////////////////////////////////////////////////////////////////
 * // in thread B
 * ///////////////////////////////////////////////////////////////////////////
 *
 * String result = runSupplierWithCaptured(captured, () -> {
 *      // your biz logic, run with the TransmittableThreadLocal values of thread A
 *      System.out.println("Hello");
 *      ...
 *      return "World";
 * }); // (2) + (3)}</pre>
 * <p>
 * The reason of providing 2 util methods is the different {@code throws Exception} type
 * to satisfy your biz logic({@code lambda}):
 * <ol>
 * <li>{@link #runCallableWithCaptured(Capture, Callable)}: {@code throws Exception}</li>
 * <li>{@link #runSupplierWithCaptured(Capture, Supplier)}: No {@code throws}</li>
 * </ol>
 * <p>
 * If you need the different {@code throws Exception} type,
 * you can define your own util method alike for function interface({@code lambda})
 * with your own {@code throws Exception} type.
 *
 * <h2>Other ThreadLocal Integration</h2>
 * <p>
 * If you can not rewrite the existed code which use {@code JDK} {@link ThreadLocal}
 * to {@link TransmittableThreadLocal}, register the {@link ThreadLocal} instances via method
 * {@link ThreadLocalTransmitRegistry#registerThreadLocal(ThreadLocal, java.util.function.UnaryOperator) ThreadLocalTransmitRegistry#registerThreadLocal}
 * to enhance the <b>Transmittable</b> ability for the existed {@link ThreadLocal} instances.
 * <p>
 * For other {@code ThreadLocal}s integration(e.g. {@code FastThreadLocal} of {@code Netty}),
 * you can implement your own {@code XxxThreadLocalRegistry}
 * (e.g. {@code FastThreadLocalRegistry}) like {@link ThreadLocalTransmitRegistry}.
 *
 * @author Yang Fang (snoop dot fy at gmail dot com)
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see TtlRunnable
 * @see TtlCallable
 * @see TransmitteeRegistry
 */
public final class Transmitter {
    private static final CompositeTransmitCallback compositeCallback = new CompositeTransmitCallback();

    static final CompositeTransmittable compositeTransmittable = new CompositeTransmittable(compositeCallback);

    /**
     * Capture all {@link TransmittableThreadLocal} and registered {@link ThreadLocal} values in the current thread.
     *
     * @return the captured {@link TransmittableThreadLocal} values
     */
    @NonNull
    public static Capture capture() {
        return compositeTransmittable.capture();
    }

    /**
     * Replay the captured {@link TransmittableThreadLocal} and registered {@link ThreadLocal} values from {@link #capture()},
     * and return the backup {@link TransmittableThreadLocal} values in the current thread before replay.
     *
     * @param captured captured {@link TransmittableThreadLocal} values from other thread from {@link #capture()}
     * @return the backup {@link TransmittableThreadLocal} values before replay
     * @see #capture()
     */
    @NonNull
    public static Backup replay(@NonNull Capture captured) {
        return compositeTransmittable.replay(captured);
    }

    /**
     * Clear all {@link TransmittableThreadLocal} and registered {@link ThreadLocal} values in the current thread,
     * and return the backup {@link TransmittableThreadLocal} values in the current thread before clear.
     * <p>
     * Semantically, the code {@code `Backup backup = clear();`} is same as {@code `Backup backup = replay(EMPTY_CAPTURE);`}.
     * <p>
     * The reason for providing this method is:
     *
     * <ol>
     * <li>lead to more readable code</li>
     * <li>need not provide the constant {@code EMPTY_CAPTURE}.</li>
     * </ol>
     *
     * @return the backup {@link TransmittableThreadLocal} values before clear
     * @see #replay(Capture)
     */
    @NonNull
    public static Backup clear() {
        return compositeTransmittable.clear();
    }

    /**
     * Restore the backup {@link TransmittableThreadLocal} and
     * registered {@link ThreadLocal} values from {@link #replay(Capture)}/{@link #clear()}.
     *
     * @param backup the backup {@link TransmittableThreadLocal} values from {@link #replay(Capture)}/{@link #clear()}
     * @see #replay(Capture)
     * @see #clear()
     */
    public static void restore(@NonNull Backup backup) {
        compositeTransmittable.restore(backup);
    }

    /**
     * Util method for simplifying {@link #replay(Capture)} and {@link #restore(Backup)} operations.
     *
     * @param captured captured {@link TransmittableThreadLocal} values from other thread from {@link #capture()}
     * @param bizLogic biz logic
     * @param <R>      the return type of biz logic
     * @return the return value of biz logic
     * @see #capture()
     * @see #replay(Capture)
     * @see #restore(Backup)
     */
    public static <R> R runSupplierWithCaptured(@NonNull Capture captured, @NonNull Supplier<R> bizLogic) {
        final Backup backup = replay(captured);
        try {
            return bizLogic.get();
        } finally {
            restore(backup);
        }
    }

    /**
     * Util method for simplifying {@link #clear()} and {@link #restore(Backup)} operations.
     *
     * @param bizLogic biz logic
     * @param <R>      the return type of biz logic
     * @return the return value of biz logic
     * @see #clear()
     * @see #restore(Backup)
     */
    public static <R> R runSupplierWithClear(@NonNull Supplier<R> bizLogic) {
        final Backup backup = clear();
        try {
            return bizLogic.get();
        } finally {
            restore(backup);
        }
    }

    /**
     * Util method for simplifying {@link #replay(Capture)} and {@link #restore(Backup)} operations.
     *
     * @param captured captured {@link TransmittableThreadLocal} values from other thread from {@link #capture()}
     * @param bizLogic biz logic
     * @param <R>      the return type of biz logic
     * @return the return value of biz logic
     * @throws Exception the exception threw by biz logic
     * @see #capture()
     * @see #replay(Capture)
     * @see #restore(Backup)
     */
    @SuppressFBWarnings("THROWS_METHOD_THROWS_CLAUSE_BASIC_EXCEPTION")
    public static <R> R runCallableWithCaptured(@NonNull Capture captured, @NonNull Callable<R> bizLogic) throws Exception {
        final Backup backup = replay(captured);
        try {
            return bizLogic.call();
        } finally {
            restore(backup);
        }
    }

    /**
     * Util method for simplifying {@link #clear()} and {@link #restore(Backup)} operations.
     *
     * @param bizLogic biz logic
     * @param <R>      the return type of biz logic
     * @return the return value of biz logic
     * @throws Exception the exception threw by biz logic
     * @see #clear()
     * @see #restore(Backup)
     */
    @SuppressFBWarnings("THROWS_METHOD_THROWS_CLAUSE_BASIC_EXCEPTION")
    public static <R> R runCallableWithClear(@NonNull Callable<R> bizLogic) throws Exception {
        final Backup backup = clear();
        try {
            return bizLogic.call();
        } finally {
            restore(backup);
        }
    }

    /**
     * Register the {@link TransmitCallback}.
     *
     * @return true if the input callback is not registered
     * @see #unregisterCallback(TransmitCallback)
     */
    public static boolean registerCallback(@NonNull TransmitCallback callback) {
        return compositeCallback.registerCallback(callback);
    }

    /**
     * Unregister the {@link TransmitCallback}.
     *
     * @return true if the input callback is registered
     * @see #registerCallback(TransmitCallback)
     */
    public static boolean unregisterCallback(@NonNull TransmitCallback callback) {
        return compositeCallback.unregisterCallback(callback);
    }

    @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
    private Transmitter() {
        throw new InstantiationError("Must not instantiate this class");
    }
}
