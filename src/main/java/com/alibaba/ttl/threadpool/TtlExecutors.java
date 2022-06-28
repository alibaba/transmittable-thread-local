package com.alibaba.ttl.threadpool;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.spi.TtlEnhanced;
import com.alibaba.ttl.spi.TtlWrapper;
import com.alibaba.ttl.threadpool.agent.TtlAgent;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.jetbrains.annotations.Contract;

import java.util.Comparator;
import java.util.concurrent.*;

/**
 * Util methods for TTL wrapper of jdk executors.
 *
 * <ol>
 *     <li>wrap(factory)/check/unwrap methods for TTL wrapper of jdk executors({@link Executor}, {@link ExecutorService}, {@link ScheduledExecutorService}).</li>
 *     <li>wrap/unwrap/check methods to disable Inheritable for {@link ThreadFactory}.</li>
 *     <li>wrap/unwrap/check methods to {@code TtlRunnableUnwrapComparator} for {@link PriorityBlockingQueue}.</li>
 * </ol>
 * <p>
 * <b><i>Note:</i></b>
 * <ul>
 * <li>all method is {@code null}-safe, when input {@code executor} parameter is {@code null}, return {@code null}.</li>
 * <li>skip wrap/decoration thread pool/{@code executor}(aka. just return input {@code executor})
 * when ttl agent is loaded, Or when input {@code executor} is already wrapped/decorated.</li>
 * </ul>
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see Executor
 * @see ExecutorService
 * @see ScheduledExecutorService
 * @see ThreadPoolExecutor
 * @see ScheduledThreadPoolExecutor
 * @see Executors
 * @see java.util.concurrent.CompletionService
 * @see java.util.concurrent.ExecutorCompletionService
 * @see ThreadFactory
 * @see Executors#defaultThreadFactory()
 * @see PriorityBlockingQueue
 * @see TtlForkJoinPoolHelper
 * @since 0.9.0
 */
public final class TtlExecutors {
    /**
     * {@link TransmittableThreadLocal} Wrapper of {@link Executor},
     * transmit the {@link TransmittableThreadLocal} from the task submit time of {@link Runnable}
     * to the execution time of {@link Runnable}.
     * <p>
     * NOTE: sine v2.12.0 the idempotency of return wrapped Executor is changed to true,
     * so the wrapped Executor can be cooperated with the usage of "Decorate Runnable and Callable".
     * <p>
     * About idempotency: if is idempotent,
     * it's allowed to submit the {@link com.alibaba.ttl.TtlRunnable}/{@link com.alibaba.ttl.TtlCallable} to the wrapped Executor;
     * otherwise throw {@link IllegalStateException}.
     *
     * @param executor input Executor
     * @return wrapped Executor
     * @see com.alibaba.ttl.TtlRunnable#get(Runnable, boolean, boolean)
     * @see com.alibaba.ttl.TtlCallable#get(Callable, boolean, boolean)
     */
    @Nullable
    @Contract("null -> null; !null -> !null")
    public static Executor getTtlExecutor(@Nullable Executor executor) {
        if (TtlAgent.isTtlAgentLoaded() || null == executor || executor instanceof TtlEnhanced) {
            return executor;
        }
        return new ExecutorTtlWrapper(executor, true);
    }

    /**
     * {@link TransmittableThreadLocal} Wrapper of {@link ExecutorService},
     * transmit the {@link TransmittableThreadLocal} from the task submit time of {@link Runnable} or {@link java.util.concurrent.Callable}
     * to the execution time of {@link Runnable} or {@link java.util.concurrent.Callable}.
     * <p>
     * NOTE: sine v2.12.0 the idempotency of return wrapped ExecutorService is changed to true,
     * so the wrapped ExecutorService can be cooperated with the usage of "Decorate Runnable and Callable".
     * <p>
     * About idempotency: if is idempotent,
     * it's allowed to submit the {@link com.alibaba.ttl.TtlRunnable}/{@link com.alibaba.ttl.TtlCallable} to the wrapped ExecutorService;
     * otherwise throw {@link IllegalStateException}.
     *
     * @param executorService input ExecutorService
     * @return wrapped ExecutorService
     * @see com.alibaba.ttl.TtlRunnable#get(Runnable, boolean, boolean)
     * @see com.alibaba.ttl.TtlCallable#get(Callable, boolean, boolean)
     */
    @Nullable
    @Contract("null -> null; !null -> !null")
    public static ExecutorService getTtlExecutorService(@Nullable ExecutorService executorService) {
        if (TtlAgent.isTtlAgentLoaded() || executorService == null || executorService instanceof TtlEnhanced) {
            return executorService;
        }
        return new ExecutorServiceTtlWrapper(executorService, true);
    }


