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
fun Runnable.wrapTtl(
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
 * @see Runnable.wrapTtl
 * @since 2.11.0
 */
fun List<Runnable>.wrapTtl(
    releaseTtlValueReferenceAfterRun: Boolean = false,
    idempotent: Boolean = false
): List<TtlRunnable> = map { it.wrapTtl(releaseTtlValueReferenceAfterRun, idempotent) }

/**
 * Extension function to unwrap [TtlRunnable] to the original/underneath one.
 * <p>
 * if input `Runnable` parameter is not a [TtlRunnable] just return input `Runnable`.
 * <p>
 * so `Runnable.wrapTtl().unwrapTtl()` will always return the same input `Runnable` object.
 *
 * @since 2.11.0
 */
fun Runnable.unwrapTtl(): Runnable = when (this) {
    is TtlRunnable -> runnable
    else -> this
}

/**
 * Extension function to unwrap [TtlRunnable] to the original/underneath one.
 * <p>
 * Invoke [Runnable.unwrapTtl] for each element in collection.
 * <p>
 *
 * @see Runnable.unwrapTtl
 * @since 2.11.0
 */
fun List<Runnable>.unwrapTtl(): List<Runnable> = map { it.unwrapTtl() }
