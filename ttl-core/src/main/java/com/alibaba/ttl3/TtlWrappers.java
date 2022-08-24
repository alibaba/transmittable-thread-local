package com.alibaba.ttl3;

import com.alibaba.crr.composite.Backup;
import com.alibaba.crr.composite.Capture;
import com.alibaba.ttl3.executor.TtlExecutors;
import com.alibaba.ttl3.spi.TtlEnhanced;
import com.alibaba.ttl3.spi.TtlWrapper;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.jetbrains.annotations.Contract;

import java.util.concurrent.Callable;
import java.util.function.*;

import static com.alibaba.ttl3.transmitter.Transmitter.*;

/**
 * Util methods for TTL Wrapper.
 *
 * <ul>
 * <li>wrap common {@code Functional Interface}.<br>
 *     if missing your desired wrapper util method,
 *     you need implement your own util method alike.
 * </li>
 * <li>unwrap TTL Wrapper and check whether it is TTL Wrapper.</li>
 * </ul>
 * <p>
 * <b><i>Note:</i></b>
 * <ul>
 * <li>all methods is {@code null}-safe, when input parameter is {@code null}, return {@code null}.</li>
 * <li>all wrap method skip wrapping (aka. just return input parameter), when input parameter is already wrapped.</li>
 * <li>the wrap methods for {@link Runnable} and {@link java.util.concurrent.Callable} are provided
 *     by {@link TtlRunnable#get(Runnable)} and {@link TtlCallable#get(Callable)}.</li>
 * </ul>
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @author huangfei1101 (fei.hf at alibaba-inc dot com)
 * @see TtlRunnable
 * @see TtlCallable
 * @see TtlWrapper
 */
public final class TtlWrappers {
    /**
     * wrap {@link Supplier} to TTL wrapper.
     *
     * @param supplier input {@link Supplier}
     * @return Wrapped {@link Supplier}
     * @see TtlWrappers#unwrap(Object)
     */
    @Nullable
    @Contract(value = "null -> null; !null -> !null", pure = true)
    public static <T> Supplier<T> wrapSupplier(@Nullable Supplier<T> supplier) {
        if (supplier == null) return null;
        else if (supplier instanceof TtlEnhanced) return supplier;
        else return new TtlSupplier<>(supplier);
    }

    /**
     * wrap {@link Consumer} to TTL wrapper.
     *
     * @param consumer input {@link Consumer}
     * @return Wrapped {@link Consumer}
     * @see TtlWrappers#unwrap(Object)
     */
    @Nullable
    @Contract(value = "null -> null; !null -> !null", pure = true)
    public static <T> Consumer<T> wrapConsumer(@Nullable Consumer<T> consumer) {
        if (consumer == null) return null;
        else if (consumer instanceof TtlEnhanced) return consumer;
        else return new TtlConsumer<>(consumer);
    }

    /**
     * wrap {@link BiConsumer} to TTL wrapper.
     *
     * @param consumer input {@link BiConsumer}
     * @return Wrapped {@link BiConsumer}
     * @see TtlWrappers#unwrap(Object)
     */
    @Nullable
    @Contract(value = "null -> null; !null -> !null", pure = true)
    public static <T, U> BiConsumer<T, U> wrapBiConsumer(@Nullable BiConsumer<T, U> consumer) {
        if (consumer == null) return null;
        else if (consumer instanceof TtlEnhanced) return consumer;
        else return new TtlBiConsumer<>(consumer);
    }

    /**
     * wrap {@link Function} to TTL wrapper.
     *
     * @param fn input {@link Function}
     * @return Wrapped {@link Function}
     * @see TtlWrappers#unwrap(Object)
     */
    @Nullable
    @Contract(value = "null -> null; !null -> !null", pure = true)
    public static <T, R> Function<T, R> wrapFunction(@Nullable Function<T, R> fn) {
        if (fn == null) return null;
        else if (fn instanceof TtlEnhanced) return fn;
        else return new TtlFunction<>(fn);
    }

    /**
     * wrap {@link BiFunction} to TTL wrapper.
     *
     * @param fn input {@link BiFunction}
     * @return Wrapped {@link BiFunction}
     * @see TtlWrappers#unwrap(Object)
     */
    @Nullable
    @Contract(value = "null -> null; !null -> !null", pure = true)
    public static <T, U, R> BiFunction<T, U, R> wrapBiFunction(@Nullable BiFunction<T, U, R> fn) {
        if (fn == null) return null;
        else if (fn instanceof TtlEnhanced) return fn;
        else return new TtlBiFunction<>(fn);
    }

