package com.alibaba.ttl.threadpool.agent.transformlet.helper;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.alibaba.ttl.threadpool.agent.logging.Logger;
import com.alibaba.ttl.threadpool.agent.transformlet.ClassInfo;
import com.alibaba.ttl.threadpool.agent.transformlet.TtlTransformlet;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javassist.*;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.alibaba.ttl.threadpool.agent.transformlet.helper.TtlTransformletHelper.signatureOfMethod;

/**
 * Abstract {@link TtlTransformlet} for {@link java.util.concurrent.Executor} and its subclass.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @author wuwen5 (wuwen.55 at aliyun dot com)
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see java.util.concurrent.Executor
 * @see java.util.concurrent.ExecutorService
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see java.util.concurrent.ScheduledThreadPoolExecutor
 * @see java.util.concurrent.Executors
 * @since 2.13.0
 */
public abstract class AbstractExecutorTtlTransformlet implements TtlTransformlet {
    protected static final String RUNNABLE_CLASS_NAME = "java.lang.Runnable";
    protected static final String CALLABLE_CLASS_NAME = "java.util.concurrent.Callable";

    protected static final String TTL_RUNNABLE_CLASS_NAME = "com.alibaba.ttl.TtlRunnable";
    protected static final String TTL_CALLABLE_CLASS_NAME = "com.alibaba.ttl.TtlCallable";

    protected static final String THREAD_FACTORY_CLASS_NAME = "java.util.concurrent.ThreadFactory";
    protected static final String THREAD_POOL_EXECUTOR_CLASS_NAME = "java.util.concurrent.ThreadPoolExecutor";

    protected final Logger logger = Logger.getLogger(getClass());

    protected final Set<String> executorClassNames;
    protected final boolean disableInheritableForThreadPool;

    private final Map<String, String> paramTypeNameToDecorateMethodClass = new HashMap<String, String>();

    /**
     * @param executorClassNames the executor class names to be transformed
     */
    public AbstractExecutorTtlTransformlet(Set<String> executorClassNames, boolean disableInheritableForThreadPool) {
        this.executorClassNames = executorClassNames;
        this.disableInheritableForThreadPool = disableInheritableForThreadPool;

        paramTypeNameToDecorateMethodClass.put(RUNNABLE_CLASS_NAME, TTL_RUNNABLE_CLASS_NAME);
        paramTypeNameToDecorateMethodClass.put(CALLABLE_CLASS_NAME, TTL_CALLABLE_CLASS_NAME);
    }

    @Override
    public final void doTransform(@NonNull final ClassInfo classInfo) throws IOException, NotFoundException, CannotCompileException {
        final CtClass clazz = classInfo.getCtClass();
        if (executorClassNames.contains(classInfo.getClassName())) {
            for (CtMethod method : clazz.getDeclaredMethods()) {
                updateSubmitMethodsOfExecutorClass_decorateToTtlWrapperAndSetAutoWrapperAttachment(method);
            }

            if (disableInheritableForThreadPool) updateConstructorDisableInheritable(clazz);

            classInfo.setModified();
        } else {
            if (clazz.isPrimitive() || clazz.isArray() || clazz.isInterface() || clazz.isAnnotation()) {
                return;
            }
            if (!clazz.subclassOf(clazz.getClassPool().get(THREAD_POOL_EXECUTOR_CLASS_NAME))) return;

            logger.info("Transforming class " + classInfo.getClassName());

            final boolean modified = updateBeforeAndAfterExecuteMethodOfExecutorSubclass(clazz);
            if (modified) classInfo.setModified();
        }
    }

