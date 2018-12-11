package com.alibaba.ttl

import java.util.concurrent.Callable


/**
 * Invoke operator for [TtlCallable].
 *
 * @since 2.11.0
 */
@Throws(Exception::class)
operator fun <V> TtlCallable<V>.invoke(): V = call()

/**
 * Extension function wrap [Callable] into [TtlCallable].
 * <p>
 *
 * @param releaseTtlValueReferenceAfterCall release TTL value reference after run, avoid memory leak even if [TtlCallable] is referred.
 * @param idempotent                        is idempotent or not. `true` will cover up bugs! **DO NOT** set, only when you know why.
 * @return Wrapped [Callable]
 *
 * @since 2.11.0
 */
fun <V> Callable<V>.wrapTtl(
    releaseTtlValueReferenceAfterCall: Boolean = false,
    idempotent: Boolean = false
): TtlCallable<V> = TtlCallable.get(this, releaseTtlValueReferenceAfterCall, idempotent)!!

/**
 * Extension function wrap input [Callable] Collection to [TtlCallable] Collection.
 *
 * @param releaseTtlValueReferenceAfterCall release TTL value reference after run, avoid memory leak even if [TtlRunnable] is referred.
 * @param idempotent                        is idempotent or not. `true` will cover up bugs! **DO NOT** set, only when you know why.
 * @return Wrapped list of [Callable]
 *
 * @see Callable.wrapTtl
 * @since 2.11.0
 */
fun <V> List<Callable<V>>.wrapTtl(
    releaseTtlValueReferenceAfterCall: Boolean = false,
    idempotent: Boolean = false
): List<TtlCallable<V>> = map { it.wrapTtl(releaseTtlValueReferenceAfterCall, idempotent) }

/**
 * Extension function to unwrap [TtlCallable] to the original/underneath one.
 * <p>
 * if input [Callable] parameter is not a [TtlCallable] just return input [Callable].
 * <p>
 * so `callable.wrapTtl().unwrapTtl()` will always return the same input [Callable] object.
 *
 * @since 2.11.0
 */
fun <V> Callable<V>.unwrapTtl(): Callable<V> = when(this) {
    is TtlCallable<V> -> getCallable()
    else -> this
}

/**
 * Extension function to unwrap [TtlCallable] to the original/underneath one.
 * <p>
 * Invoke [unwrapTtl] for each element in collection.
 * <p>
 *
 * @see Callable.unwrapTtl
 * @since 2.11.0
 */
fun <V> List<Callable<V>>.unwrapTtl() : List<Callable<V>> = map { it.unwrapTtl() }
