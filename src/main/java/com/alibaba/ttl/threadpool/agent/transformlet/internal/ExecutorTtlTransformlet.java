package com.alibaba.ttl.threadpool.agent.transformlet.internal;

import com.alibaba.ttl.threadpool.agent.transformlet.TtlTransformlet;
import com.alibaba.ttl.threadpool.agent.transformlet.helper.AbstractExecutorTtlTransformlet;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link TtlTransformlet} for {@link java.util.concurrent.Executor}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @author wuwen5 (wuwen.55 at aliyun dot com)
 * @see java.util.concurrent.Executor
 * @see java.util.concurrent.ExecutorService
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see java.util.concurrent.ScheduledThreadPoolExecutor
 * @see java.util.concurrent.Executors
 * @since 2.5.1
 */
public final class ExecutorTtlTransformlet extends AbstractExecutorTtlTransformlet implements TtlTransformlet {
    private static final Set<String> executorClassNames = new HashSet<String>();

    static {
        executorClassNames.add(THREAD_POOL_EXECUTOR_CLASS_NAME);
        executorClassNames.add("java.util.concurrent.ScheduledThreadPoolExecutor");
    }

    public ExecutorTtlTransformlet(boolean disableInheritableForThreadPool) {
        super(executorClassNames, disableInheritableForThreadPool);
    }
}
