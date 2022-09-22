package com.alibaba.ttl3.kotlin

import com.alibaba.ttl3.TtlCallable
import com.alibaba.ttl3.TtlRunnable
import com.alibaba.ttl3.TtlWrappers
import com.alibaba.ttl3.executor.TtlExecutors
import com.alibaba.ttl3.spi.TtlEnhanced
import com.alibaba.ttl3.spi.TtlWrapper
import com.alibaba.ttl3.transmitter.Transmitter
import java.util.concurrent.*
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory
import java.util.function.*
import java.util.function.Function


////////////////////////////////////////
// Runnable
////////////////////////////////////////

/**
 * wrap input [Runnable] to [TtlRunnable].
 *
 * @see TtlRunnable.get
 */
fun Runnable.ttlWrap(
    releaseTtlValueReferenceAfterRun: Boolean = false,
    idempotent: Boolean = false
): TtlRunnable = TtlRunnable.get(this, releaseTtlValueReferenceAfterRun, idempotent) as TtlRunnable

/**
 * unwrap [TtlRunnable] to the original/underneath one.
 *
 * @see TtlRunnable.ttlUnwrap
 */
fun Runnable.ttlUnwrap(): Runnable = TtlRunnable.unwrap(this) as Runnable

/**
 * wrap input [Runnable] collection to [TtlRunnable] collection.
 *
 * @see TtlRunnable.gets
 */
@JvmName("ttlWrapRunnableCollection")
fun Collection<Runnable>?.ttlWrap(
    releaseTtlValueReferenceAfterRun: Boolean = false,
    idempotent: Boolean = false
): List<TtlRunnable> = TtlRunnable.gets(this, releaseTtlValueReferenceAfterRun, idempotent)

/**
 * wrap input nullable [Runnable] collection to [TtlRunnable] collection.
 *
 * @see TtlRunnable.gets
 */
@JvmName("ttlWrapNullableRunnableCollection")
fun Collection<Runnable?>?.ttlWrap(
    releaseTtlValueReferenceAfterRun: Boolean = false,
    idempotent: Boolean = false
): List<TtlRunnable?> = TtlRunnable.gets(this, releaseTtlValueReferenceAfterRun, idempotent)

/**
 * unwrap [TtlRunnable] to the original/underneath one for collection.
 *
 * @see TtlRunnable.unwraps
 */
@JvmName("ttlUnwrapRunnableCollection")
fun Collection<Runnable>?.ttlUnwrap(): List<Runnable> =
    TtlRunnable.unwraps(this)

/**
 * unwrap nullable [TtlRunnable] to the original/underneath one for collection.
 *
 * @see TtlRunnable.unwraps
 */
@JvmName("ttlUnwrapNullableRunnableCollection")
fun Collection<Runnable?>?.ttlUnwrap(): List<Runnable?> =
    TtlRunnable.unwraps(this)


////////////////////////////////////////
// Callable
////////////////////////////////////////

/**
 * wrap input [Callable] to [TtlCallable].
 *
 * @see TtlCallable.get
 */
fun <V> Callable<V>.ttlWrap(
    releaseTtlValueReferenceAfterCall: Boolean = false,
    idempotent: Boolean = false
): TtlCallable<V> = TtlCallable.get(this, releaseTtlValueReferenceAfterCall, idempotent) as TtlCallable<V>

/**
 * unwrap [TtlCallable] to the original/underneath one.
 *
 * @see TtlCallable.ttlUnwrap
 */
fun <V> Callable<V>.ttlUnwrap(): Callable<V> = TtlCallable.unwrap(this) as Callable<V>

/**
 * wrap input [Callable] collection to [TtlCallable] collection.
 *
 * @see TtlCallable.gets
 */
@JvmName("ttlWrapCallableCollection")
fun <V> Collection<Callable<V>>?.ttlWrap(
    releaseTtlValueReferenceAfterCall: Boolean = false,
    idempotent: Boolean = false
): List<TtlCallable<V>> = TtlCallable.gets(this, releaseTtlValueReferenceAfterCall, idempotent)

/**
 * wrap nullable input [Callable] collection to [TtlCallable] collection.
 *
 * @see TtlCallable.gets
 */
