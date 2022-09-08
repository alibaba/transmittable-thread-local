package com.alibaba.ttl3.kotlin

import com.alibaba.ttl3.TtlCallable
import com.alibaba.ttl3.TtlRunnable
import com.alibaba.ttl3.executor.TtlExecutors
import java.util.concurrent.*
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory


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
 * @see TtlRunnable.unwrap
 */
fun Runnable.ttlUnwrap(): Runnable = TtlRunnable.unwrap(this) as Runnable

/**
 * wrap input [Runnable] Collection to [TtlRunnable] Collection.
 *
 * @see TtlRunnable.gets
 */
@JvmName("ttlWrapRunnableCollection")
fun Collection<Runnable>?.ttlWrap(
    releaseTtlValueReferenceAfterRun: Boolean = false,
    idempotent: Boolean = false
): List<TtlRunnable> = TtlRunnable.gets(this, releaseTtlValueReferenceAfterRun, idempotent)

/**
 * wrap input nullable [Runnable] Collection to [TtlRunnable] Collection.
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
 * @see TtlCallable.unwrap
 */
fun <V> Callable<V>.ttlUnwrap(): Callable<V> = TtlCallable.unwrap(this) as Callable<V>

/**
 * wrap input [Callable] Collection to [TtlCallable] Collection.
 *
 * @see TtlCallable.gets
 */
@JvmName("ttlWrapCallableCollection")
fun <V> Collection<Callable<V>>?.ttlWrap(
    releaseTtlValueReferenceAfterCall: Boolean = false,
    idempotent: Boolean = false
): List<TtlCallable<V>> = TtlCallable.gets(this, releaseTtlValueReferenceAfterCall, idempotent)

/**
 * wrap nullable input [Callable] Collection to [TtlCallable] Collection.
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
 * @see TtlExecutors.getDisableInheritableThreadFactory
 */
fun ThreadFactory.ttlWrapAsDisableInheritableThreadFactory(): ThreadFactory =
    TtlExecutors.getDisableInheritableThreadFactory(this) as ThreadFactory

/**
 * @see TtlExecutors.getDefaultDisableInheritableThreadFactory
 */
fun getDefaultDisableInheritableThreadFactory(): ThreadFactory =
    TtlExecutors.getDefaultDisableInheritableThreadFactory()

/**
 * @see TtlExecutors.isDisableInheritableThreadFactory
 */
fun ThreadFactory?.isDisableInheritableThreadFactory(): Boolean =
    TtlExecutors.isDisableInheritableThreadFactory(this)

/**
 * @see TtlExecutors.unwrapDisableInheritableThreadFactory
 */
fun ThreadFactory.ttlUnwrapDisableInheritableThreadFactory(): ThreadFactory =
    TtlExecutors.unwrapDisableInheritableThreadFactory(this) as ThreadFactory


/**
 * @see TtlExecutors.getDisableInheritableForkJoinWorkerThreadFactory
 */
fun ForkJoinWorkerThreadFactory.ttlWrapAsDisableInheritableForkJoinWorkerThreadFactory(): ForkJoinWorkerThreadFactory =
    TtlExecutors.getDisableInheritableForkJoinWorkerThreadFactory(this) as ForkJoinWorkerThreadFactory

/**
 * @see TtlExecutors.getDefaultDisableInheritableForkJoinWorkerThreadFactory
 */
fun getDefaultDisableInheritableForkJoinWorkerThreadFactory(): ForkJoinWorkerThreadFactory =
    TtlExecutors.getDefaultDisableInheritableForkJoinWorkerThreadFactory()

/**
 * @see TtlExecutors.isDisableInheritableForkJoinWorkerThreadFactory
 */
fun ForkJoinWorkerThreadFactory?.isDisableInheritableForkJoinWorkerThreadFactory(): Boolean =
    TtlExecutors.isDisableInheritableForkJoinWorkerThreadFactory(this)

/**
 * @see TtlExecutors.unwrapDisableInheritableForkJoinWorkerThreadFactory
 */
fun ForkJoinWorkerThreadFactory.ttlUnwrapDisableInheritableForkJoinWorkerThreadFactory(): ForkJoinWorkerThreadFactory =
    TtlExecutors.unwrapDisableInheritableForkJoinWorkerThreadFactory(this) as ForkJoinWorkerThreadFactory


////////////////////////////////////////
// Comparator
////////////////////////////////////////

/**
 * @see TtlExecutors.getTtlRunnableUnwrapComparator
 */
fun Comparator<Runnable>.ttlWrapAsTtlRunnableUnwrapComparator(): Comparator<Runnable> =
    TtlExecutors.getTtlRunnableUnwrapComparator(this) as Comparator<Runnable>

/**
 * @see TtlExecutors.getTtlRunnableUnwrapComparatorForComparableRunnable
 */
fun getTtlRunnableUnwrapComparatorForComparableRunnable(): Comparator<Runnable> =
    TtlExecutors.getTtlRunnableUnwrapComparatorForComparableRunnable()

/**
 * @see TtlExecutors.isTtlRunnableUnwrapComparator
 */
fun Comparator<Runnable>?.isTtlRunnableUnwrapComparator(): Boolean =
    TtlExecutors.isTtlRunnableUnwrapComparator(this)

/**
 * @see TtlExecutors.unwrapTtlRunnableUnwrapComparator
 */
fun Comparator<Runnable>.ttlUnwrapTtlRunnableUnwrapComparator(): Comparator<Runnable> =
    TtlExecutors.unwrapTtlRunnableUnwrapComparator(this) as Comparator<Runnable>
