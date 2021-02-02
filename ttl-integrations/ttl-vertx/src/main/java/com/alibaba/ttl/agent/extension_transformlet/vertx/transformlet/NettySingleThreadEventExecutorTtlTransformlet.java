package com.alibaba.ttl.agent.extension_transformlet.vertx.transformlet;

import com.alibaba.ttl.threadpool.agent.transformlet.helper.AbstractExecutorTtlTransformlet;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link com.alibaba.ttl.threadpool.agent.transformlet.TtlTransformlet}
 * for {@link io.netty.util.concurrent.SingleThreadEventExecutor}.
 *
 * @see io.netty.util.concurrent.SingleThreadEventExecutor
 * @see io.vertx.core.eventbus.EventBus
 * @see io.vertx.core.impl.EventLoopContext
 * @see io.vertx.core.eventbus.Message
 */
public final class NettySingleThreadEventExecutorTtlTransformlet extends AbstractExecutorTtlTransformlet {
    private static final Set<String> EXECUTOR_CLASS_NAMES = new HashSet<String>();

    static {
        EXECUTOR_CLASS_NAMES.add("io.netty.util.concurrent.SingleThreadEventExecutor");
    }

    private static final String THREAD_FACTORY_CLASS_NAME = "java.util.concurrent.ThreadFactory";

    public NettySingleThreadEventExecutorTtlTransformlet(boolean disableInheritableForThreadPool) {
        super(EXECUTOR_CLASS_NAMES, disableInheritableForThreadPool);
        System.out.println("================================================");
    }
    public NettySingleThreadEventExecutorTtlTransformlet() {
        super(EXECUTOR_CLASS_NAMES, false);
    }
}
