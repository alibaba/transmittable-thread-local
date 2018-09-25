package com.alibaba.ttl.threadpool.agent.internal.transformlet.impl;

import javassist.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 2.6.0
 */
class Utils {
    /**
     * String like {@code ScheduledFuture scheduleAtFixedRate(Runnable, long, long, TimeUnit)}
     * for {@link  java.util.concurrent.ScheduledThreadPoolExecutor#scheduleAtFixedRate}.
     *
     * @param method method object
     * @return method signature string
     */
    static String signatureOfMethod(final CtMethod method) throws NotFoundException {
        final StringBuilder stringBuilder = new StringBuilder();

        final String returnType = method.getReturnType().getSimpleName();
        final String methodName = method.getName();
        stringBuilder.append(returnType).append(" ")
                .append(methodName).append("(");

        final CtClass[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            CtClass parameterType = parameterTypes[i];
            if (i != 0) {
                stringBuilder.append(", ");
            }
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
}
