package com.alibaba.ttl.threadpool;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;

/**
 * Disable inheritable {@link ForkJoinWorkerThreadFactory}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 2.10.1
 */
public interface DisableInheritableForkJoinWorkerThreadFactory extends ForkJoinWorkerThreadFactory {
    /**
     * Unwrap {@link DisableInheritableThreadFactory} to the original/underneath one.
     */
    @NonNull
    ForkJoinWorkerThreadFactory unwrap();
}
