//package com.alibaba.ttl;
//
//import com.alibaba.ttl.spi.TtlAttachments;
//import com.alibaba.ttl.spi.TtlAttachmentsDelegate;
//import com.alibaba.ttl.spi.TtlEnhanced;
//import com.alibaba.ttl.spi.TtlWrapper;
//import edu.umd.cs.findbugs.annotations.NonNull;
//import edu.umd.cs.findbugs.annotations.Nullable;
//import io.reactivex.SingleObserver;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.atomic.AtomicReference;
//import java.util.function.Function;
//
//import static com.alibaba.ttl.TransmittableThreadLocal.Transmitter.*;
//
///**
// * @author: tk
// * @since: 2021/1/14
// */
//public class TtlSingleObserver<T> extends SingleObserver<T> implements TtlWrapper<SingleObserver>, TtlEnhanced, TtlAttachments {
//
//    private final AtomicReference<Object> capturedRef;
//    private final SingleObserver<T> singleObserver;
//    private final boolean releaseTtlValueReferenceAfterRun;
//
//    private TtlSingleObserver(@NonNull SingleObserver<T> singleObserver, boolean releaseTtlValueReferenceAfterRun) {
//        this.capturedRef = new AtomicReference<Object>(capture());
//        this.singleObserver = singleObserver;
//        this.releaseTtlValueReferenceAfterRun = releaseTtlValueReferenceAfterRun;
//    }
//
//    /**
//     * wrap method {@link SingleObserver#onSuccess(T)}.
//     */
//    @Override
//    public void onSuccess(@io.reactivex.annotations.NonNull T t) {
//        final Object captured = capturedRef.get();
//        if (captured == null || releaseTtlValueReferenceAfterRun && !capturedRef.compareAndSet(captured, null)) {
//            throw new IllegalStateException("TTL value reference is released after run!");
//        }
//
//        final Object backup = replay(captured);
//        try {
//            singleObserver.onSuccess(t);
//        } finally {
//            restore(backup);
//        }
//    }
//
//    /**
//     * return original/unwrapped {@link SingleObserver}.
//     */
//    @NonNull
//    public SingleObserver<T> getSingleObserver() {
//        return unwrap();
//    }
//
//    /**
//     * unwrap to original/unwrapped {@link SingleObserver}.
//     *
//     * @see TtlUnwrap#unwrap(Object)
//     * @since 2.11.4
//     */
//    @NonNull
//    @Override
//    public SingleObserver<T> unwrap() {
//        return singleObserver;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) {
//            return true;
//        }
//        if (o == null || getClass() != o.getClass()) {
//            return false;
//        }
//
//        TtlSingleObserver<T> that = (TtlSingleObserver<T>) o;
//
//        return singleObserver.equals(that.singleObserver);
//    }
//
//    @Override
//    public int hashCode() {
//        return singleObserver.hashCode();
//    }
//
//    @Override
//    public String toString() {
//        return this.getClass().getName() + " - " + singleObserver.toString();
//    }
//
//    /**
//     * Factory method, wrap input {@link Function} to {@link TtlFunction}.
//     *
//     * @param function input {@link Runnable}. if input is {@code null}, return {@code null}.
//     * @return Wrapped {@link Runnable}
//     * @throws IllegalStateException when input is {@link TtlFunction} already.
//     */
//    @Nullable
//    public static TtlSingleObserver get(@Nullable SingleObserver function) {
//        return get(function, false, false);
//    }
//
//    /**
//     * Factory method, wrap input {@link Runnable} to {@link TtlFunction}.
//     *
//     * @param function                         input {@link Runnable}. if input is {@code null}, return {@code null}.
//     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlFunction} is referred.
//     * @return Wrapped {@link Runnable}
//     * @throws IllegalStateException when input is {@link TtlFunction} already.
//     */
//    @Nullable
//    public static SingleObserver get(@Nullable SingleObserver singleObserver, boolean releaseTtlValueReferenceAfterRun) {
//        return get(singleObserver, releaseTtlValueReferenceAfterRun, false);
//    }
//
//    /**
//     * Factory method, wrap input {@link Function} to {@link TtlFunction}.
//     *
//     * @param singleObserver                   input {@link Function}. if input is {@code null}, return {@code null}.
//     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlFunction} is referred.
//     * @param idempotent                       is idempotent mode or not. if {@code true}, just return input {@link Function} when it's {@link TtlFunction},
//     *                                         otherwise throw {@link IllegalStateException}.
//     *                                         <B><I>Caution</I></B>: {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
//     * @return Wrapped {@link Function}
//     * @throws IllegalStateException when input is {@link TtlFunction} already and not idempotent.
//     */
//    @Nullable
//    public static TtlSingleObserver get(@Nullable SingleObserver singleObserver, boolean releaseTtlValueReferenceAfterRun, boolean idempotent) {
//        if (null == singleObserver) {
//            return null;
//        }
//
//        if (singleObserver instanceof TtlEnhanced) {
//            // avoid redundant decoration, and ensure idempotency
//            if (idempotent) {
//                return (TtlSingleObserver) singleObserver;
//            } else {
//                throw new IllegalStateException("Already TtlFunction!");
//            }
//        }
//        return new TtlSingleObserver(singleObserver, releaseTtlValueReferenceAfterRun);
//    }
//
//    /**
//     * wrap input {@link Function} Collection to {@link TtlFunction} Collection.
//     *
//     * @param tasks task to be wrapped. if input is {@code null}, return {@code null}.
//     * @return wrapped tasks
//     * @throws IllegalStateException when input is {@link TtlFunction} already.
//     */
//    @NonNull
//    public static List<TtlFunction> gets(@Nullable Collection<? extends Function> tasks) {
//        return gets(tasks, false, false);
//    }
//
//    /**
//     * wrap input {@link Function} Collection to {@link TtlFunction} Collection.
//     *
//     * @param tasks                            task to be wrapped. if input is {@code null}, return {@code null}.
//     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlFunction} is referred.
//     * @return wrapped tasks
//     * @throws IllegalStateException when input is {@link TtlFunction} already.
//     */
//    @NonNull
//    public static List<TtlFunction> gets(@Nullable Collection<? extends Function> tasks, boolean releaseTtlValueReferenceAfterRun) {
//        return gets(tasks, releaseTtlValueReferenceAfterRun, false);
//    }
//
//    /**
//     * wrap input {@link Runnable} Collection to {@link TtlFunction} Collection.
//     *
//     * @param tasks                            task to be wrapped. if input is {@code null}, return {@code null}.
//     * @param releaseTtlValueReferenceAfterRun release TTL value reference after run, avoid memory leak even if {@link TtlFunction} is referred.
//     * @param idempotent                       is idempotent mode or not. if {@code true}, just return input {@link Runnable} when it's {@link TtlFunction},
//     *                                         otherwise throw {@link IllegalStateException}.
//     *                                         <B><I>Caution</I></B>: {@code true} will cover up bugs! <b>DO NOT</b> set, only when you know why.
//     * @return wrapped tasks
//     * @throws IllegalStateException when input is {@link TtlFunction} already and not idempotent.
//     */
//    @NonNull
//    public static List<TtlFunction> gets(@Nullable Collection<? extends Function> tasks, boolean releaseTtlValueReferenceAfterRun, boolean idempotent) {
//        if (null == tasks) {
//            return Collections.emptyList();
//        }
//
//        List<TtlFunction> copy = new ArrayList<TtlFunction>();
//        for (Function task : tasks) {
//            copy.add(TtlFunction.get(task, releaseTtlValueReferenceAfterRun, idempotent));
//        }
//        return copy;
//    }
//
//    /**
//     * Unwrap {@link TtlFunction} to the original/underneath one.
//     * <p>
//     * this method is {@code null}-safe, when input {@code Function} parameter is {@code null}, return {@code null};
//     * if input {@code Function} parameter is not a {@link TtlFunction} just return input {@code Function}.
//     * <p>
//     * so {@code TtlFunction.unwrap(TtlFunction.get(function))} will always return the same input {@code function} object.
//     *
//     * @see #get(Function)
//     * @see com.alibaba.ttl.TtlUnwrap#unwrap(Object)
//     * @since 2.10.2
//     */
//    @Nullable
//    public static Function unwrap(@Nullable Function function) {
//        if (!(function instanceof TtlFunction)) {
//            return function;
//        } else {
//            return ((TtlFunction) function).getFunction();
//        }
//    }
//
//    /**
//     * Unwrap {@link TtlFunction} to the original/underneath one for collection.
//     * <p>
//     * Invoke {@link #unwrap(Function)} for each element in input collection.
//     * <p>
//     * This method is {@code null}-safe, when input {@code Function} parameter collection is {@code null}, return a empty list.
//     *
//     * @see #gets(Collection)
//     * @see #unwrap(Function)
//     * @since 2.10.2
//     */
//    @NonNull
//    public static List<Function> unwraps(@Nullable Collection<? extends Function> tasks) {
//        if (null == tasks) {
//            return Collections.emptyList();
//        }
//
//        List<Function> copy = new ArrayList<Function>();
//        for (Function task : tasks) {
//            if (!(task instanceof TtlFunction)) {
//                copy.add(task);
//            } else {
//                copy.add(((TtlFunction) task).getFunction());
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
