package com.alibaba.ttl3;

/**
 * {@code TtlCopier} create the copy value.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see TransmittableThreadLocal#withInitialAndCopier(java.util.function.Supplier, TtlCopier)
 * @see com.alibaba.ttl3.transmitter.ThreadLocalTransmitRegistry#registerThreadLocal(ThreadLocal, TtlCopier)
 */
@FunctionalInterface
public interface TtlCopier<T> {
    /**
     * the copy value logic.
     */
    T copy(T parentValue);
}
