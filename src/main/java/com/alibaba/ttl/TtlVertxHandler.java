package com.alibaba.ttl;

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
import java.util.function.Function;

import static com.alibaba.ttl.TransmittableThreadLocal.Transmitter.*;

/**
 * @author: tk
 * @since: 2021/1/14
 */
public class TtlVertxHandler<E> implements Handler<E>, TtlWrapper<Handler<E>>, TtlEnhanced, TtlAttachments {
    private final AtomicReference<Object> capturedRef;
    private final Handler<E> handler;
    private final boolean releaseTtlValueReferenceAfterRun;

    private TtlVertxHandler(@NonNull Handler<E> handler, boolean releaseTtlValueReferenceAfterRun) {
        this.capturedRef = new AtomicReference<Object>(capture());
        this.handler = handler;
        this.releaseTtlValueReferenceAfterRun = releaseTtlValueReferenceAfterRun;
    }

    /**
     * wrap method {@link Handler#handle(E)}.
     */
    @Override
    public void handle(E var1) {
        final Object captured = capturedRef.get();
        if (captured == null || releaseTtlValueReferenceAfterRun && !capturedRef.compareAndSet(captured, null)) {
            throw new IllegalStateException("TTL value reference is released after run!");
        }

        final Object backup = replay(captured);
        try {
            handler.handle(var1);
        } finally {
            restore(backup);
        }
    }

    /**
     * return original/unwrapped {@link Function}.
     */
    @NonNull
    public Handler<E> getHandler() {
        return unwrap();
    }

    /**
     * unwrap to original/unwrapped {@link Handler<E>}.
     *
     * @see TtlUnwrap#unwrap(Object)
     * @since 2.11.4
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
    public static TtlVertxHandler get(@Nullable Handler handler) {
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
    public static TtlVertxHandler get(@Nullable Handler handler, boolean releaseTtlValueReferenceAfterRun) {
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
    public static TtlVertxHandler get(@Nullable Handler handler, boolean releaseTtlValueReferenceAfterRun, boolean idempotent) {
        if (null == handler) {
            return null;
        }

        if (handler instanceof TtlEnhanced) {
            // avoid redundant decoration, and ensure idempotency
            if (idempotent) {
                return (TtlVertxHandler) handler;
            } else {
                throw new IllegalStateException("Already TtlVertxHandler!");
            }
        }
        return new TtlVertxHandler(handler, releaseTtlValueReferenceAfterRun);
    }

    /**
     * wrap input {@link Handler} Collection to {@link TtlVertxHandler} Collection.
     *
     * @param tasks task to be wrapped. if input is {@code null}, return {@code null}.
     * @return wrapped tasks
     * @throws IllegalStateException when input is {@link TtlVertxHandler} already.
     */
    @NonNull
    public static List<TtlVertxHandler> gets(@Nullable Collection<? extends Handler> tasks) {
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
    public static List<TtlVertxHandler> gets(@Nullable Collection<? extends Handler> tasks, boolean releaseTtlValueReferenceAfterRun) {
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
    public static List<TtlVertxHandler> gets(@Nullable Collection<? extends Handler> tasks, boolean releaseTtlValueReferenceAfterRun, boolean idempotent) {
        if (null == tasks) {
            return Collections.emptyList();
        }

        List<TtlVertxHandler> copy = new ArrayList<TtlVertxHandler>();
        for (Handler task : tasks) {
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
     * @since 2.10.2
     */
    @Nullable
    public static Handler unwrap(@Nullable Handler handler) {
        if (!(handler instanceof TtlVertxHandler)) {
            return handler;
        } else {
            return ((TtlVertxHandler) handler).getHandler();
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
     * @since 2.10.2
     */
    @NonNull
    public static List<Handler> unwraps(@Nullable Collection<? extends Handler> tasks) {
        if (null == tasks) {
            return Collections.emptyList();
        }

        List<Handler> copy = new ArrayList<Handler>();
        for (Handler task : tasks) {
            if (!(task instanceof TtlVertxHandler)) {
                copy.add(task);
            } else {
                copy.add(((TtlVertxHandler) task).getHandler());
            }
        }
        return copy;
    }

    private final TtlAttachmentsDelegate ttlAttachment = new TtlAttachmentsDelegate();

    /**
     * see {@link TtlAttachments#setTtlAttachment(String, Object)}
     *
     * @since 2.11.0
     */
    @Override
    public void setTtlAttachment(@NonNull String key, Object value) {
        ttlAttachment.setTtlAttachment(key, value);
    }

    /**
     * see {@link TtlAttachments#getTtlAttachment(String)}
     *
     * @since 2.11.0
     */
    @Override
    public <T> T getTtlAttachment(@NonNull String key) {
        return ttlAttachment.getTtlAttachment(key);
    }
}
