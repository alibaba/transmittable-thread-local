package com.alibaba.ttl3.threadpool;

import com.alibaba.ttl3.spi.TtlWrapper;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;

/**
 * Disable inheritable {@link ForkJoinWorkerThreadFactory}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public interface DisableInheritableForkJoinWorkerThreadFactory extends ForkJoinWorkerThreadFactory, TtlWrapper<ForkJoinWorkerThreadFactory> {
    /**
     * Unwrap {@link DisableInheritableThreadFactory} to the original/underneath one.
     */
    @NonNull
    @Override
    ForkJoinWorkerThreadFactory unwrap();
}
