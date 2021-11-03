package com.alibaba.ttl.threadpool.agent.transformlet.internal;

import com.alibaba.ttl.TtlTimerTask;
import com.alibaba.ttl.threadpool.agent.logging.Logger;
import com.alibaba.ttl.threadpool.agent.transformlet.TtlTransformlet;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.utility.JavaModule;

import java.util.TimerTask;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

/**
 * {@link TtlTransformlet} for {@link java.util.TimerTask}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @author wuwen5 (wuwen.55 at aliyun dot com)
 * @see java.util.TimerTask
 * @see java.util.Timer
 * @since 2.7.0
 */
public final class TimerTaskTtlTransformlet implements TtlTransformlet {
    private static final Logger logger = Logger.getLogger(TimerTaskTtlTransformlet.class);

    private static final String TIMER_TASK_CLASS_NAME = "java.util.Timer";
    private static final String SCHED_METHOD_NAME = "sched";

    @Override
    public AgentBuilder doTransform(AgentBuilder agentBuilder) {
        agentBuilder = agentBuilder.type(named(TIMER_TASK_CLASS_NAME))
            .transform(new AgentBuilder.Transformer() {
                @Override
                public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
                    return builder.visit(Advice.to(TimerAdvice.class)
                        .on(named(SCHED_METHOD_NAME)
                            .and(takesArgument(0, TimerTask.class))));
                }
            });
        return agentBuilder;
    }

    private static class TimerAdvice {

        @Advice.OnMethodEnter
        public static void beforeMethod(@Advice.Argument(value = 0, readOnly = false, typing = Assigner.Typing.DYNAMIC) TimerTask target) {
            target = TtlTimerTask.get(target, false, true);
        }
    }
}
