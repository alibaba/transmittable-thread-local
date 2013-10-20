package com.alibaba.mtc.threadpool.agent;

import com.alibaba.mtc.MtContextCallable;
import com.alibaba.mtc.MtContextRunnable;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author ding.lid
 * @since 0.9.0
 */
public class MtContextTransformer implements ClassFileTransformer {
    private static final Logger logger = Logger.getLogger(MtContextTransformer.class.getName());

    private static final String RUNNABLE_CLASS_NAME = "java.lang.Runnable";
    private static final String CALLABLE_CLASS_NAME = "java.util.concurrent.Callable";

    private static final String MT_CONTEXT_RUNNABLE_CLASS_NAME = MtContextRunnable.class.getName();
    private static final String MT_CONTEXT_CALLABLE_CLASS_NAME = MtContextCallable.class.getName();

    private static final String THREAD_POOL_CLASS_FILE = "java.util.concurrent.ThreadPoolExecutor".replace('.', '/');
    private static final String SCHEDULER_CLASS_FILE = "java.util.concurrent.ScheduledThreadPoolExecutor".replace('.', '/');

    private static String toClassName(String classFile) {
        return classFile.replace('/', '.');
    }

    @Override
    public byte[] transform(ClassLoader loader, String classFile, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classFileBuffer) throws IllegalClassFormatException {
        if (THREAD_POOL_CLASS_FILE.equals(classFile) || SCHEDULER_CLASS_FILE.equals(classFile)) {
            final String className = toClassName(classFile);

            try {
                logger.warning("Transforming class " + className);
                CtClass clazz = ClassPool.getDefault().makeClass(new ByteArrayInputStream(classFileBuffer), false);
                clazz.defrost();

                for (CtMethod method : clazz.getDeclaredMethods()) {
                    updateMethod(method);
                }
                return clazz.toBytecode();
            } catch (Throwable t) {
                String msg = "Fail to transform class " + className + ", cause: " + t.getMessage();
                logger.severe(msg);
                throw new IllegalStateException(msg, t);
            }
        }
        return null;
    }

    static final Set<String> updateMethodNames = new HashSet<String>();

    static {
        updateMethodNames.add("execute");
        updateMethodNames.add("submit");
        updateMethodNames.add("schedule");
        updateMethodNames.add("scheduleAtFixedRate");
        updateMethodNames.add("scheduleWithFixedDelay");
    }

    static void updateMethod(CtMethod method) throws NotFoundException, CannotCompileException {
        if (!updateMethodNames.contains(method.getName())) {
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
                String code = String.format("$%d = %s.get($%d);", i + 1, MT_CONTEXT_RUNNABLE_CLASS_NAME, i + 1);
                logger.info("insert code before method " + method + " of class " + method.getDeclaringClass().getName() + ": " + code);
                insertCode.append(code);
            } else if (CALLABLE_CLASS_NAME.equals(paraType.getName())) {
                String code = String.format("$%d = %s.get($%d);", i + 1, MT_CONTEXT_CALLABLE_CLASS_NAME, i + 1);
                logger.info("insert code before method " + method + " of class " + method.getDeclaringClass().getName() + ": " + code);
                insertCode.append(code);
            }
        }
        if (insertCode.length() > 0) {
            method.insertBefore(insertCode.toString());
        }
    }
}
