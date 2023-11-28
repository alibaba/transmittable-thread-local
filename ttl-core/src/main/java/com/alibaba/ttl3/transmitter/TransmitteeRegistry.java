package com.alibaba.ttl3.transmitter;

import com.alibaba.ttl3.TransmittableThreadLocal;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Transmittee(aka {@code ThreadLocal}) Integration,
 * {@link TransmittableThreadLocal} and {@code JDK} {@link ThreadLocal}
 * (via {@link ThreadLocalTransmitRegistry}) have been builtin integrated.
 *
 * <h2>About {@code JDK} {@link ThreadLocal} Integration</h2>
 * <p>
 * If you can not rewrite the existed code which use {@code JDK} {@link ThreadLocal}
 * to {@link TransmittableThreadLocal}, register the {@link ThreadLocal} instances
 * via {@link ThreadLocalTransmitRegistry}
 * to enhance the <b>Transmittable</b> ability for the existed {@link ThreadLocal} instances.
 *
 * <h2>Other {@code ThreadLocal} Integration</h2>
 * <p>
 * For other {@code ThreadLocal}s integration(e.g. {@code FastThreadLocal} of {@code Netty}),
 * you can implement your own {@code XxxThreadLocalRegistry}
 * (e.g. {@code FastThreadLocalRegistry}) like {@link ThreadLocalTransmitRegistry}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see Transmittee
 * @see ThreadLocalTransmitRegistry
 */
public final class TransmitteeRegistry {
    /**
     * Register the transmittee({@code CRR}), the extension point for other {@code ThreadLocal}.
     *
     * @param <C> the transmittee capture data type
     * @param <B> the transmittee backup data type
     * @return true if the input transmittee is not registered
     * @see #unregisterTransmittee(Transmittee)
     */
    public static <C, B> boolean registerTransmittee(@NonNull Transmittee<C, B> transmittee) {
        return Transmitter.compositeCrrTransmit.registerCrrTransmit(transmittee);
    }

    /**
     * Unregister the transmittee({@code CRR}), the extension point for other {@code ThreadLocal}.
     *
     * @param <C> the transmittee capture data type
     * @param <B> the transmittee backup data type
     * @return true if the input transmittee is registered
     * @see #registerTransmittee(Transmittee)
     */
    public static <C, B> boolean unregisterTransmittee(@NonNull Transmittee<C, B> transmittee) {
        return Transmitter.compositeCrrTransmit.unregisterCrrTransmit(transmittee);
    }

    @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
    private TransmitteeRegistry() {
        throw new InstantiationError("Must not instantiate this class");
    }
}
