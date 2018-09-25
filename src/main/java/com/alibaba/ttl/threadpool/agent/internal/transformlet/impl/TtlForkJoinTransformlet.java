package com.alibaba.ttl.threadpool.agent.internal.transformlet.impl;

import com.alibaba.ttl.threadpool.agent.internal.logging.Logger;
import com.alibaba.ttl.threadpool.agent.internal.transformlet.JavassistTransformlet;
import javassist.*;

import java.io.IOException;
import java.lang.reflect.Modifier;

import static com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.Utils.getCtClass;
import static com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.Utils.signatureOfMethod;

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

        final String capturedFieldName = "captured$field$add$by$ttl";
        final CtField capturedField = CtField.make("private final Object " + capturedFieldName + ";", clazz);
        clazz.addField(capturedField, "com.alibaba.ttl.TransmittableThreadLocal.Transmitter.capture();");
        logger.info("add new field " + capturedFieldName + " to class " + className);

        final String doExec_methodName = "doExec";
        final CtMethod doExecMethod = clazz.getDeclaredMethod(doExec_methodName, new CtClass[0]);
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
                "Object backup = com.alibaba.ttl.TransmittableThreadLocal.Transmitter.replay(" + capturedFieldName + ");\n" +
                "try {\n" +
                "    return " + original_doExec_method_rename + "($$);\n" +
                "} finally {\n" +
                "    com.alibaba.ttl.TransmittableThreadLocal.Transmitter.restore(backup);\n" +
                "}\n" + "}";
        new_doExecMethod.setBody(code);
        clazz.addMethod(new_doExecMethod);
        logger.info("insert code around method " + signatureOfMethod(doExecMethod) + " of class " + className + ": " + code);
    }
}