    /**
     * @see com.alibaba.ttl.TtlRunnable#get(Runnable, boolean, boolean)
     * @see com.alibaba.ttl.TtlCallable#get(Callable, boolean, boolean)
     * @see com.alibaba.ttl.spi.TtlAttachmentsDelegate#setAutoWrapperAttachment(Object)
     */
    @SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE") // [ERROR] Format string should use %n rather than \n
    private void updateSubmitMethodsOfExecutorClass_decorateToTtlWrapperAndSetAutoWrapperAttachment(@NonNull final CtMethod method) throws NotFoundException, CannotCompileException {
        final int modifiers = method.getModifiers();
        if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers)) return;

        CtClass[] parameterTypes = method.getParameterTypes();
        StringBuilder insertCode = new StringBuilder();
        for (int i = 0; i < parameterTypes.length; i++) {
            final String paramTypeName = parameterTypes[i].getName();
            if (paramTypeNameToDecorateMethodClass.containsKey(paramTypeName)) {
                String code = String.format(
                    // decorate to TTL wrapper,
                    // and then set AutoWrapper attachment/Tag
                    "    $%d = %s.get($%1$d, false, true);"
                        + "\n    com.alibaba.ttl.spi.TtlAttachmentsDelegate.setAutoWrapperAttachment($%1$d);",
                    i + 1, paramTypeNameToDecorateMethodClass.get(paramTypeName));
                logger.info("insert code before method " + signatureOfMethod(method) + " of class " + method.getDeclaringClass().getName() + ":\n" + code);
                insertCode.append(code);
            }
        }
        if (insertCode.length() > 0) method.insertBefore(insertCode.toString());
    }

    /**
     * @see TtlExecutors#getDisableInheritableThreadFactory(java.util.concurrent.ThreadFactory)
     */
    private void updateConstructorDisableInheritable(@NonNull final CtClass clazz) throws NotFoundException, CannotCompileException {
        for (CtConstructor constructor : clazz.getDeclaredConstructors()) {
            final CtClass[] parameterTypes = constructor.getParameterTypes();
            final StringBuilder insertCode = new StringBuilder();
            for (int i = 0; i < parameterTypes.length; i++) {
                final String paramTypeName = parameterTypes[i].getName();
                if (THREAD_FACTORY_CLASS_NAME.equals(paramTypeName)) {
                    String code = String.format("$%d = com.alibaba.ttl.threadpool.TtlExecutors.getDisableInheritableThreadFactory($%<d);", i + 1);
                    logger.info("insert code before method " + signatureOfMethod(constructor) + " of class " + constructor.getDeclaringClass().getName() + ": " + code);
                    insertCode.append(code);
                }
            }
            if (insertCode.length() > 0) constructor.insertBefore(insertCode.toString());
        }
    }

    /**
     * @see com.alibaba.ttl.spi.TtlAttachmentsDelegate#unwrapIfIsAutoWrapper(Object)
     */
    private boolean updateBeforeAndAfterExecuteMethodOfExecutorSubclass(@NonNull final CtClass clazz) throws NotFoundException, CannotCompileException {
        final CtClass runnableClass = clazz.getClassPool().get(RUNNABLE_CLASS_NAME);
        final CtClass threadClass = clazz.getClassPool().get("java.lang.Thread");
        final CtClass throwableClass = clazz.getClassPool().get("java.lang.Throwable");
        boolean modified = false;

        try {
            final CtMethod beforeExecute = clazz.getDeclaredMethod("beforeExecute", new CtClass[]{threadClass, runnableClass});
            // unwrap runnable if IsAutoWrapper
            String code = "$2 = com.alibaba.ttl.spi.TtlAttachmentsDelegate.unwrapIfIsAutoWrapper($2);";
            logger.info("insert code before method " + signatureOfMethod(beforeExecute) + " of class " + beforeExecute.getDeclaringClass().getName() + ": " + code);
            beforeExecute.insertBefore(code);
            modified = true;
        } catch (NotFoundException e) {
            // clazz does not override beforeExecute method, do nothing.
        }

        try {
            final CtMethod afterExecute = clazz.getDeclaredMethod("afterExecute", new CtClass[]{runnableClass, throwableClass});
            // unwrap runnable if IsAutoWrapper
            String code = "$1 = com.alibaba.ttl.spi.TtlAttachmentsDelegate.unwrapIfIsAutoWrapper($1);";
            logger.info("insert code before method " + signatureOfMethod(afterExecute) + " of class " + afterExecute.getDeclaringClass().getName() + ": " + code);
            afterExecute.insertBefore(code);
            modified = true;
        } catch (NotFoundException e) {
            // clazz does not override afterExecute method, do nothing.
        }

        return modified;
    }
}
