package com.alibaba.ttl.threadpool;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadFactory;

/**
 * Disable inheritable thread factory.
 *
 * @see ThreadFactory
 */
public interface DisableInheritableThreadFactory extends ThreadFactory {
    @Nonnull
    ThreadFactory unwrap();
}
