package com.alibaba.ttl.threadpool.agent.internal.transformlet.impl;

import com.alibaba.ttl.threadpool.agent.internal.logging.Logger;
import com.alibaba.ttl.threadpool.agent.internal.transformlet.JavassistTransformlet;

import java.lang.reflect.Method;
import java.net.URLClassLoader;

/**
 * TTL {@link JavassistTransformlet} for {@link io.vertx.core.Handler}.
 *
 * @author: tk
 * @since: 2021/1/15
 * @see com.alibaba.ttl.TtlVertxHandler
 * @see io.vertx.core.Handler
 * @see sun.misc.Launcher.AppClassLoader
 * @see URLClassLoader#findClass(String)
 */
public class TtlVertxFutureTransformlet extends BaseTtlTransformlet {
    private static final Logger LOGGER = Logger.getLogger(TtlVertxFutureTransformlet.class);

    private static final String HANDLER_INVOKE_CLASS_NAME = "io.vertx.core.Future";
    private static final String HANDLER_CLASS_NAME = "io.vertx.core.Handler";
    private static final String TTL_HANDLER_CLASS_NAME = "com.alibaba.ttl.TtlVertxHandler";

    private static final String DECORATE_SET_HANDLER_METHOD = "setHandler";

    static {
        CALL_CLASS_NAMES.add(HANDLER_INVOKE_CLASS_NAME);

        PARAM_TYPE_NAME_TO_DECORATE_METHOD_CLASS.put(HANDLER_CLASS_NAME, TTL_HANDLER_CLASS_NAME);

        DECORATE_METHODS_NAME.add(DECORATE_SET_HANDLER_METHOD);
    }

    @Override
    protected void loadClass() {
        try {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            Method findClass = URLClassLoader.class.getDeclaredMethod("findClass", String.class);
            findClass.setAccessible(true);
            findClass.invoke(contextClassLoader, TTL_HANDLER_CLASS_NAME);
        } catch (Throwable t) {
            LOGGER.info("load class failed in TtlFutureTransformlet. 【className:" + TTL_HANDLER_CLASS_NAME + "】" +
                "cause:" + t.getMessage());
        }
    }

    @Override
    protected boolean needDecorateToTtlWrapper(String methodName) {
        return DECORATE_METHODS_NAME.contains(methodName);
    }
}
