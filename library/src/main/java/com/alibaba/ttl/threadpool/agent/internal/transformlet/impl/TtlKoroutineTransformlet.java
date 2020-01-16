package com.alibaba.ttl.threadpool.agent.internal.transformlet.impl;

import com.alibaba.ttl.threadpool.agent.internal.logging.Logger;
import com.alibaba.ttl.threadpool.agent.internal.transformlet.ClassInfo;
import com.alibaba.ttl.threadpool.agent.internal.transformlet.JavassistTransformlet;
import edu.umd.cs.findbugs.annotations.NonNull;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.io.IOException;

import static com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.Utils.signatureOfMethod;

/**
 * TTL {@link JavassistTransformlet} for {@code koltin coroutine(koroutine)}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 2.12.0
 */
public class TtlKoroutineTransformlet implements JavassistTransformlet {
    private static final Logger logger = Logger.getLogger(TtlKoroutineTransformlet.class);

    private static final String KOROUTINE_CONTEXT_KT_CLASS_NAME = "kotlinx.coroutines.CoroutineContextKt";

    public static final String KOROUTINE_SCOPE_CLASS_NAME = "kotlinx.coroutines.CoroutineScope";
    public static final String KOROUTINE_CONTEXT_CLASS_NAME = "kotlin.coroutines.CoroutineContext";

    public TtlKoroutineTransformlet() {
    }

    @Override
    public void doTransform(@NonNull final ClassInfo classInfo) throws IOException, NotFoundException, CannotCompileException {
        if (classInfo.getClassName().equals(KOROUTINE_CONTEXT_KT_CLASS_NAME)) {
            updateNewCoroutineContext_plusTtlContext(classInfo.getCtClass());
            classInfo.setModified();
        }
    }

    private static void updateNewCoroutineContext_plusTtlContext(CtClass clazz) throws NotFoundException, CannotCompileException {
        final CtClass coroutineScopeClass = clazz.getClassPool().get(KOROUTINE_SCOPE_CLASS_NAME);
        final CtClass coroutineContextClass = clazz.getClassPool().get(KOROUTINE_CONTEXT_CLASS_NAME);
        final CtMethod newCoroutineContext = clazz.getDeclaredMethod("newCoroutineContext", new CtClass[]{coroutineScopeClass, coroutineContextClass});

        String code = "if ($2 != null) { $2 = $2.plus(com.alibaba.ttl.kotlin.coroutine.TtlCoroutineContextKt.ttlContext()); }";
        logger.info("insert code before method " + signatureOfMethod(newCoroutineContext) + " of class " + newCoroutineContext.getDeclaringClass().getName() + ": " + code);
        newCoroutineContext.insertBefore(code);
    }
}
