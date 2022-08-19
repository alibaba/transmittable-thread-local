package com.alibaba.ttl3;

import com.alibaba.ttl3.transmitter.ThreadLocalTransmitRegistry;

import java.util.function.Supplier;

/**
 * {@code TtlCopier} create the copy value.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see TransmittableThreadLocal#withInitialAndCopier(Supplier, TtlCopier)
 * @see ThreadLocalTransmitRegistry#registerThreadLocal(ThreadLocal, TtlCopier)
 */
@FunctionalInterface
public interface TtlCopier<T> {
    /**
     * the copy value logic.
     */
    T copy(T parentValue);
}
