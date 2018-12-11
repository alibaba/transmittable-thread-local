package com.alibaba.ttl

/**
 * Extension function wrap [Runnable] into [TtlRunnable].
 *
 * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if [TtlRunnable] is referred.
 * @param idempotent                       is idempotent mode or not. if `true`, just return input [Runnable] when it's [TtlRunnable],
 *                                         otherwise throw [IllegalStateException].
 *                                         **_Caution_**: `true` will cover up bugs! **DO NOT** set, only when you know why.
 * @return Wrapped [Runnable]
 *
 * @since 2.11.0
 */
fun Runnable.wrap(
    releaseTtlValueReferenceAfterRun: Boolean = false,
    idempotent: Boolean = false
): TtlRunnable = TtlRunnable.get(this, releaseTtlValueReferenceAfterRun, idempotent)!!

/**
 * Extension function wrap input [Runnable] Collection to [TtlRunnable] Collection.
 *
 * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if [TtlRunnable] is referred.
 * @param idempotent                       is idempotent mode or not. if {@code true}, just return input Runnable when it's [TtlRunnable],
 *                                         otherwise throw [IllegalStateException].
 *                                         **_Caution_**: `true` will cover up bugs! **DO NOT** set, only when you know why.
 * @return Wrapped list of [Runnable]
 *
 * @see Runnable.wrap
 * @since 2.11.0
 */
fun List<Runnable>.wrap(
    releaseTtlValueReferenceAfterRun: Boolean = false,
    idempotent: Boolean = false
): List<TtlRunnable> = map { it.wrap(releaseTtlValueReferenceAfterRun, idempotent) }

/**
 * Extension function to unwrap [TtlRunnable] to the original/underneath one.
 * <p>
 * if input `Runnable` parameter is not a [TtlRunnable] just return input `Runnable`.
 * <p>
 * so `Runnable.wrap().unwrap()` will always return the same input `Runnable` object.
 *
 * @since 2.11.0
 */
fun Runnable.unwrap(): Runnable = when (this) {
    is TtlRunnable -> runnable
    else -> this
}

/**
 * Extension function to unwrap [TtlRunnable] to the original/underneath one.
 * <p>
 * Invoke [Runnable.unwrap] for each element in collection.
 * <p>
 *
 * @see Runnable.unwrap
 * @since 2.11.0
 */
fun List<Runnable>.unwrap(): List<Runnable> = map { it.unwrap() }
