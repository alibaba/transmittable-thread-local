package com.alibaba.ttl.threadpool.agent.internal.transformlet.impl;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.threadpool.agent.internal.logging.Logger;
import com.alibaba.ttl.threadpool.agent.internal.transformlet.ClassInfo;
import com.alibaba.ttl.threadpool.agent.internal.transformlet.JavassistTransformlet;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javassist.*;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

import static com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.TtlForkJoinTransformlet.capturedFieldName;
import static com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.Utils.*;

/**
 * TTL {@link JavassistTransformlet} for {@code java.util.concurrent.CompletableFuture$Completion}
 * subclasses (e.g. {@code UniApply}, {@code UniAccept}, {@code BiApply}), so that they replay the
 * captured TTL context even when they are triggered through the {@code tryFire} path.
 *
 * <h2>The problem</h2>
 *
 * <p>TTL context propagation does not work correctly on {@code CompletableFuture} completion stages,
 * because the captured context is not replayed.
 *
 * <p>{@link TtlForkJoinTransformlet} instruments {@link java.util.concurrent.ForkJoinTask} so that
 * each task captures the context at construction time. When the task runs, it is expected to:
 *
 * <ol>
 *     <li>replay the captured context onto its executing thread;
 *     <li>execute under that context;
 *     <li>restore the executing thread to its previous state.
 * </ol>
 *
 * <p>{@code CompletableFuture$Completion} extends {@code ForkJoinTask}, so TTL context propagation
 * should work seamlessly. The problem is that {@code CompletableFuture} may greedily execute its
 * completion stages, bypassing the {@code ForkJoinTask} interface. {@code Completion.tryFire} is the
 * actual work of each completion stage; {@code ForkJoinTask} methods such as {@code doExec} simply
 * call {@code tryFire(ASYNC)}. But when a {@code CompletableFuture} finishes, it calls
 * {@code postComplete}, which iterates the completion stages and calls {@code tryFire} on each one
 * directly instead of going through {@code doExec}. Because TTL only instruments {@code doExec},
 * these completion stages execute without the replay/restore logic and so see the wrong context.
 *
 * <h2>The fix</h2>
 *
 * <p>Force the replay to happen inside the stored closure instead of relying on {@code doExec}, so
 * that the context is replayed no matter how the completion stage is triggered ({@code tryFire} or
 * {@code doExec}):
 *
 * <ol>
 *     <li>match all subclasses of {@code Completion} that hold a functional interface field (named
 *         {@code fn} consistently throughout the {@code CompletableFuture} source);
 *     <li>insert code at the end of the constructors that assign that field, reassigning it to a
 *         wrapped version — see the {@code wrap} overloads below;
 *     <li>mark the class with {@link Enhanced}, so that {@link TtlForkJoinTransformlet} skips the
 *         {@code doExec} replay for it and the context is not replayed twice.
 * </ol>
 *
 * <h2>Ordering</h2>
 *
 * <p>Since UniCompose etc. classes extend ForkJoinTask, it is guaranteed that ForkJoinTask will load before UniCompose. Therefore, TtlForkJoinTransformlet will have already run. As long as TtlForkJoinTransformlet is loaded along with this transformlet, there is no ordering issue.</p>
 *
 * <p>{@code Completion} subclasses without a functional field ({@code UniRelay}, {@code BiRelay},
 * {@code Signaller}, {@code AnyOf}, ...) run no user code, so they are left alone and keep the
 * plain {@code doExec} instrumentation.
 *
 * @see TtlForkJoinTransformlet
 * @see java.util.concurrent.CompletableFuture
 * @since 2.14.5
 */
public class TtlCompletionTransformlet implements JavassistTransformlet {
    /**
     * Marker interface for the {@code Completion} subclasses instrumented by this transformlet.
     * <p>
     * Implementing it signals to {@link TtlForkJoinTransformlet} that {@code doExec} must
     * <b>not</b> replay the captured context, because the wrapped {@code fn} already does.
     */
    public interface Enhanced {
    }

    private static final Logger logger = Logger.getLogger(TtlCompletionTransformlet.class);

