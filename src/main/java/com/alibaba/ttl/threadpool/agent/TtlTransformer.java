package com.alibaba.ttl.threadpool.agent;

import com.alibaba.ttl.TtlCallable;
import com.alibaba.ttl.TtlRunnable;
import javassist.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TTL {@link ClassFileTransformer} of Java Agent
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @author wuwen5 (wuwen.55 at aliyun dot com)
 * @see java.util.concurrent.Executor
 * @see java.util.concurrent.ExecutorService
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see java.util.concurrent.ScheduledThreadPoolExecutor
 * @see java.util.concurrent.Executors
 * @since 0.9.0
 */
public class TtlTransformer implements ClassFileTransformer {
    private static final Logger logger = Logger.getLogger(TtlTransformer.class.getName());

    private static final String TTL_RUNNABLE_CLASS_NAME = TtlRunnable.class.getName();
    private static final String TTL_CALLABLE_CLASS_NAME = TtlCallable.class.getName();

    private static final String RUNNABLE_CLASS_NAME = "java.lang.Runnable";
    private static final String CALLABLE_CLASS_NAME = "java.util.concurrent.Callable";
    private static final String TIMER_TASK_CLASS_NAME = "java.util.TimerTask";

    private static Set<String> EXECUTOR_CLASS_NAMES = new HashSet<>();

    static {
        EXECUTOR_CLASS_NAMES.add("java.util.concurrent.ThreadPoolExecutor");
        EXECUTOR_CLASS_NAMES.add("java.util.concurrent.ScheduledThreadPoolExecutor");
    }

    private static final String FORK_JOIN_TASK_CLASS_NAME = "java.util.concurrent.ForkJoinTask";
    private static final String TTL_RECURSIVE_ACTION_CLASS_NAME = "com.alibaba.ttl.TtlRecursiveAction";
    private static final String TTL_RECURSIVE_TASK_CLASS_NAME = "com.alibaba.ttl.TtlRecursiveTask";

    private static final byte[] EMPTY_BYTE_ARRAY = {};

    @Override
    public byte[] transform(final ClassLoader loader, final String classFile, final Class<?> classBeingRedefined,
                            final ProtectionDomain protectionDomain, final byte[] classFileBuffer) {
        try {
            // Lambda has no class file, no need to transform, just return.
            if (classFile == null) {
                return EMPTY_BYTE_ARRAY;
            }

            final String className = toClassName(classFile);
            if (EXECUTOR_CLASS_NAMES.contains(className)) {
                logger.info("Transforming class " + className);
                final CtClass clazz = getCtClass(classFileBuffer, loader);

                for (CtMethod method : clazz.getDeclaredMethods()) {
                    updateMethodOfExecutorClass(clazz, method);
                }

                return clazz.toBytecode();

            } else if (FORK_JOIN_TASK_CLASS_NAME.equals(className)) {
                logger.info("Transforming class " + className);
                final CtClass clazz = getCtClass(classFileBuffer, loader);

                updateForkJoinTaskClass(className, clazz);

                return clazz.toBytecode();

            } else if (TIMER_TASK_CLASS_NAME.equals(className)) {
                final CtClass clazz = getCtClass(classFileBuffer, loader);
                while (true) {
                    String name = clazz.getSuperclass().getName();
                    if (Object.class.getName().equals(name)) {
                        break;
                    }
                    if (TIMER_TASK_CLASS_NAME.equals(name)) {
                        logger.info("Transforming class " + className);
                        // FIXME add code here
                        return EMPTY_BYTE_ARRAY;
                    }
                }
            }
        } catch (Throwable t) {
            String msg = "Fail to transform class " + classFile + ", cause: " + t.toString();
            if (logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, msg, t);
            }
            throw new IllegalStateException(msg, t);
        }
        return EMPTY_BYTE_ARRAY;
    }

    private static String toClassName(final String classFile) {
        return classFile.replace('/', '.');
    }

    private static CtClass getCtClass(final byte[] classFileBuffer, final ClassLoader classLoader) throws IOException {
        ClassPool classPool = new ClassPool(true);
        if (classLoader == null) {
            classPool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
        } else {
            classPool.appendClassPath(new LoaderClassPath(classLoader));
        }

        CtClass clazz = classPool.makeClass(new ByteArrayInputStream(classFileBuffer), false);
        clazz.defrost();
        return clazz;
    }

    private static void updateMethodOfExecutorClass(final CtClass clazz, final CtMethod method) throws NotFoundException, CannotCompileException {
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
                logger.info("insert code before method " + method + " of class " + method.getDeclaringClass().getName() + ": " + code);
                insertCode.append(code);
            } else if (CALLABLE_CLASS_NAME.equals(paraType.getName())) {
                String code = String.format("$%d = %s.get($%d, false, true);", i + 1, TTL_CALLABLE_CLASS_NAME, i + 1);
                logger.info("insert code before method " + method + " of class " + method.getDeclaringClass().getName() + ": " + code);
                insertCode.append(code);
            }
        }
        if (insertCode.length() > 0) {
            method.insertBefore(insertCode.toString());
        }
    }

    private static void updateForkJoinTaskClass(final String className, final CtClass clazz) throws CannotCompileException, NotFoundException {
        // add new field
        final String capturedFieldName = "captured$field$add$by$ttl";
        final CtField capturedField = CtField.make("private final java.lang.Object " + capturedFieldName + ";", clazz);
        clazz.addField(capturedField, "com.alibaba.ttl.TransmittableThreadLocal.Transmitter.capture();");
        logger.info("add new field " + capturedFieldName + " to class " + className);

        final String doExec_methodName = "doExec";
        final CtMethod doExecMethod = clazz.getDeclaredMethod(doExec_methodName);
        final CtMethod new_doExecMethod = CtNewMethod.copy(doExecMethod, doExec_methodName, clazz, null);

        // rename original doExec method, and set to private method(avoid reflect out renamed method unexpectedly)
        final String original_doExec_method_rename = "original$doExec$method$renamed$by$ttl";
        doExecMethod.setName(original_doExec_method_rename);
        doExecMethod.setModifiers(doExecMethod.getModifiers() & ~Modifier.PUBLIC /* remove public */ | Modifier.PRIVATE /* add private */);

        // set new doExec method implementation
        final String code = "{\n" +
                // do nothing/directly return, if is TTL ForkJoinTask instance
                "if (this instanceof " + TTL_RECURSIVE_ACTION_CLASS_NAME + " || this instanceof " + TTL_RECURSIVE_TASK_CLASS_NAME + ") {\n" +
                "    return " + original_doExec_method_rename + "($$);\n" +
                "}\n" +
                "java.lang.Object backup = com.alibaba.ttl.TransmittableThreadLocal.Transmitter.replay(" + capturedFieldName + ");\n" +
                "try {\n" +
                "    return " + original_doExec_method_rename + "($$);\n" +
                "} finally {\n" +
                "    com.alibaba.ttl.TransmittableThreadLocal.Transmitter.restore(backup);\n" +
                "}\n" + "}";
        new_doExecMethod.setBody(code);
        clazz.addMethod(new_doExecMethod);
        logger.info("insert code around method " + doExecMethod + " of class " + className + ": " + code);
    }
}
