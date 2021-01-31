package com.alibaba.ttl.agent.extension_transformlet.sample.transformlet;

import com.alibaba.ttl.agent.extension_transformlet.sample.biz.ToBeTransformedClass;
import com.alibaba.ttl.threadpool.agent.logging.Logger;
import com.alibaba.ttl.threadpool.agent.transformlet.ClassInfo;
import com.alibaba.ttl.threadpool.agent.transformlet.TtlTransformlet;
import com.alibaba.ttl.threadpool.agent.transformlet.javassist.CannotCompileException;
import com.alibaba.ttl.threadpool.agent.transformlet.javassist.CtClass;
import com.alibaba.ttl.threadpool.agent.transformlet.javassist.CtMethod;
import com.alibaba.ttl.threadpool.agent.transformlet.javassist.NotFoundException;

import java.io.IOException;

/**
 * {@link TtlTransformlet} for {@link ToBeTransformedClass}.
 *
 * <B><I>Caution:</I></B><br>
 * MUST use string constant for class/method name!
 * <p>
 * MUST NOT use {@code Class<?> class = ToBeTransformedClass.class} to get the class to be transformed({@code ToBeTransformedClass}),
 * {@code ToBeTransformedClass.class} operation will force to load the class to be transformed,
 * and cause the Transformlet to <b>SKIP</b> the class transform!
 */
public class SampleExtensionTransformlet implements TtlTransformlet {
    private static final Logger logger = Logger.getLogger(SampleExtensionTransformlet.class);

    public static final String TO_BE_TRANSFORMED_CLASS_NAME = "com.alibaba.ttl.agent.extension_transformlet.sample.biz.ToBeTransformedClass";
    public static final String TO_BE_TRANSFORMED_METHOD = "toBeTransformedMethod";

    public void doTransform(ClassInfo classInfo) throws IOException, NotFoundException, CannotCompileException {
        if (!classInfo.getClassName().equals(TO_BE_TRANSFORMED_CLASS_NAME)) return;

        final CtClass ctClass = classInfo.getCtClass();
        final CtMethod method = ctClass.getDeclaredMethod(TO_BE_TRANSFORMED_METHOD);

        final String code = "$1 *= 2;";
        method.insertBefore(code);
        logger.info("[SampleExtensionTransformlet] insert code before method " + TO_BE_TRANSFORMED_METHOD
            + " of class " + method.getDeclaringClass().getName() + ": " + code);

        classInfo.setModified();
    }
}
