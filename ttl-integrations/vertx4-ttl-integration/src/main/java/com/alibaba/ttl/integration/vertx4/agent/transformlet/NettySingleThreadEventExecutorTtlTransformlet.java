package com.alibaba.ttl.integration.vertx4.agent.transformlet;

import com.alibaba.ttl.threadpool.agent.transformlet.helper.AbstractExecutorTtlTransformlet;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link com.alibaba.ttl.threadpool.agent.transformlet.TtlTransformlet}
 * for {@link io.netty.util.concurrent.SingleThreadEventExecutor}.
 *
 * @author tk (305809299 at qq dot com)
 * @see io.netty.util.concurrent.SingleThreadEventExecutor
 * @see io.vertx.core.eventbus.EventBus
 * @see io.vertx.core.impl.EventLoopContext
 * @see io.vertx.core.eventbus.Message
 */
public final class NettySingleThreadEventExecutorTtlTransformlet extends AbstractExecutorTtlTransformlet {

    private static Set<String> getExecutorClassNames() {
        Set<String> executorClassNames = new HashSet<>();

        executorClassNames.add("io.netty.util.concurrent.SingleThreadEventExecutor");

        return executorClassNames;
    }

    public NettySingleThreadEventExecutorTtlTransformlet() {
        super(getExecutorClassNames(), false);
    }
}
