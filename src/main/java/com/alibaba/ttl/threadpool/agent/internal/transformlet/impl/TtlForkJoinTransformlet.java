package com.alibaba.ttl.threadpool.agent.internal.transformlet.impl;

import com.alibaba.ttl.threadpool.agent.internal.logging.Logger;
import com.alibaba.ttl.threadpool.agent.internal.transformlet.JavassistTransformlet;
import javassist.*;

import java.io.IOException;

import static com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.Utils.*;

/**
 * TTL {@link JavassistTransformlet} for {@link java.util.concurrent.ForkJoinTask}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @author wuwen5 (wuwen.55 at aliyun dot com)
 * @see java.util.concurrent.ForkJoinPool
 * @see java.util.concurrent.ForkJoinTask
 * @since 2.5.1
 */
public class TtlForkJoinTransformlet implements JavassistTransformlet {
    private static final Logger logger = Logger.getLogger(TtlForkJoinTransformlet.class);

    private static final String FORK_JOIN_TASK_CLASS_NAME = "java.util.concurrent.ForkJoinTask";
    private static final String TTL_RECURSIVE_ACTION_CLASS_NAME = "com.alibaba.ttl.TtlRecursiveAction";
    private static final String TTL_RECURSIVE_TASK_CLASS_NAME = "com.alibaba.ttl.TtlRecursiveTask";

    @Override
    public byte[] doTransform(String className, byte[] classFileBuffer, ClassLoader loader) throws IOException, NotFoundException, CannotCompileException {
        if (FORK_JOIN_TASK_CLASS_NAME.equals(className)) {
            final CtClass clazz = getCtClass(classFileBuffer, loader);
            updateForkJoinTaskClass(clazz);
            return clazz.toBytecode();
        }
        return null;
    }

    private void updateForkJoinTaskClass(final CtClass clazz) throws CannotCompileException, NotFoundException {
        // add new field
        final String className = clazz.getName();

        final String capturedFieldName = "captured$field$added$by$ttl";
        final CtField capturedField = CtField.make("private final Object " + capturedFieldName + ";", clazz);
        clazz.addField(capturedField, "com.alibaba.ttl.TransmittableThreadLocal.Transmitter.capture();");
        logger.info("add new field " + capturedFieldName + " to class " + className);

        final CtMethod doExecMethod = clazz.getDeclaredMethod("doExec", new CtClass[0]);
        final String doExec_renamed_method_rename = renamedMethodNameByTtl(doExecMethod);

        final String beforeCode = "if (this instanceof " + TTL_RECURSIVE_ACTION_CLASS_NAME + " || this instanceof " + TTL_RECURSIVE_TASK_CLASS_NAME + ") {\n" +
                "    return " + doExec_renamed_method_rename + "($$);\n" + // do nothing/directly return, if is TTL ForkJoinTask instance
                "}\n" +
                "Object backup = com.alibaba.ttl.TransmittableThreadLocal.Transmitter.replay(" + capturedFieldName + ");";

        final String finallyCode = "com.alibaba.ttl.TransmittableThreadLocal.Transmitter.restore(backup);";

        doTryFinallyForMethod(doExecMethod, doExec_renamed_method_rename, beforeCode, finallyCode);
    }
}