    /**
     * {@link TransmittableThreadLocal} Wrapper of {@link ScheduledExecutorService},
     * transmit the {@link TransmittableThreadLocal} from the task submit time of {@link Runnable} or {@link java.util.concurrent.Callable}
     * to the execution time of {@link Runnable} or {@link java.util.concurrent.Callable}.
     * <p>
     * NOTE: sine v2.12.0 the idempotency of return wrapped ScheduledExecutorService is changed to true,
     * so the wrapped ScheduledExecutorService can be cooperated with the usage of "Decorate Runnable and Callable".
     * <p>
     * About idempotency: if is idempotent,
     * it's allowed to submit the {@link com.alibaba.ttl.TtlRunnable}/{@link com.alibaba.ttl.TtlCallable} to the wrapped ScheduledExecutorService;
     * otherwise throw {@link IllegalStateException}.
     *
     * @param scheduledExecutorService input scheduledExecutorService
     * @return wrapped scheduledExecutorService
     * @see com.alibaba.ttl.TtlRunnable#get(Runnable, boolean, boolean)
     * @see com.alibaba.ttl.TtlCallable#get(Callable, boolean, boolean)
     */
    @Nullable
    @Contract("null -> null; !null -> !null")
    public static ScheduledExecutorService getTtlScheduledExecutorService(@Nullable ScheduledExecutorService scheduledExecutorService) {
        if (TtlAgent.isTtlAgentLoaded() || scheduledExecutorService == null || scheduledExecutorService instanceof TtlEnhanced) {
            return scheduledExecutorService;
        }
        return new ScheduledExecutorServiceTtlWrapper(scheduledExecutorService, true);
    }

    /**
     * check the executor is a TTL executor wrapper or not.
     * <p>
     * if the parameter executor is TTL wrapper, return {@code true}, otherwise {@code false}.
     * <p>
     * NOTE: if input executor is {@code null}, return {@code false}.
     *
     * @param executor input executor
     * @param <T>      Executor type
     * @see #getTtlExecutor(Executor)
     * @see #getTtlExecutorService(ExecutorService)
     * @see #getTtlScheduledExecutorService(ScheduledExecutorService)
     * @see #unwrap(Executor)
     * @since 2.8.0
     */
    public static <T extends Executor> boolean isTtlWrapper(@Nullable T executor) {
        return executor instanceof TtlWrapper;
    }

    /**
     * Unwrap TTL executor wrapper to the original/underneath one.
     * <p>
     * if the parameter executor is TTL wrapper, return the original/underneath executor;
     * otherwise, just return the input parameter executor.
     * <p>
     * NOTE: if input executor is {@code null}, return {@code null}.
     *
     * @param executor input executor
     * @param <T>      Executor type
     * @see #getTtlExecutor(Executor)
     * @see #getTtlExecutorService(ExecutorService)
     * @see #getTtlScheduledExecutorService(ScheduledExecutorService)
     * @see #isTtlWrapper(Executor)
     * @see com.alibaba.ttl.TtlUnwrap#unwrap(Object)
     * @since 2.8.0
     */
    @Nullable
    @Contract("null -> null; !null -> !null")
    @SuppressWarnings("unchecked")
    public static <T extends Executor> T unwrap(@Nullable T executor) {
        if (!isTtlWrapper(executor)) return executor;

        return (T) ((ExecutorTtlWrapper) executor).unwrap();
    }

    /**
     * Wrapper of {@link ThreadFactory}, disable inheritable.
     *
     * @param threadFactory input thread factory
     * @see DisableInheritableThreadFactory
     * @see TtlForkJoinPoolHelper#getDisableInheritableForkJoinWorkerThreadFactory
     * @since 2.10.0
     */
    @Nullable
    @Contract("null -> null; !null -> !null")
    public static ThreadFactory getDisableInheritableThreadFactory(@Nullable ThreadFactory threadFactory) {
        if (threadFactory == null || isDisableInheritableThreadFactory(threadFactory)) return threadFactory;

        return new DisableInheritableThreadFactoryWrapper(threadFactory);
    }

    /**
     * Wrapper of {@link Executors#defaultThreadFactory()}, disable inheritable.
     *
     * @see #getDisableInheritableThreadFactory(ThreadFactory)
     * @see TtlForkJoinPoolHelper#getDefaultDisableInheritableForkJoinWorkerThreadFactory()
     * @since 2.10.0
     */
    @NonNull
    public static ThreadFactory getDefaultDisableInheritableThreadFactory() {
        return getDisableInheritableThreadFactory(Executors.defaultThreadFactory());
    }

