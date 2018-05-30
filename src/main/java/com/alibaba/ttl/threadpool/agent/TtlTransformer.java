package com.alibaba.ttl.threadpool.agent;

import com.alibaba.ttl.TtlCallable;
import com.alibaba.ttl.TtlRunnable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javassist.*;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 0.9.0
 * @see java.util.concurrent.Executor
 * @see java.util.concurrent.ExecutorService
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see java.util.concurrent.ScheduledThreadPoolExecutor
 * @see java.util.concurrent.Executors
 */
public class TtlTransformer implements ClassFileTransformer {
    private static final Logger logger = Logger.getLogger(TtlTransformer.class.getName());

    private static final String TTL_RUNNABLE_CLASS_NAME = TtlRunnable.class.getName();
    private static final String TTL_CALLABLE_CLASS_NAME = TtlCallable.class.getName();

    private static final String RUNNABLE_CLASS_NAME = "java.lang.Runnable";
    private static final String CALLABLE_CLASS_NAME = "java.util.concurrent.Callable";
    private static final String TIMER_TASK_CLASS_NAME = "java.util.TimerTask";

    private static final String FORK_JOIN_TASK_CLASS_NAME = "java.util.concurrent.ForkJoinTask";

    private static Set<String> EXECUTOR_CLASS_NAMES = new HashSet<>();

    static {
        EXECUTOR_CLASS_NAMES.add("java.util.concurrent.ThreadPoolExecutor");
        EXECUTOR_CLASS_NAMES.add("java.util.concurrent.ScheduledThreadPoolExecutor");
    }

    private static final byte[] EMPTY_BYTE_ARRAY = {};

    @Override
    public byte[] transform(ClassLoader loader, String classFile, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        try {
            // Lambda has no class file, no need to transform, just return.
            if (classFile == null) {
                return EMPTY_BYTE_ARRAY;
            }

            final String className = toClassName(classFile);
            if (EXECUTOR_CLASS_NAMES.contains(className)) {
                logger.info("Transforming class " + className);
                CtClass clazz = getCtClass(classFileBuffer, loader);

                for (CtMethod method : clazz.getDeclaredMethods()) {
                    updateMethod(clazz, method);
                }
                return clazz.toBytecode();
            } else if (TIMER_TASK_CLASS_NAME.equals(className)) {
                CtClass clazz = getCtClass(classFileBuffer, loader);
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
            } else if (FORK_JOIN_TASK_CLASS_NAME.equals(className)) {
                CtClass clazz = getCtClass(classFileBuffer, loader);

                logger.info("Transforming class " + className);

                CtField captureField = CtField.make("private final com.alibaba.ttl.TransmittableThreadLocal.Capture capture;", clazz);

                clazz.addField(captureField, "com.alibaba.ttl.TransmittableThreadLocal.capture();");

                CtMethod doExecMethod = clazz.getDeclaredMethod("doExec");

                doExecMethod.setName("doExec$0");

                CtMethod mnew = CtNewMethod.copy(doExecMethod, "doExec", clazz, null);

                final String code = "{Object captured = capture.getCapturedRef().get(); " +
                        "if (captured == null || capture.isReleaseTtlValueReferenceAfterRun() && !capture.getCapturedRef().compareAndSet(captured, null)) { " +
                        " throw new IllegalStateException(\"TTL value reference is released after call!\");} " +
                        "Object backup = com.alibaba.ttl.TransmittableThreadLocal.Transmitter.replay(captured); " +
                        "try { return doExec$0($$); } finally { " +
                        "com.alibaba.ttl.TransmittableThreadLocal.Transmitter.restore(backup);}}";

                mnew.setBody(code);

                clazz.addMethod(mnew);

                logger.info("insert code around method " + doExecMethod + " of class " + doExecMethod.getDeclaringClass().getName() + ": " + code);

                return clazz.toBytecode();
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

    private static String toClassName(String classFile) {
        return classFile.replace('/', '.');
    }

    private static CtClass getCtClass(byte[] classFileBuffer, ClassLoader classLoader) throws IOException {
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

    private static void updateMethod(CtClass clazz, CtMethod method) throws NotFoundException, CannotCompileException {
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
}
