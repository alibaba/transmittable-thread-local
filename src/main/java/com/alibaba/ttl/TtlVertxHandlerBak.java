//package com.alibaba.ttl;
//
//import io.vertx.core.Handler;
//
//import static com.alibaba.ttl.TransmittableThreadLocal.Transmitter.*;
//
///**
// * @author: tk
// * @since: 2021/1/14
// */
//public class TtlVertxHandler<E> extends BaseWrapper<Handler<E>> implements Handler<E> {
//
//    public TtlVertxHandler(Handler<E> delegate, boolean releaseTtlValueReferenceAfterRun) {
//        super(delegate, releaseTtlValueReferenceAfterRun);
//    }
//
//    /**
//     * wrap method {@link Handler#handle(E)}.
//     */
//    @Override
//    public void handle(E var1) {
//        final Object captured = capturedRef.get();
//        if (captured == null || releaseTtlValueReferenceAfterRun && !capturedRef.compareAndSet(captured, null)) {
//            throw new IllegalStateException("TTL value reference is released after run!");
//        }
//
//        final Object backup = replay(captured);
//        try {
//            delegate.handle(var1);
//        } finally {
//            restore(backup);
//        }
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
//        TtlVertxHandler<E> that = (TtlVertxHandler<E>) o;
//
//        return delegate.equals(that.delegate);
//    }
//}
