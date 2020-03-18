package com.alibaba.ttl.threadpool.agent.internal.transformlet.impl;

import com.alibaba.ttl.TtlRunnable;
import com.alibaba.ttl.spi.TtlAttachments;
import com.alibaba.ttl.spi.TtlEnhanced;
import com.alibaba.ttl.threadpool.agent.internal.logging.Logger;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javassist.*;

import java.lang.reflect.Modifier;

import static com.alibaba.ttl.TransmittableThreadLocal.Transmitter.capture;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 2.6.0
 */
public class Utils {
    private static final Logger logger = Logger.getLogger(Utils.class);

    /**
     * String like {@code public ScheduledFuture scheduleAtFixedRate(Runnable, long, long, TimeUnit)}
     * for {@link  java.util.concurrent.ScheduledThreadPoolExecutor#scheduleAtFixedRate}.
     *
     * @param method method object
     * @return method signature string
     */
    @NonNull
    static String signatureOfMethod(@NonNull final CtBehavior method) throws NotFoundException {
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

    @NonNull
    static String renamedMethodNameByTtl(@NonNull CtMethod method) {
        return "original$" + method.getName() + "$method$renamed$by$ttl";
    }

    static void doTryFinallyForMethod(@NonNull CtMethod method, @NonNull String beforeCode, @NonNull String finallyCode) throws CannotCompileException, NotFoundException {
        doTryFinallyForMethod(method, renamedMethodNameByTtl(method), beforeCode, finallyCode);
    }

    static void doTryFinallyForMethod(@NonNull CtMethod method, @NonNull String renamedMethodName, @NonNull String beforeCode, @NonNull String finallyCode) throws CannotCompileException, NotFoundException {
        final CtClass clazz = method.getDeclaringClass();
        final CtMethod newMethod = CtNewMethod.copy(method, clazz, null);

        // rename original method, and set to private method(avoid reflect out renamed method unexpectedly)
        method.setName(renamedMethodName);
        method.setModifiers(method.getModifiers()
                & ~Modifier.PUBLIC /* remove public */
                & ~Modifier.PROTECTED /* remove protected */
                | Modifier.PRIVATE /* add private */);

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
                "    " + returnOp + renamedMethodName + "($$);\n" +
                "} finally {\n" +
                "    " + finallyCode + "\n" +
                "} }";
        newMethod.setBody(code);
        clazz.addMethod(newMethod);
        logger.info("insert code around method " + signatureOfMethod(newMethod) + " of class " + clazz.getName() + ": " + code);
    }

    @Nullable
    public static Object doCaptureWhenNotTtlEnhanced(@Nullable Object obj) {
        if (obj instanceof TtlEnhanced) return null;
        else return capture();
    }

    public static void setAutoWrapperAttachment(@Nullable Object ttlAttachment) {
        if (notTtlAttachments(ttlAttachment)) return;
        ((TtlAttachments) ttlAttachment).setTtlAttachment(TtlAttachments.KEY_IS_AUTO_WRAPPER, true);
    }

    @Nullable
    public static Runnable unwrapIfIsAutoWrapper(@Nullable Runnable runnable) {
        if (isAutoWrapper(runnable)) return TtlRunnable.unwrap(runnable);
        else return runnable;
    }

    private static boolean notTtlAttachments(@Nullable Object ttlAttachment) {
        return !(ttlAttachment instanceof TtlAttachments);
    }

    private static boolean isAutoWrapper(@Nullable Runnable ttlAttachments) {
        if (notTtlAttachments(ttlAttachments)) return false;

        final Boolean value = ((TtlAttachments) ttlAttachments).getTtlAttachment(TtlAttachments.KEY_IS_AUTO_WRAPPER);
        if (value == null) return false;

        return value;
    }
}
