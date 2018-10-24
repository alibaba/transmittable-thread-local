package com.alibaba.ttl.threadpool.agent.internal.transformlet.impl;

import com.alibaba.ttl.threadpool.agent.internal.logging.Logger;
import com.alibaba.ttl.threadpool.agent.internal.transformlet.JavassistTransformlet;
import javassist.*;

import java.io.IOException;
import java.lang.reflect.Modifier;

import static com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.Utils.getCtClass;
import static com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.Utils.signatureOfMethod;

/**
 * TTL {@link JavassistTransformlet} for {@link java.util.TimerTask}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @author wuwen5 (wuwen.55 at aliyun dot com)
 * @see java.util.TimerTask
 * @see java.util.Timer
 * @since 2.7.0
 */
public class TtlTimerTaskTransformlet implements JavassistTransformlet {
    private static final Logger logger = Logger.getLogger(TtlTimerTaskTransformlet.class);

    private static final String TIMER_TASK_CLASS_NAME = "java.util.TimerTask";
    private static final String RUN_METHOD_NAME = "run";

    @Override
    public byte[] doTransform(String className, byte[] classFileBuffer, ClassLoader loader) throws IOException, NotFoundException, CannotCompileException {
        if (TIMER_TASK_CLASS_NAME.equals(className)) return null; // No need transform TimerTask class

        final CtClass clazz = getCtClass(classFileBuffer, loader);

        if (clazz.isPrimitive() || clazz.isArray() || clazz.isInterface() || clazz.isAnnotation()) {
            return null;
        }
        // class contains method `void run()` ?
        try {
            final CtMethod runMethod = clazz.getDeclaredMethod(RUN_METHOD_NAME, new CtClass[0]);
            if (!CtClass.voidType.equals(runMethod.getReturnType())) return null;
        } catch (NotFoundException e) {
            return null;
        }
        if (!clazz.subclassOf(clazz.getClassPool().get(TIMER_TASK_CLASS_NAME))) return null;

        logger.info("Transforming class " + className);
        updateTimerTaskClass(clazz);
        return clazz.toBytecode();
    }

    private void updateTimerTaskClass(final CtClass clazz) throws CannotCompileException, NotFoundException {
        // add new field
        final String className = clazz.getName();

        final String capturedFieldName = "captured$field$add$by$ttl";
        final CtField capturedField = CtField.make("private final Object " + capturedFieldName + ";", clazz);
        clazz.addField(capturedField, "com.alibaba.ttl.TransmittableThreadLocal.Transmitter.capture();");
        logger.info("add new field " + capturedFieldName + " to class " + className);

        final CtMethod runMethod = clazz.getDeclaredMethod(RUN_METHOD_NAME, new CtClass[0]);
        final CtMethod new_runMethod = CtNewMethod.copy(runMethod, clazz, null);

        // rename original run method, and set to private method(avoid reflect out renamed method unexpectedly)
        final String original_run_method_rename = "original$" + runMethod.getName() + "$method$renamed$by$ttl";
        runMethod.setName(original_run_method_rename);
        runMethod.setModifiers(runMethod.getModifiers() & ~Modifier.PUBLIC /* remove public */ | Modifier.PRIVATE /* add private */);

        // set new run method implementation
        final String code = "{\n" +
                "Object backup = com.alibaba.ttl.TransmittableThreadLocal.Transmitter.replay(" + capturedFieldName + ");\n" +
                "try {\n" +
                "    return " + original_run_method_rename + "($$);\n" +
                "} finally {\n" +
                "    com.alibaba.ttl.TransmittableThreadLocal.Transmitter.restore(backup);\n" +
                "}\n" + "}";
        new_runMethod.setBody(code);
        clazz.addMethod(new_runMethod);
        logger.info("insert code around method " + signatureOfMethod(runMethod) + " of class " + className + ": " + code);
    }
}
