package com.alibaba.ttl.threadpool.agent.transformlet.internal;

import com.alibaba.ttl.threadpool.TtlForkJoinPoolHelper;
import com.alibaba.ttl.threadpool.agent.TtlAgent;
import com.alibaba.ttl.threadpool.agent.logging.Logger;
import com.alibaba.ttl.threadpool.agent.transformlet.TtlTransformlet;
import edu.umd.cs.findbugs.annotations.NonNull;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TtlForkJoinTask;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * {@link TtlTransformlet} for {@link java.util.concurrent.ForkJoinTask}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @author wuwen5 (wuwen.55 at aliyun dot com)
 * @see java.util.concurrent.ForkJoinPool
 * @see java.util.concurrent.ForkJoinTask
 * @since 2.5.1
 */
public final class ForkJoinTtlTransformlet implements TtlTransformlet {

    private static final Logger logger = Logger.getLogger(ForkJoinTtlTransformlet.class);

    private static final String FORK_JOIN_POOL_CLASS_NAME = "java.util.concurrent.ForkJoinPool";

    private final boolean disableInheritableForThreadPool;

    public ForkJoinTtlTransformlet() {
        this.disableInheritableForThreadPool = TtlAgent.isDisableInheritableForThreadPool();
    }

    @Override
    public AgentBuilder doTransform(@NonNull AgentBuilder agentBuilder) {
        agentBuilder = agentBuilder.type(named(FORK_JOIN_POOL_CLASS_NAME))
            .transform(new AgentBuilder.Transformer() {
                @Override
                public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
                    // 防止监听的FutureTask与提交到线程池中唤醒的task不是同一个FutureTask
                    // 这里可能会改变之前代码中行为造成之间代码的强制类型转换失败
                    builder = builder.visit(Advice.to(ForkJoinTaskAdvice.class)
                        .on(namedOneOf("submit")
                            .and(ElementMatchers.takesArgument(0, ForkJoinTask.class))));
                    builder = builder.visit(Advice.to(ForkJoinTaskAdvice.class)
                        .on(named("externalPush")));
                    if (disableInheritableForThreadPool) {
                        builder.visit(Advice.to(ConstructorAdvice.class)
                            .on(isConstructor()
                                .and(takesArgument(1, ForkJoinPool.ForkJoinWorkerThreadFactory.class))));
                    }
                    return builder;
                }
            });
        agentBuilder = agentBuilder.type(named("java.util.concurrent.ForkJoinPool$WorkQueue"))
            .transform(new AgentBuilder.Transformer() {
                @Override
                public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
                    builder = builder.visit(Advice.to(ForkJoinTaskAdvice.class)
                        .on(named("push")));
                    if (disableInheritableForThreadPool) {
                        builder.visit(Advice.to(ConstructorAdvice.class)
                            .on(isConstructor()
                                .and(takesArgument(1, ForkJoinPool.ForkJoinWorkerThreadFactory.class))));
                    }
                    return builder;
                }
            });
        return agentBuilder;
    }

    private static class ForkJoinTaskAdvice {
        @Advice.OnMethodEnter
        public static void beforeMethod(@Advice.Argument(value = 0, readOnly = false) ForkJoinTask<?> forkJoinTask) {
            forkJoinTask = TtlForkJoinTask.get(forkJoinTask, true, true);
        }
    }

    private static class ConstructorAdvice {
        @Advice.OnMethodEnter
        public static void beforeConstruct(@Advice.Argument(value = 1, readOnly = false) ForkJoinPool.ForkJoinWorkerThreadFactory factory) {
            factory = TtlForkJoinPoolHelper.getDisableInheritableForkJoinWorkerThreadFactory(factory);
        }
    }
}
