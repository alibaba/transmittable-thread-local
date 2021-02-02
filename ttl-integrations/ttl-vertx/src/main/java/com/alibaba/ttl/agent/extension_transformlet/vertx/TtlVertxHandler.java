package com.alibaba.ttl.agent.extension_transformlet.vertx;

import com.alibaba.ttl.spi.TtlAttachments;
import com.alibaba.ttl.spi.TtlAttachmentsDelegate;
import com.alibaba.ttl.spi.TtlEnhanced;
import com.alibaba.ttl.spi.TtlWrapper;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.vertx.core.Handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.alibaba.ttl.TransmittableThreadLocal.Transmitter.*;

/**
 * {@link TtlVertxHandler} decorate {@link Handler}, so as to get {@link com.alibaba.ttl.TransmittableThreadLocal}
 * and transmit it to the time of {@link Handler} execution,
 * needed when use {@link Handler} to {@link io.vertx.core.Future}.
 * <p>
 * we will capture ttl value in another thread by modify {@link io.netty.util.concurrent.SingleThreadEventExecutor#execute(Runnable)},
 * but we can not capture the ttl value which we expect in callback of identical thread.
 * the reason of above issue is some async io callback was invoked by the
 * {@link io.netty.channel.nio.NioEventLoop#run()} rather than the {@link com.alibaba.ttl.TtlRunnable#run()}
 *
 * @author tk (305809299 at qq dot com)
 * @see io.netty.channel.nio.NioEventLoop#run()
 * @see io.netty.channel.nio.NioEventLoop#processSelectedKeys()
 * @see io.vertx.core.Future
 * @see com.alibaba.ttl.TransmittableThreadLocal.Transmitter#restore(Object)
 */
public class TtlVertxHandler<E> implements Handler<E>, TtlWrapper<Handler<E>>, TtlEnhanced, TtlAttachments {
    private final AtomicReference<Object> capturedRef;
    private final Handler<E> handler;
    private final boolean releaseTtlValueReferenceAfterRun;

    private TtlVertxHandler(@NonNull Handler<E> handler, boolean releaseTtlValueReferenceAfterRun) {
        this.capturedRef = new AtomicReference<>(capture());
        this.handler = handler;
        this.releaseTtlValueReferenceAfterRun = releaseTtlValueReferenceAfterRun;
    }

    /**
     * wrap method {@link Handler#handle(E)}.
     */
    @Override
    public void handle(E event) {
        final Object captured = capturedRef.get();
        if (captured == null || releaseTtlValueReferenceAfterRun && !capturedRef.compareAndSet(captured, null)) {
            throw new IllegalStateException("TTL value reference is released after run!");
        }

        final Object backup = replay(captured);
        try {
            handler.handle(event);
        } finally {
            restore(backup);
        }
    }

    /**
     * return original/unwrapped {@link Handler}.
     */
    @NonNull
    public Handler<E> getHandler() {
        return unwrap();
    }

    /**
     * unwrap to original/unwrapped {@link Handler<E>}.
     *
     * @see com.alibaba.ttl.TtlUnwrap#unwrap(Object)
     */
    @NonNull
    @Override
    public Handler<E> unwrap() {
        return handler;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        @SuppressWarnings("unchecked")
        TtlVertxHandler<E> that = (TtlVertxHandler<E>) o;

        return handler.equals(that.handler);
    }

    @Override
    public int hashCode() {
        return handler.hashCode();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " - " + handler.toString();
    }

    /**
     * Factory method, wrap input {@link Handler} to {@link TtlVertxHandler}.
     *
     * @param handler input {@link Handler}. if input is {@code null}, return {@code null}.
     * @return Wrapped {@link Handler}
     * @throws IllegalStateException when input is {@link TtlVertxHandler} already.
     */
    @Nullable
    public static <T> TtlVertxHandler<T> get(@Nullable Handler<T> handler) {
        return get(handler, false, false);
    }

    /**
     * Factory method, wrap input {@link Handler} to {@link TtlVertxHandler}.
     *
     * @param handler                          input {@link Handler}. if input is {@code null}, return {@code null}.
     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlVertxHandler} is referred.
     * @return Wrapped {@link Handler}
     * @throws IllegalStateException when input is {@link TtlVertxHandler} already.
     */
    @Nullable
    public static <T> TtlVertxHandler<T> get(@Nullable Handler<T> handler, boolean releaseTtlValueReferenceAfterRun) {
        return get(handler, releaseTtlValueReferenceAfterRun, false);
    }

    /**
     * Factory method, wrap input {@link Handler} to {@link TtlVertxHandler}.
     *
     * @param handler                          input {@link Handler}. if input is {@code null}, return {@code null}.
     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlVertxHandler} is referred.
     * @param idempotent                       is idempotent mode or not. if {@code true}, just return input {@link Handler} when it's {@link TtlVertxHandler},
     *                                         otherwise throw {@link IllegalStateException}.
     *                                         <B><I>Caution</I></B>: {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
     * @return Wrapped {@link Handler}
     * @throws IllegalStateException when input is {@link TtlVertxHandler} already and not idempotent.
     */
    @Nullable
    public static <T> TtlVertxHandler<T> get(@Nullable Handler<T> handler, boolean releaseTtlValueReferenceAfterRun, boolean idempotent) {
        if (null == handler) {
            return null;
        }

        if (handler instanceof TtlEnhanced) {
            // avoid redundant decoration, and ensure idempotency
            if (idempotent) {
                return (TtlVertxHandler<T>) handler;
            } else {
                throw new IllegalStateException("Already TtlVertxHandler!");
            }
        }
        return new TtlVertxHandler<>(handler, releaseTtlValueReferenceAfterRun);
    }

