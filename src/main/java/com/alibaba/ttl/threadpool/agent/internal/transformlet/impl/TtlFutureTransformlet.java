package com.alibaba.ttl.threadpool.agent.internal.transformlet.impl;

import com.alibaba.ttl.threadpool.agent.internal.logging.Logger;
import com.alibaba.ttl.threadpool.agent.internal.transformlet.ClassInfo;
import com.alibaba.ttl.threadpool.agent.internal.transformlet.JavassistTransformlet;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.Utils.signatureOfMethod;

/**
 * @author: tk
 * @since: 2021/1/15
 * 包装io.vertx.core.Handler
 * 解决vertx回调里拿不到ttl值的问题
 */
public class TtlFutureTransformlet implements JavassistTransformlet {
    private static final Logger logger = Logger.getLogger(TtlExecutorTransformlet.class);

    private static final Set<String> CALLBACK_EXECUTOR_CLASS_NAMES = new HashSet<String>();

    private static final Map<String, String> PARAM_TYPE_NAME_TO_DECORATE_METHOD_CLASS = new HashMap<String, String>();

    private static final String HANDLER_INVOKE_CLASS_NAME = "io.vertx.core.Future";
    private static final String HANDLER_CLASS_NAME = "io.vertx.core.Handler";
    private static final String TTL_HANDLER_CLASS_NAME = "com.alibaba.ttl.TtlVertxHandler";

    static {

        CALLBACK_EXECUTOR_CLASS_NAMES.add(HANDLER_INVOKE_CLASS_NAME);

        PARAM_TYPE_NAME_TO_DECORATE_METHOD_CLASS.put(HANDLER_CLASS_NAME, TTL_HANDLER_CLASS_NAME);
    }

    public TtlFutureTransformlet() {
    }

    @Override
    public void doTransform(@NonNull final ClassInfo classInfo) throws IOException, NotFoundException, CannotCompileException {
        final CtClass clazz = classInfo.getCtClass();
        if (CALLBACK_EXECUTOR_CLASS_NAMES.contains(classInfo.getClassName())) {
            for (CtMethod method : clazz.getDeclaredMethods()) {
                updateSubmitMethodsOfExecutorClass_decorateToTtlWrapperAndSetAutoWrapperAttachment(method);
            }

            classInfo.setModified();
        } else {
            if (clazz.isPrimitive() || clazz.isArray() || clazz.isInterface() || clazz.isAnnotation()) {
                return;
            }

            logger.info("Transforming class " + classInfo.getClassName());
        }
    }

    /**
     * @see com.alibaba.ttl.TtlRunnable#get(Runnable, boolean, boolean)
     * @see com.alibaba.ttl.TtlCallable#get(Callable, boolean, boolean)
     * @see com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.Utils#setAutoWrapperAttachment(Object)
     */
    @SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE") // [ERROR] Format string should use %n rather than \n
    private void updateSubmitMethodsOfExecutorClass_decorateToTtlWrapperAndSetAutoWrapperAttachment(@NonNull final CtMethod method) throws NotFoundException, CannotCompileException {
        final int modifiers = method.getModifiers();
        if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers)) return;

        CtClass[] parameterTypes = method.getParameterTypes();
        StringBuilder insertCode = new StringBuilder();
        for (int i = 0; i < parameterTypes.length; i++) {
            final String paramTypeName = parameterTypes[i].getName();
            if (PARAM_TYPE_NAME_TO_DECORATE_METHOD_CLASS.containsKey(paramTypeName)) {
                //如果参数类型是io.vertx.core.Handler.则只包装setHandler
                if (paramTypeName.equals(HANDLER_CLASS_NAME)) {
                    if (!"setHandler".equals(method.getName())) {
                        return;
                    }
                }
                String code = String.format(
                    // decorate to TTL wrapper,
                    // and then set AutoWrapper attachment/Tag
                    "$%d = %s.get($%1$d, false, true);"
                        + "\ncom.alibaba.ttl.threadpool.agent.internal.transformlet.impl.Utils.setAutoWrapperAttachment($%1$d);",
                    i + 1, PARAM_TYPE_NAME_TO_DECORATE_METHOD_CLASS.get(paramTypeName));
                logger.info("insert code before method " + signatureOfMethod(method) + " of class " + method.getDeclaringClass().getName() + ": " + code);
                insertCode.append(code);
            }
        }
        if (insertCode.length() > 0) method.insertBefore(insertCode.toString());
    }
}
