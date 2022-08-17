package com.alibaba.ttl3.threadpool;

import com.alibaba.ttl3.spi.TtlWrapper;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.concurrent.ThreadFactory;

/**
 * Disable inheritable {@link ThreadFactory}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see ThreadFactory
 */
public interface DisableInheritableThreadFactory extends ThreadFactory, TtlWrapper<ThreadFactory> {
    /**
     * Unwrap {@link DisableInheritableThreadFactory} to the original/underneath one.
     */
    @NonNull
    @Override
    ThreadFactory unwrap();
}