@JvmName("ttlWrapNullableCallableCollection")
fun <V> Collection<Callable<V>?>?.ttlWrap(
    releaseTtlValueReferenceAfterCall: Boolean = false,
    idempotent: Boolean = false
): List<TtlCallable<V>?> = TtlCallable.gets(this, releaseTtlValueReferenceAfterCall, idempotent)

/**
 * unwrap [TtlCallable] to the original/underneath one for collection.
 *
 * @see TtlCallable.unwraps
 */
@JvmName("ttlUnwrapCallableCollection")
fun <V> Collection<Callable<V>>?.ttlUnwrap(): List<Callable<V>> =
    TtlCallable.unwraps(this)

/**
 * unwrap nullable [TtlCallable] to the original/underneath one for collection.
 *
 * @see TtlCallable.unwraps
 */
@JvmName("ttlUnwrapNullableCallableCollection")
fun <V> Collection<Callable<V>?>?.ttlUnwrap(): List<Callable<V>?> =
    TtlCallable.unwraps(this)


////////////////////////////////////////
// java common functional interface
////////////////////////////////////////

/**
 * wrap [Supplier] to TTL wrapper.
 *
 * @see TtlWrappers.wrapSupplier
 */
fun <T> Supplier<T>.ttlWrap(): Supplier<T> = TtlWrappers.wrapSupplier(this) as Supplier<T>

/**
 * wrap [Consumer] to TTL wrapper.
 *
 * @see TtlWrappers.wrapConsumer
 */
fun <T> Consumer<T>.ttlWrap(): Consumer<T> = TtlWrappers.wrapConsumer(this) as Consumer<T>

/**
 * wrap [BiConsumer] to TTL wrapper.
 *
 * @see TtlWrappers.wrapBiConsumer
 */
fun <T, U> BiConsumer<T, U>.ttlWrap(): BiConsumer<T, U> = TtlWrappers.wrapBiConsumer(this) as BiConsumer<T, U>

/**
 * wrap [Function] to TTL wrapper.
 *
 * @see TtlWrappers.wrapFunction
 */
fun <T, R> Function<T, R>.ttlWrap(): Function<T, R> =
    TtlWrappers.wrapFunction(this) as Function<T, R>

/**
 * wrap [BiFunction] to TTL wrapper.
 *
 * @see TtlWrappers.wrapFunction
 */
fun <T, U, R> BiFunction<T, U, R>.ttlWrap(): BiFunction<T, U, R> =
    TtlWrappers.wrapBiFunction(this) as BiFunction<T, U, R>


////////////////////////////////////////
// kotlin function types
////////////////////////////////////////

/**
 * wrap to TTL wrapper.
 */
fun <R> (() -> R).ttlWrap(): () -> R {
    if (this is TtlEnhanced) return this

    val captured = Transmitter.capture()

    return object : () -> R, TtlWrapper<() -> R> {
        override fun unwrap(): () -> R = this@ttlWrap

        override fun invoke(): R {
            val backup = Transmitter.replay(captured)
            try {
                return this@ttlWrap.invoke()
            } finally {
                Transmitter.restore(backup)
            }
        }
    }
}

/**
 * wrap to TTL wrapper.
 */
fun <P1, R> ((P1) -> R).ttlWrap(): (P1) -> R {
    if (this is TtlEnhanced) return this

    val captured = Transmitter.capture()

    return object : (P1) -> R, TtlWrapper<(P1) -> R> {
        override fun unwrap(): (P1) -> R = this@ttlWrap

        override fun invoke(arg: P1): R {
            val backup = Transmitter.replay(captured)
            try {
                return this@ttlWrap.invoke(arg)
            } finally {
                Transmitter.restore(backup)
            }
        }
    }
}

/**
 * wrap to TTL wrapper.
 */
fun <P1, P2, R> ((P1, P2) -> R).ttlWrap(): (P1, P2) -> R {
    if (this is TtlEnhanced) return this

    val captured = Transmitter.capture()

    return object : (P1, P2) -> R, TtlWrapper<(P1, P2) -> R> {
        override fun unwrap(): (P1, P2) -> R = this@ttlWrap

        override fun invoke(arg1: P1, arg2: P2): R {
            val backup = Transmitter.replay(captured)
            try {
                return this@ttlWrap.invoke(arg1, arg2)
            } finally {
                Transmitter.restore(backup)
            }
        }
    }
}

/**
 * wrap to TTL wrapper.
 */
