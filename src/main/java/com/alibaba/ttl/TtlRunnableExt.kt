package com.alibaba.ttl

/**
 * Extension function wrap {@link Runnable} into {@link TtlRunnable}.
 * <p>
 *
 * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlRunnable} is referred.
 * @param idempotent                       is idempotent mode or not. if {@code true}, just return input {@link Runnable} when it's {@link TtlRunnable},
 *                                         otherwise throw {@link IllegalStateException}.
 *                                         <B><I>Caution</I></B>: {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
 * @return Wrapped {@link Runnable}
 *
 * * @since TODO
 */
fun Runnable.wrap(
    releaseTtlValueReferenceAfterRun: Boolean = false,
    idempotent: Boolean = false
): TtlRunnable = TtlRunnable.get(this, releaseTtlValueReferenceAfterRun, idempotent)!!

/**
 * Extension function wrap input {@link Runnable} Collection to {@link TtlRunnable} Collection.
 *
 * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlRunnable} is referred.
 * @param idempotent                       is idempotent mode or not. if {@code true}, just return input {@link Runnable} when it's {@link TtlRunnable},
 *                                         otherwise throw {@link IllegalStateException}.
 *                                         <B><I>Caution</I></B>: {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
 * @return Wrapped list of {@link Runnable}
 *
 * @see #Runnable::wrap
 * @since TODO
 */
fun List<Runnable>.wrap(
    releaseTtlValueReferenceAfterRun: Boolean = false,
    idempotent: Boolean = false
): List<TtlRunnable> = map { it.wrap(releaseTtlValueReferenceAfterRun, idempotent) }

/**
 * Extension function to unwrap {@link TtlRunnable} to the original/underneath one.
 * <p>
 * if input {@code Runnable} parameter is not a {@link TtlRunnable} just return input {@code Runnable}.
 * <p>
 * so {@code Runnable.wrap().unwrap()} will always return the same input {@code Runnable} object.
 *
 * @since TODO
 */
fun Runnable.unwrap(): Runnable = when (this) {
    is TtlRunnable -> runnable
    else -> this
}

/**
 * Extension function to unwrap {@link TtlRunnable} to the original/underneath one.
 * <p>
 * Invoke {@link #unwrap(Runnable)} for each element in collection.
 * <p>
 *
 * @see #Runnable::unwrap
 * @since TODO
 */
fun List<Runnable>.unwrap(): List<Runnable> = map { it.unwrap() }