    private static final String COMPLETABLE_FUTURE_CLASS_NAME = "java.util.concurrent.CompletableFuture";
    private static final String COMPLETION_CLASS_NAME = COMPLETABLE_FUTURE_CLASS_NAME + "$Completion";
    private static final String FORK_JOIN_TASK_CLASS_NAME = "java.util.concurrent.ForkJoinTask";

    private static final String WRAP_METHOD = TtlCompletionTransformlet.class.getName() + ".wrap";

    static final List<String> supportedFunctionalInterfaces = Arrays.asList(
            Runnable.class.getName(),
            Function.class.getName(),
            Consumer.class.getName(),
            BiFunction.class.getName(),
            BiConsumer.class.getName()
    );

    @Override
    public void doTransform(@NonNull final ClassInfo classInfo) throws IOException, NotFoundException, CannotCompileException {
        // cheap pre-filter before touching javassist: Completion is package-private to
        // java.util.concurrent and all its subclasses are nested classes of CompletableFuture
        if (!isClassOrInnerClass(classInfo.getClassName(), COMPLETABLE_FUTURE_CLASS_NAME)) {
            return;
        }

        final CtClass clazz = classInfo.getCtClass();

        if (!isCompletionSubclass(clazz)) {
            return;
        }

        final CtField fnField = findFunctionalField(clazz);

        if (fnField == null) {  // no wrappable field: e.g. UniRelay/Signaller, which run no user code
            return;
        }

        final CtConstructor constructor = findConstructorAssignableToField(clazz, fnField.getType());

        if (constructor == null) {
            return;
        }

        // ClassInfo builds its ClassPool over the *original* class files, so the javassist compiler
        // cannot see that field unless it is declared on ForkJoinTask in this pool too. This pool is
        // never used to write ForkJoinTask back out, so the declaration is compile-time only.
        declareTemporaryCapturedFieldOnForkJoinTask(clazz.getClassPool());

        // The JDK declares these fields non-final (they are nulled out once the stage has fired);
        // be defensive anyway, since the injected code assigns to the field.
        if (Modifier.isFinal(fnField.getModifiers())) {
            fnField.setModifiers(fnField.getModifiers() & ~Modifier.FINAL);
        }

        final String code = "this." + fnField.getName() + " = " + WRAP_METHOD
            + "(this." + fnField.getName() + ", this." + capturedFieldName + ");";
        logger.info("insert code after constructor " + signatureOfMethod(constructor) + " of class "
                + clazz.getName() + ": " + code);
        constructor.insertAfter(code);

        // tell TtlForkJoinTransformlet to not replay again in doExec. Safe unconditionally:
        // findFunctionalField only matches types that have a real, replaying wrap overload.
        clazz.addInterface(clazz.getClassPool().get(Enhanced.class.getName()));

        classInfo.setModified();
    }

    /**
     * Whether {@code clazz} is {@code CompletableFuture$Completion} or a subclass of it.
     * <p>
     * {@link CtClass#subclassOf(CtClass)} matches on class name only and never touches the
     * argument's {@link ClassPool}, so resolving {@code Completion} from {@code clazz}'s own pool
     * is fine. Note it swallows a {@link NotFoundException} raised while walking the super chain
     * and answers {@code false}; the {@link ClassPool#get} here still fails loudly if
     * {@code Completion} itself cannot be resolved.
     */
    static boolean isCompletionSubclass(@NonNull final CtClass clazz) throws NotFoundException {
        return clazz.subclassOf(clazz.getClassPool().get(COMPLETION_CLASS_NAME));
    }

