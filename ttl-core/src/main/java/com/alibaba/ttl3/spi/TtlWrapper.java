package com.alibaba.ttl3.spi;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Ttl Wrapper interface.
 * <p>
 * Used to mark wrapper types, for example:
 * <ul>
 *     <li>{@link com.alibaba.ttl3.TtlRunnable TtlRunnable}s, {@link com.alibaba.ttl3.TtlCallable TtlCallable}s
 *     <li>{@code TtlWrappers} created by util methods {@code wrap*()} of class
 *         {@link com.alibaba.ttl3.TtlWrappers TtlWrappers},
 *         e.g. {@link com.alibaba.ttl3.TtlWrappers#wrapSupplier(java.util.function.Supplier) wrapSupplier}
 *     <li>{@code TtlExecutors} created by util methods {@code get*()} of class
 *         {@link com.alibaba.ttl3.executor.TtlExecutors TtlExecutors},
 *         e.g. {@link com.alibaba.ttl3.executor.TtlExecutors#getTtlExecutorService(java.util.concurrent.ExecutorService) getTtlExecutorService}
 *     <li>{@code DisableInheritableThreadFactories} created by util method
 *         {@link com.alibaba.ttl3.executor.TtlExecutors#getDisableInheritableThreadFactory(java.util.concurrent.ThreadFactory) getDisableInheritableThreadFactory}
 * </ul>
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see com.alibaba.ttl3.TtlWrappers#unwrap
 * @see com.alibaba.ttl3.TtlRunnable
 * @see com.alibaba.ttl3.TtlCallable
 * @see com.alibaba.ttl3.TtlWrappers
 * @see com.alibaba.ttl3.executor.TtlExecutors
 */
public interface TtlWrapper<T> extends TtlEnhanced {
    /**
     * unwrap {@link TtlWrapper} to the original/underneath one.
     *
     * @see com.alibaba.ttl3.TtlWrappers#unwrap
     */
    @NonNull
    T unwrap();
}
