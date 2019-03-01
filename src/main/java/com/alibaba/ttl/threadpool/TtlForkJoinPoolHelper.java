package com.alibaba.ttl.threadpool;

import javax.annotation.Nullable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;

/**
 * Factory Utils for getting TTL wrapper of {@link ForkJoinWorkerThreadFactory}.
 * <p>
 * all method is {@code null}-safe, when input parameter(eg: {@link ForkJoinWorkerThreadFactory}) is {@code null}, return {@code null}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see ForkJoinPool
 * @see ForkJoinWorkerThreadFactory
 * @see ForkJoinPool#defaultForkJoinWorkerThreadFactory
 * @since 2.10.1
 */
public class TtlForkJoinPoolHelper {
    /**
     * Wrapper of {@link ForkJoinWorkerThreadFactory}, disable inheritable.
     *
     * @param threadFactory input thread factory
     * @return decorated ThreadFactory
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
     * @return default decorated ForkJoinWorkerThreadFactory without inheritable feature
     * @see #getDisableInheritableForkJoinWorkerThreadFactory(ForkJoinWorkerThreadFactory)
     * @since 2.10.1
     */
    @Nullable
    public static ForkJoinWorkerThreadFactory getDefaultDisableInheritableForkJoinWorkerThreadFactory() {
        return getDisableInheritableForkJoinWorkerThreadFactory(ForkJoinPool.defaultForkJoinWorkerThreadFactory);
    }

    /**
     * check the {@link ForkJoinWorkerThreadFactory} is  {@link DisableInheritableForkJoinWorkerThreadFactory} or not.
     *
     * @param threadFactory target ForkJoinWorkerThreadFactory
     * @return if it's decorated
     * @see DisableInheritableForkJoinWorkerThreadFactory
     * @since 2.10.1
     */
    public static boolean isDisableInheritableForkJoinWorkerThreadFactory(@Nullable ForkJoinWorkerThreadFactory threadFactory) {
        return threadFactory instanceof DisableInheritableForkJoinWorkerThreadFactory;
    }

    /**
     * Unwrap {@link DisableInheritableForkJoinWorkerThreadFactory} to the original/underneath one.
     *
     * @param threadFactory target ForkJoinWorkerThreadFactory
     * @return original ForkJoinWorkerThreadFactory
     * @see DisableInheritableForkJoinWorkerThreadFactory
     * @since 2.10.1
     */
    @Nullable
    public static ForkJoinWorkerThreadFactory unwrap(@Nullable ForkJoinWorkerThreadFactory threadFactory) {
        if (!isDisableInheritableForkJoinWorkerThreadFactory(threadFactory)) return threadFactory;

        return ((DisableInheritableForkJoinWorkerThreadFactoryWrapper) threadFactory).unwrap();
    }

    private TtlForkJoinPoolHelper() {
    }
}
