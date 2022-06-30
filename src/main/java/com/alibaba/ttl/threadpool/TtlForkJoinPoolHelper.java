package com.alibaba.ttl.threadpool;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.jetbrains.annotations.Contract;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ThreadFactory;

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
 * @see java.util.stream.Stream
 * @see TtlExecutors
 * @since 2.10.1
 */
public final class TtlForkJoinPoolHelper {
    /**
     * Wrapper of {@link ForkJoinWorkerThreadFactory}, disable inheritable.
     *
     * @param threadFactory input thread factory
     * @see DisableInheritableForkJoinWorkerThreadFactory
     * @see TtlExecutors#getDisableInheritableThreadFactory(ThreadFactory)
     * @since 2.10.1
     */
    @Nullable
    @Contract(value = "null -> null; !null -> !null", pure = true)
    public static ForkJoinWorkerThreadFactory getDisableInheritableForkJoinWorkerThreadFactory(@Nullable ForkJoinWorkerThreadFactory threadFactory) {
        if (threadFactory == null || isDisableInheritableForkJoinWorkerThreadFactory(threadFactory))
            return threadFactory;

        return new DisableInheritableForkJoinWorkerThreadFactoryWrapper(threadFactory);
    }

    /**
     * Wrapper of {@link ForkJoinPool#defaultForkJoinWorkerThreadFactory}, disable inheritable.
     *
     * @see #getDisableInheritableForkJoinWorkerThreadFactory(ForkJoinWorkerThreadFactory)
     * @see TtlExecutors#getDefaultDisableInheritableThreadFactory()
     * @since 2.10.1
     */
    @NonNull
    public static ForkJoinWorkerThreadFactory getDefaultDisableInheritableForkJoinWorkerThreadFactory() {
        return getDisableInheritableForkJoinWorkerThreadFactory(ForkJoinPool.defaultForkJoinWorkerThreadFactory);
    }

    /**
     * check the {@link ForkJoinWorkerThreadFactory} is {@link DisableInheritableForkJoinWorkerThreadFactory} or not.
     *
     * @see #getDisableInheritableForkJoinWorkerThreadFactory(ForkJoinWorkerThreadFactory)
     * @see #getDefaultDisableInheritableForkJoinWorkerThreadFactory()
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
     * @see TtlExecutors#unwrap(ThreadFactory)
     * @since 2.10.1
     */
    @Nullable
    @Contract(value = "null -> null; !null -> !null", pure = true)
    public static ForkJoinWorkerThreadFactory unwrap(@Nullable ForkJoinWorkerThreadFactory threadFactory) {
        if (!isDisableInheritableForkJoinWorkerThreadFactory(threadFactory)) return threadFactory;

        return ((DisableInheritableForkJoinWorkerThreadFactory) threadFactory).unwrap();
    }

    private TtlForkJoinPoolHelper() {
        throw new InstantiationError("Must not instantiate this class");
    }
}