fun <P1, P2, P3, R> ((P1, P2, P3) -> R).ttlWrap(): (P1, P2, P3) -> R {
    if (this is TtlEnhanced) return this

    val captured = Transmitter.capture()

    return object : (P1, P2, P3) -> R, TtlWrapper<(P1, P2, P3) -> R> {
        override fun unwrap(): (P1, P2, P3) -> R = this@ttlWrap

        override fun invoke(arg1: P1, arg2: P2, arg3: P3): R {
            val backup = Transmitter.replay(captured)
            try {
                return this@ttlWrap.invoke(arg1, arg2, arg3)
            } finally {
                Transmitter.restore(backup)
            }
        }
    }
}

/**
 * wrap to TTL wrapper.
 */
fun <P1, P2, P3, P4, R> ((P1, P2, P3, P4) -> R).ttlWrap(): (P1, P2, P3, P4) -> R {
    if (this is TtlEnhanced) return this

    val captured = Transmitter.capture()

    return object : (P1, P2, P3, P4) -> R, TtlWrapper<(P1, P2, P3, P4) -> R> {
        override fun unwrap(): (P1, P2, P3, P4) -> R = this@ttlWrap

        override fun invoke(arg1: P1, arg2: P2, arg3: P3, arg4: P4): R {
            val backup = Transmitter.replay(captured)
            try {
                return this@ttlWrap.invoke(arg1, arg2, arg3, arg4)
            } finally {
                Transmitter.restore(backup)
            }
        }
    }
}


////////////////////////////////////////
// Generic unwrap/check method
////////////////////////////////////////

/**
 * generic unwrap method, unwrap [TtlWrapper] to the original/underneath one.
 *
 * if input parameter is not a [TtlWrapper] just return input.
 *
 * @see TtlWrappers.unwrap
 */
@Suppress("UNCHECKED_CAST")
fun <T> T.ttlUnwrap(): T = TtlWrappers.unwrap(this) as T

/**
 * check the input object is a `TtlWrapper` or not.
 *
 * @see TtlWrappers.isWrapper
 */
fun <T> T.isTtlWrapper(): Boolean = TtlWrappers.isWrapper(this)


////////////////////////////////////////
// Executor
////////////////////////////////////////

/**
 * wrap input [Executor] to `TtlExecutor`.
 *
 * @see TtlExecutors.getTtlExecutor
 */
fun Executor.ttlWrap(): Executor =
    TtlExecutors.getTtlExecutor(this) as Executor

/**
 * wrap input [ExecutorService] to `TtlExecutorService`.
 *
 * @see TtlExecutors.getTtlExecutorService
 */
fun ExecutorService.ttlWrap(): ExecutorService =
    TtlExecutors.getTtlExecutorService(this) as ExecutorService

/**
 * wrap input [ScheduledExecutorService] to `TtlScheduledExecutorService`.
 *
 * @see TtlExecutors.getTtlScheduledExecutorService
 */
fun ScheduledExecutorService.ttlWrap(): ScheduledExecutorService =
    TtlExecutors.getTtlScheduledExecutorService(this) as ScheduledExecutorService

/**
 * check the executor is a TTL executor wrapper or not.
 */
fun Executor?.isTtlExecutor(): Boolean = TtlExecutors.isTtlExecutor(this)

/**
 * unwrap `TtlExecutor` to the original/underneath one.
 *
 * @see TtlExecutors.unwrapTtlExecutor
 */
fun <E : Executor> E.ttlUnwrap(): E = TtlExecutors.unwrapTtlExecutor(this) as E


////////////////////////////////////////
// Thread Factory
////////////////////////////////////////

/**
 * wrapper of [ThreadFactory], disable inheritable.
 *
 * @see TtlExecutors.getDisableInheritableThreadFactory
 */
fun ThreadFactory.ttlWrapToDisableInheritableThreadFactory(): ThreadFactory =
    TtlExecutors.getDisableInheritableThreadFactory(this) as ThreadFactory

/**
 * wrapper of [Executors.defaultThreadFactory], disable inheritable.
 *
 * @see TtlExecutors.getDefaultDisableInheritableThreadFactory
 */
fun getDefaultDisableInheritableThreadFactory(): ThreadFactory =
    TtlExecutors.getDefaultDisableInheritableThreadFactory()

/**
 * check the [ThreadFactory] is `DisableInheritableThreadFactory` or not.
 *
 * @see TtlExecutors.isDisableInheritableThreadFactory
 */
