package com.alibaba.ttl.threadpool;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadFactory;

/**
 * Disable inheritable {@link ThreadFactory}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see ThreadFactory
 * @since 2.10.0
 */
public interface DisableInheritableThreadFactory extends ThreadFactory {
    /**
     * Unwrap {@link DisableInheritableThreadFactory} to the original/underneath one.
     *
     * @return original ThreadFactory
     */
    @Nonnull
    ThreadFactory unwrap();
}
