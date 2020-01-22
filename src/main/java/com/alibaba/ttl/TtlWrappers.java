package com.alibaba.ttl;

import com.alibaba.ttl.spi.TtlEnhanced;
import com.alibaba.ttl.spi.TtlWrapper;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.concurrent.Callable;
import java.util.function.*;

import static com.alibaba.ttl.TransmittableThreadLocal.Transmitter.*;

/**
 * Util methods for TTL Wrapper:
 * wrap common {@code Functional Interface}, unwrap TTL Wrapper and check TTL Wrapper.
 * <p>
 * <b><i>Note:</i></b>
 * <ul>
 * <li>all methods is {@code null}-safe, when input parameter is {@code null}, return {@code null}.</li>
 * <li>all wrap method skip wrap (aka. just return input parameter), when input parameter is already wrapped.</li>
 * </ul>
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see TtlRunnable
 * @see TtlRunnable#get(Runnable)
 * @see TtlRunnable#unwrap(Runnable)
 * @see TtlCallable
 * @see TtlCallable#get(Callable)
 * @see TtlCallable#unwrap(Callable)
 * @see TtlWrapper
 * @since 2.11.4
 */
public class TtlWrappers {
    /**
     * wrap input {@link Supplier} to TTL wrapper.
     *
     * @param supplier input {@link Supplier}
     * @return Wrapped {@link Supplier}
     * @since 2.11.4
     */
    @Nullable
    public static <T> Supplier<T> wrap(@Nullable Supplier<T> supplier) {
        if (supplier == null) return null;
        else if (supplier instanceof TtlEnhanced) return supplier;
        else return new TtlSupplier<T>(supplier);
    }

    private static class TtlSupplier<T> implements Supplier<T>, TtlWrapper<Supplier<T>>, TtlEnhanced {
        final Supplier<T> supplier;
        final Object capture;

        TtlSupplier(@NonNull Supplier<T> supplier) {
            this.supplier = supplier;
            this.capture = capture();
        }

        @Override
        public T get() {
            final Object backup = replay(capture);
            try {
                return supplier.get();
            } finally {
                restore(backup);
            }
        }

        @NonNull
        @Override
        public Supplier<T> unwrap() {
            return supplier;
        }
    }

    /**
     * wrap input {@link Consumer} to TTL wrapper.
     *
     * @param consumer input {@link Consumer}
     * @return Wrapped {@link Consumer}
     * @since 2.11.4
     */
    @Nullable
    public static <T> Consumer<T> wrap(@Nullable Consumer<T> consumer) {
        if (consumer == null) return null;
        else if (consumer instanceof TtlEnhanced) return consumer;
        else return new TtlConsumer<T>(consumer);
    }

    private static class TtlConsumer<T> implements Consumer<T>, TtlWrapper<Consumer<T>>, TtlEnhanced {
        final Consumer<T> consumer;
        final Object capture;

        TtlConsumer(@NonNull Consumer<T> consumer) {
            this.consumer = consumer;
            this.capture = capture();
        }

        @Override
        public void accept(T t) {
            final Object backup = replay(capture);
            try {
                consumer.accept(t);
            } finally {
                restore(backup);
            }
        }

        @NonNull
        @Override
        public Consumer<T> unwrap() {
            return consumer;
        }
    }


    /**
     * wrap input {@link BiConsumer} to TTL wrapper.
     *
     * @param consumer input {@link BiConsumer}
     * @return Wrapped {@link BiConsumer}
     * @since 2.11.4
     */
    @Nullable
    public static <T, U> BiConsumer<T, U> wrap(@Nullable BiConsumer<T, U> consumer) {
        if (consumer == null) return null;
        else if (consumer instanceof TtlEnhanced) return consumer;
        else return new TtlBiConsumer<T, U>(consumer);
    }

    private static class TtlBiConsumer<T, U> implements BiConsumer<T, U>, TtlWrapper<BiConsumer<T, U>>, TtlEnhanced {
        final BiConsumer<T, U> consumer;
        final Object capture;