    /**
     * The (single, in practice) instance field of {@code clazz} whose type is one of
     * {@link #supportedFunctionalInterfaces}.
     * <p>
     * Matching on the supported types rather than on {@code @FunctionalInterface} keeps
     * instrumentation and the {@link #wrap} overloads in lockstep: a field this returns is always
     * one the wrapper can actually replay for.
     */
    @Nullable
    static CtField findFunctionalField(@NonNull final CtClass clazz) throws NotFoundException {
        for (CtField field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && supportedFunctionalInterfaces.contains(field.getType().getName())) {
                return field;
            }
        }
        return null;
    }

    /**
     * The constructor of {@code clazz} that take a parameter assignable to {@code fieldType},
     * i.e. the one that can be assigning the functional field.
     */
    @Nullable
    static CtConstructor findConstructorAssignableToField(@NonNull final CtClass clazz,
                                                          @NonNull final CtClass fieldType) throws NotFoundException {
        for (CtConstructor constructor : clazz.getDeclaredConstructors()) {
            for (CtClass parameterType : constructor.getParameterTypes()) {
                if (parameterType.subtypeOf(fieldType)) {
                    return constructor;
                }
            }
        }
        return null;
    }

    /**
     * Declare captured$field$added$by$ttl temporarily on ForkJoinTask class so that this agent's added code can reference it without compile error. This is not persisted, so does not conflict with TtlForkJoinTransformlet's declaration.
     */
    private static void declareTemporaryCapturedFieldOnForkJoinTask(@NonNull final ClassPool classPool)
        throws NotFoundException, CannotCompileException {
        final CtClass forkJoinTask = classPool.get(FORK_JOIN_TASK_CLASS_NAME);
        // forkJoinTask is guaranteed to never have the field since it is obtained from a fresh ClassPool
        forkJoinTask.addField(TtlForkJoinTransformlet.makeCapturedField(forkJoinTask));
    }

    ////////////////////////////////////////////////////////////////////////////////
    // the wrappers, called from the code injected into the Completion constructors
    ////////////////////////////////////////////////////////////////////////////////

    public static Runnable wrap(final Runnable fn, final Object captured) {
        return () -> {
            final Object backup = TransmittableThreadLocal.Transmitter.replay(captured);
            try {
                fn.run();
            } finally {
                TransmittableThreadLocal.Transmitter.restore(backup);
            }
        };
    }

    public static <T, R> Function<T, R> wrap(final Function<T, R> fn, final Object captured) {
        return t -> {
            final Object backup = TransmittableThreadLocal.Transmitter.replay(captured);
            try {
                return fn.apply(t);
            } finally {
                TransmittableThreadLocal.Transmitter.restore(backup);
            }
        };
    }

    public static <T> Consumer<T> wrap(final Consumer<T> fn, final Object captured) {
        return t -> {
            final Object backup = TransmittableThreadLocal.Transmitter.replay(captured);
            try {
                fn.accept(t);
            } finally {
                TransmittableThreadLocal.Transmitter.restore(backup);
            }
        };
    }

    public static <T, U, R> BiFunction<T, U, R> wrap(final BiFunction<T, U, R> fn, final Object captured) {
        return (t, u) -> {
            final Object backup = TransmittableThreadLocal.Transmitter.replay(captured);
            try {
                return fn.apply(t, u);
            } finally {
                TransmittableThreadLocal.Transmitter.restore(backup);
            }
        };
    }

    public static <T, U> BiConsumer<T, U> wrap(final BiConsumer<T, U> fn, final Object captured) {
        return (t, u) -> {
            final Object backup = TransmittableThreadLocal.Transmitter.replay(captured);
            try {
                fn.accept(t, u);
            } finally {
                TransmittableThreadLocal.Transmitter.restore(backup);
            }
        };
    }

    /**
     * Catch-all overload, selected by the javassist compiler for a functional field whose static
     * type matches none of the overloads above.
     * <p>
     * {@link #findFunctionalField} only ever matches the types in
     * {@link #supportedFunctionalInterfaces}, and every one of those has a real overload above, so
     * this is unreachable in practice. If it is ever called, {@code supportedFunctionalInterfaces}
     * and the {@code wrap} overloads have drifted apart.
     * <p>
     * There is no way to replay/restore a type whose shape we don't know, so it returns {@code fn}
     * unwrapped. It logs at {@link Level#SEVERE} rather than {@code WARNING} on purpose: the default
     * {@code STDERR} logger implementation discards everything below {@code SEVERE}, and dropping
     * context propagation silently is exactly what this overload exists to prevent.
     */
    public static <T> T wrap(final T fn, final Object captured) {
        logger.log(Level.SEVERE, "no wrap overload for functional interface type " + (fn == null ? "null" : fn.getClass().getName())
            + ", TTL context will not be replayed for this CompletableFuture completion stage.", null);
        return fn;
    }
}
