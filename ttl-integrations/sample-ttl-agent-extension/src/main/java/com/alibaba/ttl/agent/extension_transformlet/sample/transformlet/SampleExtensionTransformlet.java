package com.alibaba.ttl.agent.extension_transformlet.sample.transformlet;

import com.alibaba.ttl.threadpool.agent.transformlet.javassist.CannotCompileException;
import com.alibaba.ttl.threadpool.agent.transformlet.javassist.CtClass;
import com.alibaba.ttl.threadpool.agent.transformlet.javassist.CtMethod;
import com.alibaba.ttl.threadpool.agent.transformlet.javassist.NotFoundException;
import com.alibaba.ttl.threadpool.agent.logging.Logger;
import com.alibaba.ttl.threadpool.agent.transformlet.ClassInfo;
import com.alibaba.ttl.threadpool.agent.transformlet.TtlTransformlet;

import java.io.IOException;

public class SampleExtensionTransformlet implements TtlTransformlet {
    private static final Logger logger = Logger.getLogger(SampleExtensionTransformlet.class);

    // *CAUTION*:
    //
    // MUST use string constant for class/method name!
    //
    // MUST NOT use `Class<?> class = ToBeTransformedClass.class` to get the class to be transformed(`ToBeTransformedClass`).
    //
    // `ToBeTransformedClass.class` will force to load the class to be transformed(`ToBeTransformedClass`),
    // make the Transformlet SKIP the class transform!

    public static final String TO_BE_TRANSFORMED_CLASS_NAME = "com.alibaba.ttl.agent.extension_transformlet.sample.biz.ToBeTransformedClass";
    public static final String TO_BE_TRANSFORMED_METHOD = "toBeTransformedMethod";

    public void doTransform(ClassInfo classInfo) throws IOException, NotFoundException, CannotCompileException {
        // logger.info("[SampleJavassistTransformlet]: doTransform " + classInfo.getClassName() + " of classloader " + classInfo.getClassLoader());

        if (!classInfo.getClassName().equals(TO_BE_TRANSFORMED_CLASS_NAME)) return;

        final CtClass ctClass = classInfo.getCtClass();
        final CtMethod method = ctClass.getDeclaredMethod(TO_BE_TRANSFORMED_METHOD);

        final String code = "$1 *= 2;";
        method.insertBefore(code);
        logger.info("[SampleJavassistTransformlet] insert code before method " + TO_BE_TRANSFORMED_METHOD
            + " of class " + method.getDeclaringClass().getName() + ": " + code);

        classInfo.setModified();
    }
}