        TtlBiConsumer(@NonNull BiConsumer<T, U> consumer) {
            this.consumer = consumer;
            this.capture = capture();
        }

        @Override
        public void accept(T t, U u) {
            final Object backup = replay(capture);
            try {
                consumer.accept(t, u);
            } finally {
                restore(backup);
            }
        }

        @NonNull
        @Override
        public BiConsumer<T, U> unwrap() {
            return consumer;
        }
    }

    /**
     * wrap input {@link Function} to TTL wrapper.
     *
     * @param fn input {@link Function}
     * @return Wrapped {@link Function}
     * @since 2.11.4
     */
    @Nullable
    public static <T, R> Function<T, R> wrap(@Nullable Function<T, R> fn) {
        if (fn == null) return null;
        else if (fn instanceof TtlEnhanced) return fn;
        else return new TtlFunction<T, R>(fn);
    }

    private static class TtlFunction<T, R> implements Function<T, R>, TtlWrapper<Function<T, R>>, TtlEnhanced {
        final Function<T, R> fn;
        final Object capture;

        TtlFunction(@NonNull Function<T, R> fn) {
            this.fn = fn;
            this.capture = capture();
        }

        @Override
        public R apply(T t) {
            final Object backup = replay(capture);
            try {
                return fn.apply(t);
            } finally {
                restore(backup);
            }
        }

        @NonNull
        @Override
        public Function<T, R> unwrap() {
            return fn;
        }
    }


    /**
     * wrap input {@link BiFunction} to TTL wrapper.
     *
     * @param fn input {@link BiFunction}
     * @return Wrapped {@link BiFunction}
     * @since 2.11.4
     */
    @Nullable
    public static <T, U, R> BiFunction<T, U, R> wrap(@Nullable BiFunction<T, U, R> fn) {
        if (fn == null) return null;
        else if (fn instanceof TtlEnhanced) return fn;
        else return new TtlBiFunction<T, U, R>(fn);
    }

    private static class TtlBiFunction<T, U, R> implements BiFunction<T, U, R>, TtlWrapper<BiFunction<T, U, R>>, TtlEnhanced {
        final BiFunction<T, U, R> fn;
        final Object capture;

        TtlBiFunction(@NonNull BiFunction<T, U, R> fn) {
            this.fn = fn;
            this.capture = capture();
        }

        @Override
        public R apply(T t, U u) {
            final Object backup = replay(capture);
            try {
                return fn.apply(t, u);
            } finally {
                restore(backup);
            }
        }

        @NonNull
        @Override
        public BiFunction<T, U, R> unwrap() {
            return fn;
        }
    }

    /**
     * Generic unwrap method, unwrap {@code TtlWrapper} to the original/underneath one.
     * <p>
     * this method is {@code null}-safe, when input {@code BiFunction} parameter is {@code null}, return {@code null};
     * if input parameter is not a {@code TtlWrapper} just return input.
     * <p>
     * so {@code unwrap} will always return the same input object.
     *
     * @see TtlRunnable#unwrap(Runnable)
     * @see TtlCallable#unwrap(Callable)
     * @see com.alibaba.ttl.threadpool.TtlExecutors#unwrap(java.util.concurrent.Executor)
     * @see com.alibaba.ttl.threadpool.TtlExecutors#unwrap(java.util.concurrent.ThreadFactory)
     * @see com.alibaba.ttl.threadpool.TtlForkJoinPoolHelper#unwrap(java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory)
     * @since 2.11.4
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T unwrap(@Nullable T obj) {
        if (!isWrapper(obj)) return obj;
        else return ((TtlWrapper<T>) obj).unwrap();
    }

    /**
     * check the input object is {@code TtlWrapper} or not.
     *
     * @since 2.11.4
     */
    public static <T> boolean isWrapper(@Nullable T obj) {
        return obj instanceof TtlWrapper;
    }

    private TtlWrappers() {
        throw new InstantiationError("Must not instantiate this class");
    }
}
