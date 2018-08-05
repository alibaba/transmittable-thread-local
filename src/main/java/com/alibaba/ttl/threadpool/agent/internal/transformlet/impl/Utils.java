package com.alibaba.ttl.threadpool.agent.internal.transformlet.impl;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * @since 2.6.0
 * @author Jerry Lee (oldratlee at gmail dot com)
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
}
