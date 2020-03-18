package com.alibaba.ttl.threadpool.agent.internal.transformlet.impl;

import com.alibaba.ttl.spi.TtlEnhanced;
import com.alibaba.ttl.threadpool.agent.internal.logging.Logger;
import com.alibaba.ttl.threadpool.agent.internal.transformlet.ClassInfo;
import com.alibaba.ttl.threadpool.agent.internal.transformlet.JavassistTransformlet;
import edu.umd.cs.findbugs.annotations.NonNull;
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
    private static final String FORK_JOIN_POOL_CLASS_NAME = "java.util.concurrent.ForkJoinPool";
    private static final String FORK_JOIN_WORKER_THREAD_FACTORY_CLASS_NAME = "java.util.concurrent.ForkJoinPool$ForkJoinWorkerThreadFactory";

    private final boolean disableInheritableForThreadPool;

    public TtlForkJoinTransformlet(boolean disableInheritableForThreadPool) {
        this.disableInheritableForThreadPool = disableInheritableForThreadPool;
    }

    @Override
    public void doTransform(@NonNull final ClassInfo classInfo) throws IOException, NotFoundException, CannotCompileException {
        if (FORK_JOIN_TASK_CLASS_NAME.equals(classInfo.getClassName())) {
            updateForkJoinTaskClass(classInfo.getCtClass());
            classInfo.setModified();
        } else if (disableInheritableForThreadPool && FORK_JOIN_POOL_CLASS_NAME.equals(classInfo.getClassName())) {
            updateConstructorDisableInheritable(classInfo.getCtClass());
            classInfo.setModified();
        }
    }

    /**
     * @see Utils#doCaptureWhenNotTtlEnhanced(java.lang.Object)
     */
    private void updateForkJoinTaskClass(@NonNull final CtClass clazz) throws CannotCompileException, NotFoundException {
        final String className = clazz.getName();

        // add new field
        final String capturedFieldName = "captured$field$added$by$ttl";
        final CtField capturedField = CtField.make("private final Object " + capturedFieldName + ";", clazz);
        clazz.addField(capturedField, "com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.Utils.doCaptureWhenNotTtlEnhanced(this);");
        logger.info("add new field " + capturedFieldName + " to class " + className);

        final CtMethod doExecMethod = clazz.getDeclaredMethod("doExec", new CtClass[0]);
        final String doExec_renamed_method_name = renamedMethodNameByTtl(doExecMethod);

        final String beforeCode = "if (this instanceof " + TtlEnhanced.class.getName() + ") {\n" + // if the class is already TTL enhanced(eg: com.alibaba.ttl.TtlRecursiveTask)
                "    return " + doExec_renamed_method_name + "($$);\n" +                           // return directly/do nothing
                "}\n" +
                "Object backup = com.alibaba.ttl.TransmittableThreadLocal.Transmitter.replay(" + capturedFieldName + ");";

        final String finallyCode = "com.alibaba.ttl.TransmittableThreadLocal.Transmitter.restore(backup);";

        doTryFinallyForMethod(doExecMethod, doExec_renamed_method_name, beforeCode, finallyCode);
    }

    private void updateConstructorDisableInheritable(@NonNull final CtClass clazz) throws NotFoundException, CannotCompileException {
        for (CtConstructor constructor : clazz.getDeclaredConstructors()) {
            final CtClass[] parameterTypes = constructor.getParameterTypes();
            final StringBuilder insertCode = new StringBuilder();
            for (int i = 0; i < parameterTypes.length; i++) {
                final String paramTypeName = parameterTypes[i].getName();
                if (FORK_JOIN_WORKER_THREAD_FACTORY_CLASS_NAME.equals(paramTypeName)) {
                    String code = String.format("$%d = com.alibaba.ttl.threadpool.TtlForkJoinPoolHelper.getDisableInheritableForkJoinWorkerThreadFactory($%<d);", i + 1);
                    logger.info("insert code before method " + signatureOfMethod(constructor) + " of class " + constructor.getDeclaringClass().getName() + ": " + code);
                    insertCode.append(code);
                }
            }
            if (insertCode.length() > 0) constructor.insertBefore(insertCode.toString());
        }
    }
}
