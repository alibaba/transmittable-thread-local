package com.alibaba.ttl.threadpool;

import com.alibaba.ttl.spi.TtlWrapper;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.concurrent.ThreadFactory;

/**
 * Disable inheritable {@link ThreadFactory}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see ThreadFactory
 * @since 2.10.0
 */
public interface DisableInheritableThreadFactory extends ThreadFactory, TtlWrapper<ThreadFactory> {
    /**
     * Unwrap {@link DisableInheritableThreadFactory} to the original/underneath one.
     */
    @Override
    @NonNull
    ThreadFactory unwrap();
}