    /**
     * Generic unwrap method, unwrap {@link TtlWrapper} to the original/underneath one.
     * <p>
     * this method is {@code null}-safe, when input parameter is {@code null}, return {@code null};
     * if input parameter is not a {@link TtlWrapper} just return input.
     *
     * @see TtlRunnable#unwrap(Runnable)
     * @see TtlCallable#unwrap(java.util.concurrent.Callable)
     * @see TtlExecutors#unwrapTtlExecutor(java.util.concurrent.Executor)
     * @see TtlExecutors#unwrapDisableInheritableThreadFactory(java.util.concurrent.ThreadFactory)
     * @see TtlExecutors#unwrapDisableInheritableForkJoinWorkerThreadFactory(java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory)
     * @see TtlExecutors#unwrapTtlRunnableUnwrapComparator(java.util.Comparator)
     * @see TtlWrappers#wrapSupplier(Supplier)
     * @see TtlWrappers#wrapConsumer(Consumer)
     * @see TtlWrappers#wrapBiConsumer(BiConsumer)
     * @see TtlWrappers#wrapFunction(Function)
     * @see TtlWrappers#wrapBiFunction(BiFunction)
     * @see #isWrapper(Object)
     */
    @Nullable
    @Contract(value = "null -> null; !null -> !null", pure = true)
    @SuppressWarnings("unchecked")
    public static <T> T unwrap(@Nullable T obj) {
        if (!isWrapper(obj)) return obj;
        else return ((TtlWrapper<T>) obj).unwrap();
    }

    /**
     * check the input object is a {@code TtlWrapper} or not.
     *
     * @see #unwrap(Object)
     */
    public static <T> boolean isWrapper(@Nullable T obj) {
        return obj instanceof TtlWrapper;
    }

    ///////////////////////////////////////////////////////////////////////////
    // private inner classes
    ///////////////////////////////////////////////////////////////////////////

    private static class TtlSupplier<T> implements Supplier<T>, TtlWrapper<Supplier<T>>, TtlEnhanced {
        final Supplier<T> supplier;
        final Capture captured;

        TtlSupplier(@NonNull Supplier<T> supplier) {
            this.supplier = supplier;
            this.captured = capture();
        }

        @Override
        public T get() {
            final Backup backup = replay(captured);
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TtlSupplier<?> that = (TtlSupplier<?>) o;

            return supplier.equals(that.supplier);
        }

        @Override
        public int hashCode() {
            return supplier.hashCode();
        }

        @Override
        public String toString() {
            return this.getClass().getName() + " - " + supplier.toString();
        }
    }

    private static class TtlConsumer<T> implements Consumer<T>, TtlWrapper<Consumer<T>>, TtlEnhanced {
        final Consumer<T> consumer;
        final Capture captured;

        TtlConsumer(@NonNull Consumer<T> consumer) {
            this.consumer = consumer;
            this.captured = capture();
        }

        @Override
        public void accept(T t) {
            final Backup backup = replay(captured);
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TtlConsumer<?> that = (TtlConsumer<?>) o;

            return consumer.equals(that.consumer);
        }

        @Override
        public int hashCode() {
            return consumer.hashCode();
        }

        @Override
        public String toString() {
            return this.getClass().getName() + " - " + consumer.toString();
        }
    }

    private static class TtlBiConsumer<T, U> implements BiConsumer<T, U>, TtlWrapper<BiConsumer<T, U>>, TtlEnhanced {
        final BiConsumer<T, U> consumer;
        final Capture captured;

        TtlBiConsumer(@NonNull BiConsumer<T, U> consumer) {
            this.consumer = consumer;
            this.captured = capture();
        }

        @Override
        public void accept(T t, U u) {
            final Backup backup = replay(captured);
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TtlBiConsumer<?, ?> that = (TtlBiConsumer<?, ?>) o;

            return consumer.equals(that.consumer);
        }

        @Override
        public int hashCode() {
            return consumer.hashCode();
        }

        @Override
        public String toString() {
            return this.getClass().getName() + " - " + consumer.toString();
        }
    }

    private static class TtlFunction<T, R> implements Function<T, R>, TtlWrapper<Function<T, R>>, TtlEnhanced {
        final Function<T, R> fn;
        final Capture captured;

        TtlFunction(@NonNull Function<T, R> fn) {
            this.fn = fn;
            this.captured = capture();
        }

        @Override
        public R apply(T t) {
            final Backup backup = replay(captured);
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TtlFunction<?, ?> that = (TtlFunction<?, ?>) o;

            return fn.equals(that.fn);
        }

        @Override
        public int hashCode() {
            return fn.hashCode();
        }

        @Override
        public String toString() {
            return this.getClass().getName() + " - " + fn.toString();
        }
    }

    private static class TtlBiFunction<T, U, R> implements BiFunction<T, U, R>, TtlWrapper<BiFunction<T, U, R>>, TtlEnhanced {
        final BiFunction<T, U, R> fn;
        final Capture captured;

        TtlBiFunction(@NonNull BiFunction<T, U, R> fn) {
            this.fn = fn;
            this.captured = capture();
        }

        @Override
        public R apply(T t, U u) {
            final Backup backup = replay(captured);
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TtlBiFunction<?, ?, ?> that = (TtlBiFunction<?, ?, ?>) o;

            return fn.equals(that.fn);
        }

        @Override
        public int hashCode() {
            return fn.hashCode();
        }

        @Override
        public String toString() {
            return this.getClass().getName() + " - " + fn.toString();
        }
    }

    private TtlWrappers() {
        throw new InstantiationError("Must not instantiate this class");
    }
}
