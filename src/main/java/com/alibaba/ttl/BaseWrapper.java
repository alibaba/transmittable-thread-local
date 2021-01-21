//package com.alibaba.ttl;
//
//import com.alibaba.ttl.spi.TtlAttachments;
//import com.alibaba.ttl.spi.TtlAttachmentsDelegate;
//import com.alibaba.ttl.spi.TtlEnhanced;
//import com.alibaba.ttl.spi.TtlWrapper;
//import edu.umd.cs.findbugs.annotations.NonNull;
//import edu.umd.cs.findbugs.annotations.Nullable;
//import io.vertx.core.Handler;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.atomic.AtomicReference;
//
//import static com.alibaba.ttl.TransmittableThreadLocal.Transmitter.capture;
//
///**
// * @author: tk
// * @since: 2021/1/21
// */
//public abstract class BaseWrapper<T> implements TtlWrapper<T>, TtlEnhanced, TtlAttachments {
//    protected final AtomicReference<Object> capturedRef;
//    protected final T delegate;
//    protected final boolean releaseTtlValueReferenceAfterRun;
//
//    BaseWrapper(@NonNull T delegate, boolean releaseTtlValueReferenceAfterRun) {
//        this.capturedRef = new AtomicReference<Object>(capture());
//        this.delegate = delegate;
//        this.releaseTtlValueReferenceAfterRun = releaseTtlValueReferenceAfterRun;
//    }
//
//    /**
//     * return original/unwrapped {@link T}.
//     */
//    @NonNull
//    public T getDelegate() {
//        return unwrap();
//    }
//
//    /**
//     * unwrap to original/unwrapped {@link T}.
//     *
//     * @see TtlUnwrap#unwrap(Object)
//     * @since 2.11.4
//     */
//    @NonNull
//    @Override
//    public T unwrap() {
//        return delegate;
//    }
//
//
//    @Override
//    public int hashCode() {
//        return delegate.hashCode();
//    }
//
//    @Override
//    public String toString() {
//        return this.getClass().getName() + " - " + delegate.toString();
//    }
//
//    /**
//     * Factory method, wrap input {@link Handler} to {@link TtlVertxHandlerOld}.
//     *
//     * @param handler input {@link Handler}. if input is {@code null}, return {@code null}.
//     * @return Wrapped {@link Handler}
//     * @throws IllegalStateException when input is {@link TtlVertxHandlerOld} already.
//     */
//    @Nullable
//    public static <R> R get(@Nullable T handler) {
//        return get(handler, false, false);
//    }
//
//    /**
//     * Factory method, wrap input {@link Handler} to {@link TtlVertxHandlerOld}.
//     *
//     * @param handler                          input {@link Handler}. if input is {@code null}, return {@code null}.
//     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlVertxHandlerOld} is referred.
//     * @return Wrapped {@link Handler}
//     * @throws IllegalStateException when input is {@link TtlVertxHandlerOld} already.
//     */
//    @Nullable
//    public static TtlVertxHandlerOld get(@Nullable Handler handler, boolean releaseTtlValueReferenceAfterRun) {
//        return get(handler, releaseTtlValueReferenceAfterRun, false);
//    }
//
//    /**
//     * Factory method, wrap input {@link Handler} to {@link TtlVertxHandlerOld}.
//     *
//     * @param handler                          input {@link Handler}. if input is {@code null}, return {@code null}.
//     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlVertxHandlerOld} is referred.
//     * @param idempotent                       is idempotent mode or not. if {@code true}, just return input {@link Handler} when it's {@link TtlVertxHandlerOld},
//     *                                         otherwise throw {@link IllegalStateException}.
//     *                                         <B><I>Caution</I></B>: {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
//     * @return Wrapped {@link Handler}
//     * @throws IllegalStateException when input is {@link TtlVertxHandlerOld} already and not idempotent.
//     */
//    @Nullable
//    public static <R> R get(@Nullable T handler, boolean releaseTtlValueReferenceAfterRun, boolean idempotent) {
//        if (null == handler) {
//            return null;
//        }
//
//        if (handler instanceof TtlEnhanced) {
//            // avoid redundant decoration, and ensure idempotency
//            if (idempotent) {
//                return (TtlVertxHandlerOld) handler;
//            } else {
//                throw new IllegalStateException("Already TtlVertxHandler!");
//            }
//        }
//        return new TtlVertxHandlerOld(handler, releaseTtlValueReferenceAfterRun);
//    }
//
//    public static <R,X> X get(@Nullable T handler,Class<X> clazz, boolean releaseTtlValueReferenceAfterRun, boolean idempotent) {
//        if (null == handler) {
//            return null;
//        }
//
//        if (handler instanceof TtlEnhanced) {
//            // avoid redundant decoration, and ensure idempotency
//            if (idempotent) {
//                return (X) handler;
//            } else {
//                throw new IllegalStateException("Already TtlVertxHandler!");
//            }
//        }
//        return new TtlVertxHandlerOld(handler, releaseTtlValueReferenceAfterRun);
//    }
//    /**
//     * wrap input {@link Handler} Collection to {@link TtlVertxHandlerOld} Collection.
//     *
//     * @param tasks task to be wrapped. if input is {@code null}, return {@code null}.
//     * @return wrapped tasks
//     * @throws IllegalStateException when input is {@link TtlVertxHandlerOld} already.
//     */
//    @NonNull
//    public static List<TtlVertxHandlerOld> gets(@Nullable Collection<? extends Handler> tasks) {
//        return gets(tasks, false, false);
//    }
//
//    /**
//     * wrap input {@link Handler} Collection to {@link TtlVertxHandlerOld} Collection.
//     *
//     * @param tasks                            task to be wrapped. if input is {@code null}, return {@code null}.
//     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlVertxHandlerOld} is referred.
//     * @return wrapped tasks
//     * @throws IllegalStateException when input is {@link TtlVertxHandlerOld} already.
//     */
//    @NonNull
//    public static List<TtlVertxHandlerOld> gets(@Nullable Collection<? extends Handler> tasks, boolean releaseTtlValueReferenceAfterRun) {
//        return gets(tasks, releaseTtlValueReferenceAfterRun, false);
//    }
//
//    /**
//     * wrap input {@link Handler} Collection to {@link TtlVertxHandlerOld} Collection.
//     *
//     * @param tasks                            task to be wrapped. if input is {@code null}, return {@code null}.
//     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlVertxHandlerOld} is referred.
//     * @param idempotent                       is idempotent mode or not. if {@code true}, just return input {@link Handler} when it's {@link TtlVertxHandlerOld},
//     *                                         otherwise throw {@link IllegalStateException}.
//     *                                         <B><I>Caution</I></B>: {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
//     * @return wrapped tasks
//     * @throws IllegalStateException when input is {@link TtlVertxHandlerOld} already and not idempotent.
//     */
//    @NonNull
//    public static List<TtlVertxHandlerOld> gets(@Nullable Collection<? extends Handler> tasks, boolean releaseTtlValueReferenceAfterRun, boolean idempotent) {
//        if (null == tasks) {
//            return Collections.emptyList();
//        }
//
//        List<TtlVertxHandlerOld> copy = new ArrayList<TtlVertxHandlerOld>();
//        for (Handler task : tasks) {
//            copy.add(TtlVertxHandlerOld.get(task, releaseTtlValueReferenceAfterRun, idempotent));
//        }
//        return copy;
//    }
//
//    /**
//     * Unwrap {@link TtlVertxHandlerOld} to the original/underneath one.
//     * <p>
//     * this method is {@code null}-safe, when input {@code Function} parameter is {@code null}, return {@code null};
//     * if input {@code Function} parameter is not a {@link TtlVertxHandlerOld} just return input {@code Function}.
//     * <p>
//     * so {@code TtlVertxHandler.unwrap(TtlVertxHandler.get(function))} will always return the same input {@code function} object.
//     *
//     * @see #handle(Object)
//     * @see TtlUnwrap#unwrap(Object)
//     * @since 2.10.2
//     */
//    @Nullable
//    public static Handler unwrap(@Nullable Handler handler) {
//        if (!(handler instanceof TtlVertxHandlerOld)) {
//            return handler;
//        } else {
//            return ((TtlVertxHandlerOld) handler).getHandler();
//        }
//    }
//
//    /**
//     * Unwrap {@link TtlVertxHandlerOld} to the original/underneath one for collection.
//     * <p>
//     * Invoke {@link #unwrap(Handler)} for each element in input collection.
//     * <p>
//     * This method is {@code null}-safe, when input {@code Handler} parameter collection is {@code null}, return a empty list.
//     *
//     * @see #gets(Collection)
//     * @see #unwrap(Handler)
//     * @since 2.10.2
//     */
//    @NonNull
//    public static List<Handler> unwraps(@Nullable Collection<? extends Handler> tasks) {
//        if (null == tasks) {
//            return Collections.emptyList();
//        }
//
//        List<Handler> copy = new ArrayList<Handler>();
//        for (Handler task : tasks) {
//            if (!(task instanceof TtlVertxHandlerOld)) {
//                copy.add(task);
//            } else {
//                copy.add(((TtlVertxHandlerOld) task).getHandler());
//            }
//        }
//        return copy;
//    }
//
//    private final TtlAttachmentsDelegate ttlAttachment = new TtlAttachmentsDelegate();
//
//    /**
//     * see {@link TtlAttachments#setTtlAttachment(String, Object)}
//     *
//     * @since 2.11.0
//     */
//    @Override
//    public void setTtlAttachment(@NonNull String key, Object value) {
//        ttlAttachment.setTtlAttachment(key, value);
//    }
//
//    /**
//     * see {@link TtlAttachments#getTtlAttachment(String)}
//     *
//     * @since 2.11.0
//     */
//    @Override
//    public <T> T getTtlAttachment(@NonNull String key) {
//        return ttlAttachment.getTtlAttachment(key);
//    }
//}