fun ThreadFactory?.isDisableInheritableThreadFactory(): Boolean =
    TtlExecutors.isDisableInheritableThreadFactory(this)

/**
 * unwrap `DisableInheritableThreadFactory` to the original/underneath one.
 *
 * @see TtlExecutors.unwrapDisableInheritableThreadFactory
 */
fun ThreadFactory.ttlUnwrapDisableInheritableThreadFactory(): ThreadFactory =
    TtlExecutors.unwrapDisableInheritableThreadFactory(this) as ThreadFactory


/**
 * wrapper of [ForkJoinWorkerThreadFactory], disable inheritable.
 *
 * @see TtlExecutors.getDisableInheritableForkJoinWorkerThreadFactory
 */
fun ForkJoinWorkerThreadFactory.ttlWrapToDisableInheritableForkJoinWorkerThreadFactory(): ForkJoinWorkerThreadFactory =
    TtlExecutors.getDisableInheritableForkJoinWorkerThreadFactory(this) as ForkJoinWorkerThreadFactory

/**
 * wrapper of [ForkJoinPool.defaultForkJoinWorkerThreadFactory], disable inheritable.
 *
 * @see TtlExecutors.getDefaultDisableInheritableForkJoinWorkerThreadFactory
 */
fun getDefaultDisableInheritableForkJoinWorkerThreadFactory(): ForkJoinWorkerThreadFactory =
    TtlExecutors.getDefaultDisableInheritableForkJoinWorkerThreadFactory()

/**
 * check the [ForkJoinWorkerThreadFactory] is `DisableInheritableForkJoinWorkerThreadFactory` or not.
 *
 * @see TtlExecutors.isDisableInheritableForkJoinWorkerThreadFactory
 */
fun ForkJoinWorkerThreadFactory?.isDisableInheritableForkJoinWorkerThreadFactory(): Boolean =
    TtlExecutors.isDisableInheritableForkJoinWorkerThreadFactory(this)

/**
 * unwrap `DisableInheritableForkJoinWorkerThreadFactory` to the original/underneath one.
 *
 * @see TtlExecutors.unwrapDisableInheritableForkJoinWorkerThreadFactory
 */
fun ForkJoinWorkerThreadFactory.ttlUnwrapDisableInheritableForkJoinWorkerThreadFactory(): ForkJoinWorkerThreadFactory =
    TtlExecutors.unwrapDisableInheritableForkJoinWorkerThreadFactory(this) as ForkJoinWorkerThreadFactory


////////////////////////////////////////
// Comparator
////////////////////////////////////////

/**
 * wrapper of `Comparator<Runnable>` which unwrap [com.alibaba.ttl3.TtlRunnable] before compare,
 * aka `TtlRunnableUnwrapComparator`.
 *
 * @see TtlExecutors.getTtlRunnableUnwrapComparator
 */
fun Comparator<Runnable>.ttlWrapToTtlRunnableUnwrapComparator() =
    TtlExecutors.getTtlRunnableUnwrapComparator(this) as Comparator<Runnable>

/**
 * `TtlRunnableUnwrapComparator` that compares {@link Comparable Comparable} objects.
 *
 * @see TtlExecutors.getTtlRunnableUnwrapComparatorForComparableRunnable
 */
fun getTtlRunnableUnwrapComparatorForComparableRunnable(): Comparator<Runnable> =
    TtlExecutors.getTtlRunnableUnwrapComparatorForComparableRunnable()

/**
 * check the `Comparator<Runnable>` is a wrapper `TtlRunnableUnwrapComparator` or not.
 *
 * @see TtlExecutors.isTtlRunnableUnwrapComparator
 */
fun Comparator<Runnable>?.isTtlRunnableUnwrapComparator(): Boolean =
    TtlExecutors.isTtlRunnableUnwrapComparator(this)

/**
 * unwrap `TtlRunnableUnwrapComparator` to the original/underneath `Comparator<Runnable>`.
 *
 * @see TtlExecutors.unwrapTtlRunnableUnwrapComparator
 */
fun Comparator<Runnable>.ttlUnwrapTtlRunnableUnwrapComparator(): Comparator<Runnable> =
    TtlExecutors.unwrapTtlRunnableUnwrapComparator(this) as Comparator<Runnable>
