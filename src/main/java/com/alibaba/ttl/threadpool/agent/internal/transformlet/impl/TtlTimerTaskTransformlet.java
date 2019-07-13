package com.alibaba.ttl.threadpool.agent.internal.transformlet.impl;

import com.alibaba.ttl.threadpool.agent.internal.logging.Logger;
import com.alibaba.ttl.threadpool.agent.internal.transformlet.ClassInfo;
import com.alibaba.ttl.threadpool.agent.internal.transformlet.JavassistTransformlet;
import edu.umd.cs.findbugs.annotations.NonNull;
import javassist.*;

import java.io.IOException;

import static com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.Utils.doTryFinallyForMethod;

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
    public void doTransform(@NonNull final ClassInfo classInfo) throws IOException, NotFoundException, CannotCompileException {
        if (TIMER_TASK_CLASS_NAME.equals(classInfo.getClassName())) return; // No need transform TimerTask class

        final CtClass clazz = classInfo.getCtClass();

        if (clazz.isPrimitive() || clazz.isArray() || clazz.isInterface() || clazz.isAnnotation()) {
            return;
        }
        // class contains method `void run()` ?
        try {
            final CtMethod runMethod = clazz.getDeclaredMethod(RUN_METHOD_NAME, new CtClass[0]);
            if (!CtClass.voidType.equals(runMethod.getReturnType())) return;
        } catch (NotFoundException e) {
            return;
        }
        if (!clazz.subclassOf(clazz.getClassPool().get(TIMER_TASK_CLASS_NAME))) return;

        logger.info("Transforming class " + classInfo.getClassName());

        updateTimerTaskClass(clazz);
        classInfo.setModified();
    }

    /**
     * @see Utils#doCaptureWhenNotTtlEnhanced(java.lang.Object)
     */
    private void updateTimerTaskClass(@NonNull final CtClass clazz) throws CannotCompileException, NotFoundException {
        final String className = clazz.getName();

        // add new field
        final String capturedFieldName = "captured$field$added$by$ttl";
        final CtField capturedField = CtField.make("private final Object " + capturedFieldName + ";", clazz);
        clazz.addField(capturedField, "com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.Utils.doCaptureWhenNotTtlEnhanced(this);");
        logger.info("add new field " + capturedFieldName + " to class " + className);

        final CtMethod runMethod = clazz.getDeclaredMethod(RUN_METHOD_NAME, new CtClass[0]);

        final String beforeCode = "Object backup = com.alibaba.ttl.TransmittableThreadLocal.Transmitter.replay(" + capturedFieldName + ");";
        final String finallyCode = "com.alibaba.ttl.TransmittableThreadLocal.Transmitter.restore(backup);";

        doTryFinallyForMethod(runMethod, beforeCode, finallyCode);
    }
}
