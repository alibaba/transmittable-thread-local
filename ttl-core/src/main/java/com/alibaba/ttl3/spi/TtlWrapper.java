package com.alibaba.ttl3.spi;

import com.alibaba.ttl3.executor.TtlExecutors;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.concurrent.ExecutorService;

/**
 * Ttl Wrapper interface.
 * <p>
 * Used to mark wrapper types, for example:
 * <ul>
 *     <li>{@link com.alibaba.ttl3.TtlCallable TtlCallable}
 *     <li>{@code TtlExecutors} created by util method
 *         {@link TtlExecutors#getTtlExecutorService(ExecutorService)} getTtlExecutorService}
 *     <li>{@code DisableInheritableThreadFactory} created by util method
 *         {@link TtlExecutors#getDisableInheritableThreadFactory(java.util.concurrent.ThreadFactory) getDisableInheritableThreadFactory}
 * </ul>
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see com.alibaba.ttl3.TtlWrappers#unwrap
 * @see com.alibaba.ttl3.TtlCallable
 * @see com.alibaba.ttl3.TtlRunnable
 * @see com.alibaba.ttl3.executor.TtlExecutors
 */
public interface TtlWrapper<T> extends TtlEnhanced {
    /**
     * unwrap {@link TtlWrapper} to the original/underneath one.
     *
     * @see com.alibaba.ttl3.TtlWrappers#unwrap(Object)
     */
    @NonNull
    T unwrap();
}
