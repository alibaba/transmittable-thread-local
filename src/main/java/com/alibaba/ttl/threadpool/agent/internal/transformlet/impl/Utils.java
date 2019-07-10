package com.alibaba.ttl.threadpool.agent.internal.transformlet.impl;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.spi.TtlEnhanced;
import com.alibaba.ttl.threadpool.agent.internal.logging.Logger;
import javassist.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;

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
    static String signatureOfMethod(final CtBehavior method) throws NotFoundException {
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

    static CtClass getCtClass(final byte[] classFileBuffer, final ClassLoader classLoader) throws IOException {
        final ClassPool classPool = new ClassPool(true);
        if (classLoader == null) {
            classPool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
        } else {
            classPool.appendClassPath(new LoaderClassPath(classLoader));
        }

        final CtClass clazz = classPool.makeClass(new ByteArrayInputStream(classFileBuffer), false);
        clazz.defrost();
        return clazz;
    }


    static String renamedMethodNameByTtl(CtMethod method) {
        return "original$" + method.getName() + "$method$renamed$by$ttl";
    }

    static void doTryFinallyForMethod(CtMethod method, String beforeCode, String finallyCode) throws CannotCompileException, NotFoundException {
        doTryFinallyForMethod(method, renamedMethodNameByTtl(method), beforeCode, finallyCode);
    }

    static void doTryFinallyForMethod(CtMethod method, String renamedMethodName, String beforeCode, String finallyCode) throws CannotCompileException, NotFoundException {
        final CtClass clazz = method.getDeclaringClass();
        final CtMethod new_method = CtNewMethod.copy(method, clazz, null);

        // rename original method, and set to private method(avoid reflect out renamed method unexpectedly)
        method.setName(renamedMethodName);
        method.setModifiers(method.getModifiers()
                & ~Modifier.PUBLIC /* remove public */
                & ~Modifier.PROTECTED /* remove protected */
                | Modifier.PRIVATE /* add private */);

        // set new method implementation
        final String code = "{\n" +
                beforeCode + "\n" +
                "try {\n" +
                "    return " + renamedMethodName + "($$);\n" +
                "} finally {\n" +
                "    " + finallyCode + "\n" +
                "} }";
        new_method.setBody(code);
        clazz.addMethod(new_method);
        logger.info("insert code around method " + signatureOfMethod(method) + " of class " + clazz.getName() + ": " + code);
    }

    @SuppressWarnings("unused")
    public static Object doCaptureWhenNotTtlEnhanced(Object obj) {
        if (obj instanceof TtlEnhanced) return null;
        else return TransmittableThreadLocal.Transmitter.capture();
    }

}
