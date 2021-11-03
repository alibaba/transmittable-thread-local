package com.alibaba.ttl.threadpool.agent.transformlet.helper;

import com.alibaba.ttl.TtlCallable;
import com.alibaba.ttl.TtlRunnable;
import com.alibaba.ttl.spi.TtlAttachmentsDelegate;
import com.alibaba.ttl.threadpool.TtlExecutors;
import com.alibaba.ttl.threadpool.agent.logging.Logger;
import com.alibaba.ttl.threadpool.agent.transformlet.TtlTransformlet;
import edu.umd.cs.findbugs.annotations.NonNull;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Abstract {@link TtlTransformlet} for {@link java.util.concurrent.Executor} and its subclass.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @author wuwen5 (wuwen.55 at aliyun dot com)
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see java.util.concurrent.Executor
 * @see java.util.concurrent.ExecutorService
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see java.util.concurrent.ScheduledThreadPoolExecutor
 * @see java.util.concurrent.Executors
 * @since 2.13.0
 */
public abstract class AbstractExecutorTtlTransformlet implements TtlTransformlet {

    protected final Logger logger = Logger.getLogger(getClass());

    protected final Set<String> executorClassNames;
    protected final boolean disableInheritableForThreadPool;

    /**
     * @param executorClassNames the executor class names to be transformed
     */
    public AbstractExecutorTtlTransformlet(Set<String> executorClassNames, boolean disableInheritableForThreadPool) {
        this.executorClassNames = Collections.unmodifiableSet(executorClassNames);
        this.disableInheritableForThreadPool = disableInheritableForThreadPool;
    }

    @Override
    public final AgentBuilder doTransform(@NonNull AgentBuilder agentBuilder) {
        String[] executorClassNames = this.executorClassNames.toArray(new String[0]);
        agentBuilder = agentBuilder.type(namedOneOf(executorClassNames))
            .transform(new AgentBuilder.Transformer() {
                @Override
                public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
                    builder = builder.visit(Advice.to(RunnableAdvice.class)
                            .on(isPublic()
                                .and(not(isStatic()))
                                .and(takesArgument(0, Runnable.class))))
                        .visit(Advice.to(CallableAdvice.class)
                            .on(isPublic()
                                .and(not(isStatic()))
                                .and(takesArgument(0, Callable.class))));
                    builder = builder.visit(Advice.to(BeforeTtlAttachmentsAdvice.class)
                        .on(named("beforeExecute")));
                    builder = builder.visit(Advice.to(AfterTtlAttachmentsAdvice.class)
                        .on(named("afterExecute")));
                    if (disableInheritableForThreadPool) {
                        builder = builder.visit(Advice.to(ConstructorAdvice.class)
                            .on(isConstructor()
                                .and(takesArgument(6, ThreadFactory.class))));
                    }
                    return builder;
                }
            });
        return agentBuilder;
    }

    private static class BeforeTtlAttachmentsAdvice {
        @Advice.OnMethodEnter
        public static void beforeMethod(@Advice.Argument(value = 1, readOnly = false) Runnable runnable) {
            runnable = TtlAttachmentsDelegate.unwrapIfIsAutoWrapper(runnable);
        }
    }

    private static class AfterTtlAttachmentsAdvice {
        @Advice.OnMethodEnter
        public static void beforeMethod(@Advice.Argument(value = 0, readOnly = false) Runnable runnable) {
            runnable = TtlAttachmentsDelegate.unwrapIfIsAutoWrapper(runnable);
        }
    }

    private static class RunnableAdvice {
        @Advice.OnMethodEnter
        public static void beforeMethod(@Advice.Argument(value = 0, readOnly = false) Runnable runnable) {
            runnable = TtlRunnable.get(runnable, false, true);
            TtlAttachmentsDelegate.setAutoWrapperAttachment(runnable);
        }
    }

    private static class CallableAdvice {
        @Advice.OnMethodEnter
        public static void beforeMethod(@Advice.Argument(value = 0, readOnly = false) Callable<?> callable) {
            callable = TtlCallable.get(callable, false, true);
            TtlAttachmentsDelegate.setAutoWrapperAttachment(callable);
        }
    }

    private static class ConstructorAdvice {
        @Advice.OnMethodEnter
        public static void beforeConstruct(@Advice.Argument(value = 6, readOnly = false) ThreadFactory threadFactory) {
            threadFactory = TtlExecutors.getDisableInheritableThreadFactory(threadFactory);
        }
    }
}
