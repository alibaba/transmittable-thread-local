package com.alibaba.ttl.threadpool.agent.transformlet;

import com.alibaba.ttl.threadpool.agent.JavassistTransformlet;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import com.alibaba.ttl.threadpool.agent.internal.logging.Logger;

import static com.alibaba.ttl.threadpool.agent.transformlet.Utils.signatureOfMethod;

/**
 * TTL {@link JavassistTransformlet} for {@link java.util.concurrent.Executor}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @author wuwen5 (wuwen.55 at aliyun dot com)
 * @see java.util.concurrent.Executor
 * @see java.util.concurrent.ExecutorService
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see java.util.concurrent.ScheduledThreadPoolExecutor
 * @see java.util.concurrent.Executors
 * @since 2.5.1
 */
public class TtlExecutorTransformlet implements JavassistTransformlet {
    private static final Logger logger = Logger.getLogger(TtlExecutorTransformlet.class);

    private static final String TTL_RUNNABLE_CLASS_NAME = "com.alibaba.ttl.TtlRunnable";
    private static final String TTL_CALLABLE_CLASS_NAME = "com.alibaba.ttl.TtlCallable";

    private static final String RUNNABLE_CLASS_NAME = "java.lang.Runnable";
    private static final String CALLABLE_CLASS_NAME = "java.util.concurrent.Callable";

    private static Set<String> EXECUTOR_CLASS_NAMES = new HashSet<String>();

    static {
        EXECUTOR_CLASS_NAMES.add("java.util.concurrent.ThreadPoolExecutor");
        EXECUTOR_CLASS_NAMES.add("java.util.concurrent.ScheduledThreadPoolExecutor");
    }

    @Override
    public boolean needTransform(String className) {
        return EXECUTOR_CLASS_NAMES.contains(className);
    }

    @Override
    public void doTransform(CtClass clazz) throws NotFoundException, CannotCompileException, IOException {
        for (CtMethod method : clazz.getDeclaredMethods()) {
            updateMethodOfExecutorClass(clazz, method);
        }
    }

    private void updateMethodOfExecutorClass(final CtClass clazz, final CtMethod method) throws NotFoundException, CannotCompileException {
        if (method.getDeclaringClass() != clazz) {
            return;
        }
        final int modifiers = method.getModifiers();
        if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers)) {
            return;
        }

        CtClass[] parameterTypes = method.getParameterTypes();
        StringBuilder insertCode = new StringBuilder();
        for (int i = 0; i < parameterTypes.length; i++) {
            CtClass paraType = parameterTypes[i];
            if (RUNNABLE_CLASS_NAME.equals(paraType.getName())) {
                String code = String.format("$%d = %s.get($%d, false, true);", i + 1, TTL_RUNNABLE_CLASS_NAME, i + 1);
                logger.info("insert code before method " + signatureOfMethod(method) + " of class " + method.getDeclaringClass().getName() + ": " + code);
                insertCode.append(code);
            } else if (CALLABLE_CLASS_NAME.equals(paraType.getName())) {
                String code = String.format("$%d = %s.get($%d, false, true);", i + 1, TTL_CALLABLE_CLASS_NAME, i + 1);
                logger.info("insert code before method " + signatureOfMethod(method) + " of class " + method.getDeclaringClass().getName() + ": " + code);
                insertCode.append(code);
            }
        }
        if (insertCode.length() > 0) {
            method.insertBefore(insertCode.toString());
        }
    }

}
