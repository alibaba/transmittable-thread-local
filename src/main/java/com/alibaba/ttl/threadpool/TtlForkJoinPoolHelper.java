package com.alibaba.ttl.threadpool;

import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;

/**
 * Util methods to wrap/unwrap/check methods to disable Inheritable for {@link ForkJoinWorkerThreadFactory}.
 * <p>
 * <b><i>Note:</i></b>
 * <p>
 * all method is {@code null}-safe, when input parameter(eg: {@link ForkJoinWorkerThreadFactory}) is {@code null}, return {@code null}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see ForkJoinPool
 * @see ForkJoinWorkerThreadFactory
 * @see ForkJoinPool#defaultForkJoinWorkerThreadFactory
 * @see com.alibaba.ttl.TtlUnwrap#unwrap(Object)
 * @since 2.10.1
 */
public class TtlForkJoinPoolHelper {
    /**
     * Wrapper of {@link ForkJoinWorkerThreadFactory}, disable inheritable.
     *
     * @param threadFactory input thread factory
     * @see DisableInheritableForkJoinWorkerThreadFactory
     * @since 2.10.1
     */
    @Nullable
    public static ForkJoinWorkerThreadFactory getDisableInheritableForkJoinWorkerThreadFactory(@Nullable ForkJoinWorkerThreadFactory threadFactory) {
        if (threadFactory == null || isDisableInheritableForkJoinWorkerThreadFactory(threadFactory))
            return threadFactory;

        return new DisableInheritableForkJoinWorkerThreadFactoryWrapper(threadFactory);
    }

    /**
     * Wrapper of {@link ForkJoinPool#defaultForkJoinWorkerThreadFactory}, disable inheritable.
     *
     * @see #getDisableInheritableForkJoinWorkerThreadFactory(ForkJoinWorkerThreadFactory)
     * @since 2.10.1
     */
    @Nullable
    public static ForkJoinWorkerThreadFactory getDefaultDisableInheritableForkJoinWorkerThreadFactory() {
        return getDisableInheritableForkJoinWorkerThreadFactory(ForkJoinPool.defaultForkJoinWorkerThreadFactory);
    }

    /**
     * check the {@link ForkJoinWorkerThreadFactory} is {@link DisableInheritableForkJoinWorkerThreadFactory} or not.
     *
     * @see DisableInheritableForkJoinWorkerThreadFactory
     * @since 2.10.1
     */
    public static boolean isDisableInheritableForkJoinWorkerThreadFactory(@Nullable ForkJoinWorkerThreadFactory threadFactory) {
        return threadFactory instanceof DisableInheritableForkJoinWorkerThreadFactory;
    }

    /**
     * Unwrap {@link DisableInheritableForkJoinWorkerThreadFactory} to the original/underneath one.
     *
     * @see com.alibaba.ttl.TtlUnwrap#unwrap(Object)
     * @see DisableInheritableForkJoinWorkerThreadFactory
     * @since 2.10.1
     */
    @Nullable
    public static ForkJoinWorkerThreadFactory unwrap(@Nullable ForkJoinWorkerThreadFactory threadFactory) {
        if (!isDisableInheritableForkJoinWorkerThreadFactory(threadFactory)) return threadFactory;

        return ((DisableInheritableForkJoinWorkerThreadFactory) threadFactory).unwrap();
    }

    private TtlForkJoinPoolHelper() {
    }
}
