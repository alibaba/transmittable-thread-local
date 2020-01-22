package com.alibaba.ttl;

import com.alibaba.ttl.spi.TtlEnhanced;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.concurrent.Callable;
import java.util.function.*;

import static com.alibaba.ttl.TransmittableThreadLocal.Transmitter.*;

/**
 * Util methods for TTL Wrapper for common {@code Functional Interface}.
 * <p>
 * <b><i>Note:</i></b>
 * <ul>
 * <li>all method is {@code null}-safe, when input parameter is {@code null}, return {@code null}.</li>
 * <li>skip wrap (aka. just return input parameter), when input parameter is already wrapped.</li>
 * </ul>
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see TtlRunnable
 * @see TtlRunnable#get(Runnable)
 * @see TtlRunnable#unwrap(Runnable)
 * @see TtlCallable
 * @see TtlCallable#get(Callable)
 * @see TtlCallable#unwrap(Callable)
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

    private static class TtlSupplier<T> implements Supplier<T>, TtlEnhanced {
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
    }

    /**
     * Unwrap {@code TtlSupplier} to the original/underneath one.
     * <p>
     * this method is {@code null}-safe, when input {@code Supplier} parameter is {@code null}, return {@code null};
     * if input {@code Supplier} parameter is not a {@code Supplier} just return input {@code Supplier}.
     * <p>
     * so {@code unwrap(Supplier)} will always return the same input {@code Supplier} object.
     *
     * @see #wrap(Supplier)
     * @since 2.11.4
     */
    @Nullable
    public static <T> Supplier<T> unwrap(@Nullable Supplier<T> supplier) {
        if (!(supplier instanceof TtlSupplier)) return supplier;
        else return ((TtlSupplier<T>) supplier).supplier;
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

    private static class TtlConsumer<T> implements Consumer<T>, TtlEnhanced {
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
    }

    /**
     * Unwrap {@code TtlConsumer} to the original/underneath one.
     * <p>
     * this method is {@code null}-safe, when input {@code Consumer} parameter is {@code null}, return {@code null};
     * if input {@code Consumer} parameter is not a {@code Consumer} just return input {@code Consumer}.
     * <p>
     * so {@code unwrap(Consumer)} will always return the same input {@code Consumer} object.
     *
     * @see #wrap(Consumer)
     * @since 2.11.4
     */
    @Nullable
    public static <T> Consumer<T> unwrap(@Nullable Consumer<T> consumer) {
        if (!(consumer instanceof TtlConsumer)) return consumer;
        else return ((TtlConsumer<T>) consumer).consumer;
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

    private static class TtlBiConsumer<T, U> implements BiConsumer<T, U>, TtlEnhanced {
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
    }

    /**
     * Unwrap {@code TtlBiConsumer} to the original/underneath one.
     * <p>
     * this method is {@code null}-safe, when input {@code BiConsumer} parameter is {@code null}, return {@code null};
     * if input {@code BiConsumer} parameter is not a {@code BiConsumer} just return input {@code BiConsumer}.
     * <p>
     * so {@code unwrap(BiConsumer)} will always return the same input {@code BiConsumer} object.
     *
     * @see #wrap(BiConsumer)
     * @since 2.11.4
     */
    @Nullable
    public static <T, U> BiConsumer<T, U> unwrap(@Nullable BiConsumer<T, U> consumer) {
        if (!(consumer instanceof TtlBiConsumer)) return consumer;
        else return ((TtlBiConsumer<T, U>) consumer).consumer;
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

    private static class TtlFunction<T, R> implements Function<T, R>, TtlEnhanced {
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
    }

    /**
     * Unwrap {@code TtlFunction} to the original/underneath one.
     * <p>
     * this method is {@code null}-safe, when input {@code Function} parameter is {@code null}, return {@code null};
     * if input {@code Function} parameter is not a {@code TtlFunction} just return input {@code Function}.
     * <p>
     * so {@code unwrap(Function)} will always return the same input {@code Function} object.
     *
     * @see #wrap(Function)
     * @since 2.11.4
     */
    @Nullable
    public static <T, R> Function<T, R> unwrap(@Nullable Function<T, R> fn) {
        if (!(fn instanceof TtlFunction)) return fn;
        else return ((TtlFunction<T, R>) fn).fn;
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

    private static class TtlBiFunction<T, U, R> implements BiFunction<T, U, R>, TtlEnhanced {
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
    }

    /**
     * Unwrap {@code TtlBiFunction} to the original/underneath one.
     * <p>
     * this method is {@code null}-safe, when input {@code BiFunction} parameter is {@code null}, return {@code null};
     * if input {@code BiFunction} parameter is not a {@code TtlBiFunction} just return input {@code BiFunction}.
     * <p>
     * so {@code unwrap(BiFunction)} will always return the same input {@code BiFunction} object.
     *
     * @see #wrap(BiFunction)
     * @since 2.11.4
     */
    @Nullable
    public static <T, U, R> BiFunction<T, U, R> unwrap(@Nullable BiFunction<T, U, R> fn) {
        if (!(fn instanceof TtlBiFunction)) return fn;
        else return ((TtlBiFunction<T, U, R>) fn).fn;
    }

    private TtlWrappers() {
        throw new InstantiationError("Must not instantiate this class");
    }
}
