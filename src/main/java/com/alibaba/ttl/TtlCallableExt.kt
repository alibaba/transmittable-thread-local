package com.alibaba.ttl

import java.util.concurrent.Callable

@Throws(Exception::class)
operator fun <V> TtlCallable<V>.invoke(): V = call()

/**
 * Extension function wrap {@link Callable} into {@link TtlCallable}.
 * <p>
 *
 * @param releaseTtlValueReferenceAfterCall release TTL value reference after run, avoid memory leak even if {@link TtlRunnable} is referred.
 * @param idempotent                        is idempotent or not. {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
 * @return Wrapped {@link Callable}
 *
 * * @since TODO
 */
fun <V> Callable<V>.wrap(
    releaseTtlValueReferenceAfterCall: Boolean = false,
    idempotent: Boolean = false
): TtlCallable<V> = TtlCallable.get(this, releaseTtlValueReferenceAfterCall, idempotent)!!

/**
 * Extension function wrap input {@link Callable} Collection to {@link TtlCallable} Collection.
 *
 * @param releaseTtlValueReferenceAfterCall release TTL value reference after run, avoid memory leak even if {@link TtlRunnable} is referred.
 * @param idempotent                        is idempotent or not. {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
 * @return Wrapped list of {@link Callable}
 *
 * @see #Callable::wrap
 * @since TODO
 */
fun <V> List<Callable<V>>.wrap(
    releaseTtlValueReferenceAfterCall: Boolean = false,
    idempotent: Boolean = false
): List<TtlCallable<V>> = map { it.wrap(releaseTtlValueReferenceAfterCall, idempotent) }

/**
 * Extension function to unwrap {@link TtlCallable} to the original/underneath one.
 * <p>
 * if input {@code Callable} parameter is not a {@link TtlCallable} just return input {@code Callable}.
 * <p>
 * so {@code callable.wrap().unwrap()} will always return the same input {@code callable} object.
 *
 * @since TODO
 */
fun <V> Callable<V>.unwrap(): Callable<V> = when(this) {
    is TtlCallable<V> -> getCallable()
    else -> this
}

/**
 * Extension function to unwrap {@link TtlCallable} to the original/underneath one.
 * <p>
 * Invoke {@link #unwrap(Callable)} for each element in collection.
 * <p>
 *
 * @see #Callable::unwrap
 * @since TODO
 */
fun <V> List<Callable<V>>.unwrap() : List<Callable<V>> = map { it.unwrap() }
