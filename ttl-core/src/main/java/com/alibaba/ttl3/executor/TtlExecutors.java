package com.alibaba.ttl3.executor;

import com.alibaba.ttl3.TransmittableThreadLocal;
import com.alibaba.ttl3.agent.TtlAgentStatus;
import com.alibaba.ttl3.spi.TtlEnhanced;
import com.alibaba.ttl3.spi.TtlWrapper;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.jetbrains.annotations.Contract;

import java.util.Comparator;
import java.util.concurrent.*;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;

/**
 * Util methods for TTL wrapper of jdk executors.
 *
 * <ol>
 *     <li>wrap/check/unwrap methods for TTL wrapper of jdk executors({@link Executor}, {@link ExecutorService}, {@link ScheduledExecutorService}).</li>
 *     <li>wrap/check/unwrap methods for disable Inheritable wrapper of {@link ThreadFactory}.</li>
 *     <li>wrap/check/unwrap methods for disable Inheritable wrapper of {@link ForkJoinWorkerThreadFactory}.</li>
 *     <li>wrap/check/unwrap methods for {@code TtlRunnableUnwrapComparator} wrapper of {@link PriorityBlockingQueue}.</li>
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
 * @see ThreadFactory
 * @see Executors#defaultThreadFactory()
 * @see PriorityBlockingQueue
 * @see ForkJoinPool
 * @see ForkJoinWorkerThreadFactory
 * @see ForkJoinPool#defaultForkJoinWorkerThreadFactory
 */
public final class TtlExecutors {

    ///////////////////////////////////////////////////////////////////////////
    // Executor utils
    ///////////////////////////////////////////////////////////////////////////

    /**
     * {@link TransmittableThreadLocal} Wrapper of {@link Executor},
     * transmit the {@link TransmittableThreadLocal} from the task submit time of {@link Runnable}
     * to the execution time of {@link Runnable}.
     *
     * @param executor input Executor
     * @return wrapped Executor
     * @see com.alibaba.ttl3.TtlRunnable#get(Runnable, boolean, boolean)
     * @see com.alibaba.ttl3.TtlCallable#get(Callable, boolean, boolean)
     */
    @Nullable
    @Contract(value = "null -> null; !null -> !null", pure = true)
    public static Executor getTtlExecutor(@Nullable Executor executor) {
        if (TtlAgentStatus.getInstance().isTtlAgentLoaded() || null == executor || executor instanceof TtlEnhanced) {
            return executor;
        }
        return new ExecutorTtlWrapper(executor, true);
    }

    /**
     * {@link TransmittableThreadLocal} Wrapper of {@link ExecutorService},
     * transmit the {@link TransmittableThreadLocal} from the task submit time of {@link Runnable} or {@link Callable}
     * to the execution time of {@link Runnable} or {@link Callable}.
     *
     * @param executorService input ExecutorService
     * @return wrapped ExecutorService
     * @see com.alibaba.ttl3.TtlRunnable#get(Runnable, boolean, boolean)
     * @see com.alibaba.ttl3.TtlCallable#get(Callable, boolean, boolean)
     */
    @Nullable
    @Contract(value = "null -> null; !null -> !null", pure = true)
    public static ExecutorService getTtlExecutorService(@Nullable ExecutorService executorService) {
        if (TtlAgentStatus.getInstance().isTtlAgentLoaded() || executorService == null || executorService instanceof TtlEnhanced) {
            return executorService;
        }
        return new ExecutorServiceTtlWrapper(executorService, true);
    }


