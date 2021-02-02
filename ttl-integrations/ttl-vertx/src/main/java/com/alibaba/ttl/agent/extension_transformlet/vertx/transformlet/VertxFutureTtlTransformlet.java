package com.alibaba.ttl.agent.extension_transformlet.vertx.transformlet;

import com.alibaba.ttl.threadpool.agent.logging.Logger;
import com.alibaba.ttl.threadpool.agent.transformlet.ClassInfo;
import com.alibaba.ttl.threadpool.agent.transformlet.TtlTransformlet;
import com.alibaba.ttl.threadpool.agent.transformlet.javassist.CannotCompileException;
import com.alibaba.ttl.threadpool.agent.transformlet.javassist.CtClass;
import com.alibaba.ttl.threadpool.agent.transformlet.javassist.CtMethod;
import com.alibaba.ttl.threadpool.agent.transformlet.javassist.NotFoundException;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URLClassLoader;

import static com.alibaba.ttl.threadpool.agent.transformlet.helper.TtlTransformletHelper.signatureOfMethod;

/**
 * {@link TtlTransformlet} for {@link io.vertx.core.Future}.
 *
 * @author tk (305809299 at qq dot com)
 * @see com.alibaba.ttl.integration.vertx4.TtlVertxHandler
 * @see io.vertx.core.Future
 * @see io.vertx.core.Handler
 * @see sun.misc.Launcher.AppClassLoader
 * @see URLClassLoader#findClass(String)
 */
public class VertxFutureTtlTransformlet implements TtlTransformlet {
    private static final Logger logger = Logger.getLogger(VertxFutureTtlTransformlet.class);

    private static final String HANDLER_CLASS_NAME = "io.vertx.core.Handler";
    private static final String TTL_HANDLER_CLASS_NAME = "com.alibaba.ttl.agent.extension_transformlet.vertx.TtlVertxHandler";
    private static final String FUTURE_CLASS_NAME = "io.vertx.core.Future";

    @Override
    public void doTransform(@NonNull ClassInfo classInfo) throws CannotCompileException, NotFoundException, IOException {
        // FIXME: add logic for subclasses of Future

        final CtClass clazz = classInfo.getCtClass();
        if (FUTURE_CLASS_NAME.contains(classInfo.getClassName())) {
            for (CtMethod method : clazz.getDeclaredMethods()) {
                updateSetHandlerMethodsOfFutureClass_decorateToTtlWrapperAndSetAutoWrapperAttachment(method);
            }
            classInfo.setModified();
        }
    }

    private void updateSetHandlerMethodsOfFutureClass_decorateToTtlWrapperAndSetAutoWrapperAttachment(CtMethod method) throws NotFoundException, CannotCompileException {
        final int modifiers = method.getModifiers();
        if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers) || Modifier.isAbstract(modifiers)) return;

        CtClass[] parameterTypes = method.getParameterTypes();
        StringBuilder insertCode = new StringBuilder();
        for (int i = 0; i < parameterTypes.length; i++) {
            final String paramTypeName = parameterTypes[i].getName();
            if (HANDLER_CLASS_NAME.equals(paramTypeName)) {
                String code = String.format(
                    // decorate to TTL wrapper,
                    // and then set AutoWrapper attachment/Tag
                    "$%d = %s.get($%1$d, false, true);"
                        + "\n    com.alibaba.ttl.spi.TtlAttachmentsDelegate.setAutoWrapperAttachment($%1$d);",
                    i + 1, TTL_HANDLER_CLASS_NAME);
                logger.info("insert code before method " + signatureOfMethod(method) + " of class " + method.getDeclaringClass().getName() + ":\n" + code);
                insertCode.append(code);
            }
        }
        if (insertCode.length() > 0) method.insertBefore(insertCode.toString());
    }
}