    /**
     * check the {@link ThreadFactory} is {@link DisableInheritableThreadFactory} or not.
     *
     * @see TtlForkJoinPoolHelper#getDisableInheritableForkJoinWorkerThreadFactory
     * @see #getDefaultDisableInheritableThreadFactory()
     * @see DisableInheritableThreadFactory
     * @since 2.10.0
     */
    public static boolean isDisableInheritableThreadFactory(@Nullable ThreadFactory threadFactory) {
        return threadFactory instanceof DisableInheritableThreadFactory;
    }

    /**
     * Unwrap {@link DisableInheritableThreadFactory} to the original/underneath one.
     *
     * @see #getDisableInheritableThreadFactory(ThreadFactory)
     * @see #getDefaultDisableInheritableThreadFactory()
     * @see #isDisableInheritableThreadFactory(ThreadFactory)
     * @see TtlForkJoinPoolHelper#unwrap(ForkJoinPool.ForkJoinWorkerThreadFactory)
     * @see DisableInheritableThreadFactory
     * @see com.alibaba.ttl.TtlUnwrap#unwrap(Object)
     * @since 2.10.0
     */
    @Nullable
    @Contract("null -> null; !null -> !null")
    public static ThreadFactory unwrap(@Nullable ThreadFactory threadFactory) {
        if (!isDisableInheritableThreadFactory(threadFactory)) return threadFactory;

        return ((DisableInheritableThreadFactory) threadFactory).unwrap();
    }

    /**
     * Wrapper of {@code Comparator<Runnable>} which unwrap {@link com.alibaba.ttl.TtlRunnable} before compare,
     * aka {@code TtlRunnableUnwrapComparator}.
     * <p>
     * Prepared for {@code comparator} parameter of constructor
     * {@link PriorityBlockingQueue#PriorityBlockingQueue(int, Comparator)}.
     * <p>
     * {@link PriorityBlockingQueue} can be used by constructor
     * {@link ThreadPoolExecutor#ThreadPoolExecutor(int, int, long, java.util.concurrent.TimeUnit, java.util.concurrent.BlockingQueue)}.
     *
     * @param comparator input comparator
     * @return wrapped comparator
     * @see ThreadPoolExecutor
     * @see ThreadPoolExecutor#ThreadPoolExecutor(int, int, long, java.util.concurrent.TimeUnit, java.util.concurrent.BlockingQueue)
     * @see PriorityBlockingQueue
     * @see PriorityBlockingQueue#PriorityBlockingQueue(int, Comparator)
     * @since 2.12.3
     */
    @Nullable
    @Contract("null -> null; !null -> !null")
    public static Comparator<Runnable> getTtlRunnableUnwrapComparator(@Nullable Comparator<Runnable> comparator) {
        if (comparator == null || isTtlRunnableUnwrapComparator(comparator)) return comparator;

        return new TtlUnwrapComparator<>(comparator);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final Comparator INSTANCE = new TtlUnwrapComparator(ComparableComparator.INSTANCE);

    /**
     * {@code TtlRunnableUnwrapComparator} that compares {@link Comparable Comparable} objects.
     *
     * @see #getTtlRunnableUnwrapComparator(Comparator)
     * @since 2.12.3
     */
    @NonNull
    @SuppressWarnings("unchecked")
    public static Comparator<Runnable> getTtlRunnableUnwrapComparatorForComparableRunnable() {
        return (Comparator<Runnable>) INSTANCE;
    }

    /**
     * check the {@code Comparator<Runnable>} is a wrapper {@code TtlRunnableUnwrapComparator} or not.
     *
     * @see #getTtlRunnableUnwrapComparator(Comparator)
     * @see #getTtlRunnableUnwrapComparatorForComparableRunnable()
     * @since 2.12.3
     */
    public static boolean isTtlRunnableUnwrapComparator(@Nullable Comparator<Runnable> comparator) {
        return comparator instanceof TtlUnwrapComparator;
    }

    /**
     * Unwrap {@code TtlRunnableUnwrapComparator} to the original/underneath {@code Comparator<Runnable>}.
     *
     * @see #getTtlRunnableUnwrapComparator(Comparator)
     * @see #getTtlRunnableUnwrapComparatorForComparableRunnable()
     * @see #isTtlRunnableUnwrapComparator(Comparator)
     * @see com.alibaba.ttl.TtlUnwrap#unwrap(Object)
     * @since 2.12.3
     */
    @Nullable
    @Contract("null -> null; !null -> !null")
    public static Comparator<Runnable> unwrap(@Nullable Comparator<Runnable> comparator) {
        if (!isTtlRunnableUnwrapComparator(comparator)) return comparator;

        return ((TtlUnwrapComparator<Runnable>) comparator).unwrap();
    }

    private TtlExecutors() {
        throw new InstantiationError("Must not instantiate this class");
    }
}
