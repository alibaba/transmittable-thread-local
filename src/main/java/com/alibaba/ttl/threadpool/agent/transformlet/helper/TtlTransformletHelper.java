package com.alibaba.ttl.threadpool.agent.transformlet.helper;

import com.alibaba.ttl.spi.TtlEnhanced;
import com.alibaba.ttl.threadpool.agent.transformlet.TtlTransformlet;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javassist.*;

import java.lang.reflect.Modifier;

import static com.alibaba.ttl.TransmittableThreadLocal.Transmitter.capture;

/**
 * Helper methods for {@link TtlTransformlet} implementation.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 2.13.0
 */
public final class TtlTransformletHelper {

    // ======== Javassist Object ToString Helper ========

    /**
     * Output string like {@code public ScheduledFuture scheduleAtFixedRate(Runnable, long, long, TimeUnit)}
     * for {@link  java.util.concurrent.ScheduledThreadPoolExecutor#scheduleAtFixedRate}.
     *
     * @param method method object
     * @return method signature string
     */
    @NonNull
    public static String signatureOfMethod(@NonNull final CtBehavior method) throws NotFoundException {
        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(Modifier.toString(method.getModifiers()));
        if (method instanceof CtMethod) {
            final String returnType = ((CtMethod) method).getReturnType().getSimpleName();
            stringBuilder.append(" ").append(returnType);
        }
        stringBuilder.append(" ").append(method.getName()).append("(");

        final CtClass[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            CtClass parameterType = parameterTypes[i];
            if (i != 0) stringBuilder.append(", ");
            stringBuilder.append(parameterType.getSimpleName());
        }

        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    // ======== Method Transform Helper ========

    @NonNull
    public static String renamedMethodNameByTtl(@NonNull CtMethod method) {
        return "original$" + method.getName() + "$method$renamed$by$ttl";
    }

    public static String addTryFinallyToMethod(@NonNull CtMethod method, @NonNull String beforeCode, @NonNull String finallyCode) throws CannotCompileException, NotFoundException {
        return addTryFinallyToMethod(method, renamedMethodNameByTtl(method), beforeCode, finallyCode);
    }

    /**
     * Add {@code try-finally} logic to method.
     *
     * @return the body code of method rewritten
     */
    public static String addTryFinallyToMethod(@NonNull CtMethod method, @NonNull String nameForOriginalMethod, @NonNull String beforeCode, @NonNull String finallyCode) throws CannotCompileException, NotFoundException {
        final CtClass clazz = method.getDeclaringClass();

        final CtMethod newMethod = CtNewMethod.copy(method, clazz, null);
        // rename original method, and set to private method(avoid reflect out renamed method unexpectedly)
        newMethod.setName(nameForOriginalMethod);
        newMethod.setModifiers(newMethod.getModifiers()
                & ~Modifier.PUBLIC /* remove public */
                & ~Modifier.PROTECTED /* remove protected */
                | Modifier.PRIVATE /* add private */);
        clazz.addMethod(newMethod);

        final String returnOp;
        if (method.getReturnType() == CtClass.voidType) {
            returnOp = "";
        } else {
            returnOp = "return ";
        }
        // set new method implementation
        final String code = "{\n" +
                beforeCode + "\n" +
                "try {\n" +
                "    " + returnOp + nameForOriginalMethod + "($$);\n" +
                "} finally {\n" +
                "    " + finallyCode + "\n" +
                "} }";
        method.setBody(code);

        return code;
    }

    // ======== CRR Helper ========

    @Nullable
    public static Object doCaptureIfNotTtlEnhanced(@Nullable Object obj) {
        if (obj instanceof TtlEnhanced) return null;
        else return capture();
    }

    private TtlTransformletHelper() {
        throw new InstantiationError("Must not instantiate this class");
    }
}