    /**
     * {@link TransmittableThreadLocal} Wrapper of {@link ScheduledExecutorService},
     * transmit the {@link TransmittableThreadLocal} from the task submit time of {@link Runnable} or {@link Callable}
     * to the execution time of {@link Runnable} or {@link Callable}.
     *
     * @param scheduledExecutorService input scheduledExecutorService
     * @return wrapped scheduledExecutorService
     * @see com.alibaba.ttl3.TtlRunnable#get(Runnable, boolean, boolean)
     * @see com.alibaba.ttl3.TtlCallable#get(Callable, boolean, boolean)
     */
    @Nullable
    @Contract(value = "null -> null; !null -> !null", pure = true)
    public static ScheduledExecutorService getTtlScheduledExecutorService(@Nullable ScheduledExecutorService scheduledExecutorService) {
        if (TtlAgentStatus.getInstance().isTtlAgentLoaded() || scheduledExecutorService == null || scheduledExecutorService instanceof TtlEnhanced) {
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
     * @see #unwrapTtlExecutor(Executor)
     */
    public static <T extends Executor> boolean isTtlExecutor(@Nullable T executor) {
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
     * @see #isTtlExecutor(Executor)
     * @see com.alibaba.ttl3.TtlWrappers#unwrap(Object)
     */
    @Nullable
    @Contract(value = "null -> null; !null -> !null", pure = true)
    @SuppressWarnings("unchecked")
    public static <T extends Executor> T unwrapTtlExecutor(@Nullable T executor) {
        if (!isTtlExecutor(executor)) return executor;

        return (T) ((ExecutorTtlWrapper) executor).unwrap();
    }

    /**
     * Wrapper of {@link ThreadFactory}, disable inheritable.
     *
     * @param threadFactory input thread factory
     * @see TtlExecutors#getDisableInheritableForkJoinWorkerThreadFactory
     */
    @Nullable
    @Contract(value = "null -> null; !null -> !null", pure = true)
    public static ThreadFactory getDisableInheritableThreadFactory(@Nullable ThreadFactory threadFactory) {
        if (threadFactory == null || isDisableInheritableThreadFactory(threadFactory)) return threadFactory;

        return new DisableInheritableThreadFactoryWrapper(threadFactory);
    }

    /**
     * Wrapper of {@link Executors#defaultThreadFactory()}, disable inheritable.
     *
     * @see #getDisableInheritableThreadFactory(ThreadFactory)
     * @see TtlExecutors#getDefaultDisableInheritableForkJoinWorkerThreadFactory()
     */
    @NonNull
    public static ThreadFactory getDefaultDisableInheritableThreadFactory() {
        return getDisableInheritableThreadFactory(Executors.defaultThreadFactory());
    }

    /**
     * check the {@link ThreadFactory} is {@code DisableInheritableThreadFactory} or not.
     *
     * @see TtlExecutors#getDisableInheritableForkJoinWorkerThreadFactory
     * @see #getDefaultDisableInheritableThreadFactory()
     */
    public static boolean isDisableInheritableThreadFactory(@Nullable ThreadFactory threadFactory) {
        return threadFactory instanceof DisableInheritableThreadFactoryWrapper;
    }

    /**
     * Unwrap {@code DisableInheritableThreadFactory} to the original/underneath one.
     *
     * @see #getDisableInheritableThreadFactory(ThreadFactory)
     * @see #getDefaultDisableInheritableThreadFactory()
     * @see #isDisableInheritableThreadFactory(ThreadFactory)
     * @see TtlExecutors#unwrapDisableInheritableForkJoinWorkerThreadFactory(ForkJoinWorkerThreadFactory)
     * @see com.alibaba.ttl3.TtlWrappers#unwrap(Object)
     */
    @Nullable
    @Contract(value = "null -> null; !null -> !null", pure = true)
    public static ThreadFactory unwrapDisableInheritableThreadFactory(@Nullable ThreadFactory threadFactory) {
        if (!isDisableInheritableThreadFactory(threadFactory)) return threadFactory;

        return ((DisableInheritableThreadFactoryWrapper) threadFactory).unwrap();
    }

    ///////////////////////////////////////////////////////////////////////////
    // ForkJoinPool utils
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Wrapper of {@link ForkJoinWorkerThreadFactory}, disable inheritable.
     *
     * @param threadFactory input thread factory
     * @see TtlExecutors#getDisableInheritableThreadFactory(ThreadFactory)
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
     */
    @NonNull
    public static ForkJoinWorkerThreadFactory getDefaultDisableInheritableForkJoinWorkerThreadFactory() {
        return getDisableInheritableForkJoinWorkerThreadFactory(ForkJoinPool.defaultForkJoinWorkerThreadFactory);
    }

    /**
     * check the {@link ForkJoinWorkerThreadFactory} is {@code DisableInheritableForkJoinWorkerThreadFactory} or not.
     *
     * @see #getDisableInheritableForkJoinWorkerThreadFactory(ForkJoinWorkerThreadFactory)
     * @see #getDefaultDisableInheritableForkJoinWorkerThreadFactory()
     */
    public static boolean isDisableInheritableForkJoinWorkerThreadFactory(@Nullable ForkJoinWorkerThreadFactory threadFactory) {
        return threadFactory instanceof DisableInheritableForkJoinWorkerThreadFactoryWrapper;
    }

    /**
     * Unwrap {@code DisableInheritableForkJoinWorkerThreadFactory} to the original/underneath one.
     *
     * @see com.alibaba.ttl3.TtlWrappers#unwrap(Object)
     * @see TtlExecutors#unwrapDisableInheritableThreadFactory(ThreadFactory)
     */
    @Nullable
    @Contract(value = "null -> null; !null -> !null", pure = true)
    public static ForkJoinWorkerThreadFactory unwrapDisableInheritableForkJoinWorkerThreadFactory(@Nullable ForkJoinWorkerThreadFactory threadFactory) {
        if (!isDisableInheritableForkJoinWorkerThreadFactory(threadFactory)) return threadFactory;

        return ((DisableInheritableForkJoinWorkerThreadFactoryWrapper) threadFactory).unwrap();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Comparator utils
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Wrapper of {@code Comparator<Runnable>} which unwrap {@link com.alibaba.ttl3.TtlRunnable} before compare,
     * aka {@code TtlRunnableUnwrapComparator}.
     * <p>
     * Prepared for {@code comparator} parameter of constructor
     * {@link PriorityBlockingQueue#PriorityBlockingQueue(int, Comparator)}.
     * <p>
     * {@link PriorityBlockingQueue} can be used by constructor
     * {@link ThreadPoolExecutor#ThreadPoolExecutor(int, int, long, TimeUnit, BlockingQueue)}.
     *
     * @param comparator input comparator
     * @return wrapped comparator
     * @see ThreadPoolExecutor
     * @see ThreadPoolExecutor#ThreadPoolExecutor(int, int, long, TimeUnit, BlockingQueue)
     * @see PriorityBlockingQueue
     * @see PriorityBlockingQueue#PriorityBlockingQueue(int, Comparator)
     */
    @Nullable
    @Contract(value = "null -> null; !null -> !null", pure = true)
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
     * @see com.alibaba.ttl3.TtlWrappers#unwrap(Object)
     */
    @Nullable
    @Contract(value = "null -> null; !null -> !null", pure = true)
    public static Comparator<Runnable> unwrapTtlRunnableUnwrapComparator(@Nullable Comparator<Runnable> comparator) {
        if (!isTtlRunnableUnwrapComparator(comparator)) return comparator;

        return ((TtlUnwrapComparator<Runnable>) comparator).unwrap();
    }

    private TtlExecutors() {
        throw new InstantiationError("Must not instantiate this class");
    }
}