    /**
     * wrap input {@link Handler} Collection to {@link TtlVertxHandler} Collection.
     *
     * @param tasks task to be wrapped. if input is {@code null}, return {@code null}.
     * @return wrapped tasks
     * @throws IllegalStateException when input is {@link TtlVertxHandler} already.
     */
    @NonNull
    public static <T> List<TtlVertxHandler<T>> gets(@Nullable Collection<? extends Handler<T>> tasks) {
        return gets(tasks, false, false);
    }

    /**
     * wrap input {@link Handler} Collection to {@link TtlVertxHandler} Collection.
     *
     * @param tasks                            task to be wrapped. if input is {@code null}, return {@code null}.
     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlVertxHandler} is referred.
     * @return wrapped tasks
     * @throws IllegalStateException when input is {@link TtlVertxHandler} already.
     */
    @NonNull
    public static <T> List<TtlVertxHandler<T>> gets(@Nullable Collection<? extends Handler<T>> tasks, boolean releaseTtlValueReferenceAfterRun) {
        return gets(tasks, releaseTtlValueReferenceAfterRun, false);
    }

    /**
     * wrap input {@link Handler} Collection to {@link TtlVertxHandler} Collection.
     *
     * @param tasks                            task to be wrapped. if input is {@code null}, return {@code null}.
     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlVertxHandler} is referred.
     * @param idempotent                       is idempotent mode or not. if {@code true}, just return input {@link Handler} when it's {@link TtlVertxHandler},
     *                                         otherwise throw {@link IllegalStateException}.
     *                                         <B><I>Caution</I></B>: {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
     * @return wrapped tasks
     * @throws IllegalStateException when input is {@link TtlVertxHandler} already and not idempotent.
     */
    @NonNull
    public static <T> List<TtlVertxHandler<T>> gets(@Nullable Collection<? extends Handler<T>> tasks, boolean releaseTtlValueReferenceAfterRun, boolean idempotent) {
        if (null == tasks) {
            return Collections.emptyList();
        }

        List<TtlVertxHandler<T>> copy = new ArrayList<>();
        for (Handler<T> task : tasks) {
            copy.add(TtlVertxHandler.get(task, releaseTtlValueReferenceAfterRun, idempotent));
        }
        return copy;
    }

    /**
     * Unwrap {@link TtlVertxHandler} to the original/underneath one.
     * <p>
     * this method is {@code null}-safe, when input {@code Function} parameter is {@code null}, return {@code null};
     * if input {@code Function} parameter is not a {@link TtlVertxHandler} just return input {@code Function}.
     * <p>
     * so {@code TtlVertxHandler.unwrap(TtlVertxHandler.get(function))} will always return the same input {@code function} object.
     *
     * @see #handle(Object)
     * @see com.alibaba.ttl.TtlUnwrap#unwrap(Object)
     */
    @Nullable
    public static <T> Handler<T> unwrap(@Nullable Handler<T> handler) {
        if (!(handler instanceof TtlVertxHandler)) {
            return handler;
        } else {
            return ((TtlVertxHandler<T>) handler).getHandler();
        }
    }

    /**
     * Unwrap {@link TtlVertxHandler} to the original/underneath one for collection.
     * <p>
     * Invoke {@link #unwrap(Handler)} for each element in input collection.
     * <p>
     * This method is {@code null}-safe, when input {@code Handler} parameter collection is {@code null}, return a empty list.
     *
     * @see #gets(Collection)
     * @see #unwrap(Handler)
     */
    @NonNull
    public static <T> List<Handler<T>> unwraps(@Nullable Collection<? extends Handler<T>> tasks) {
        if (null == tasks) {
            return Collections.emptyList();
        }

        List<Handler<T>> copy = new ArrayList<>();
        for (Handler<T> task : tasks) {
            if (!(task instanceof TtlVertxHandler)) {
                copy.add(task);
            } else {
                copy.add(((TtlVertxHandler<T>) task).getHandler());
            }
        }
        return copy;
    }

    private final TtlAttachmentsDelegate ttlAttachment = new TtlAttachmentsDelegate();

    /**
     * see {@link TtlAttachments#setTtlAttachment(String, Object)}
     */
    @Override
    public void setTtlAttachment(@NonNull String key, Object value) {
        ttlAttachment.setTtlAttachment(key, value);
    }

    /**
     * see {@link TtlAttachments#getTtlAttachment(String)}
     */
    @Override
    public <T> T getTtlAttachment(@NonNull String key) {
        return ttlAttachment.getTtlAttachment(key);
    }
}
