package com.alibaba.ttl.threadpool.agent;

import com.alibaba.ttl.threadpool.agent.logging.Logger;
import com.alibaba.ttl.threadpool.agent.transformlet.TtlTransformlet;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.matcher.ElementMatchers.isSynthetic;


/**
 * TTL {@link ClassFileTransformer} of Java Agent
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see ClassFileTransformer
 * @see <a href="https://docs.oracle.com/javase/10/docs/api/java/lang/instrument/package-summary.html">The mechanism for instrumentation</a>
 * @since 0.9.0
 */
public class TtlTransformer {
    private static final Logger logger = Logger.getLogger(TtlTransformer.class);


    private final TtlExtensionTransformletManager extensionTransformletManager;
    private final List<TtlTransformlet> transformletList = new ArrayList<TtlTransformlet>();
    private final boolean logClassTransform;
    private final AgentBuilder agentBuilder;

    TtlTransformer(List<? extends TtlTransformlet> transformletList, boolean logClassTransform) {
        extensionTransformletManager = new TtlExtensionTransformletManager();
        this.logClassTransform = logClassTransform;
        for (TtlTransformlet ttlTransformlet : transformletList) {
            this.transformletList.add(ttlTransformlet);
            logger.info("[TtlTransformer] add Transformlet " + ttlTransformlet.getClass().getName());
        }

        this.agentBuilder = new AgentBuilder.Default()
            .ignore(agentIgnore())
            .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
            .with(new TransformLoggingListener());
    }


    private AgentBuilder.RawMatcher agentIgnore() {
        return new AgentBuilder.RawMatcher.ForElementMatchers(nameStartsWith("net.bytebuddy.")
            .and(not(nameStartsWith(NamingStrategy.SuffixingRandom.BYTE_BUDDY_RENAME_PACKAGE + ".")))
            .or(nameStartsWith("sun.reflect.")
                .or(nameStartsWith("jdk.reflect."))
                .or(nameStartsWith("org.slf4j."))
                .or(nameStartsWith("org.groovy."))
                .or(nameContains("javassist"))
                .or(nameContains(".asm."))
                .or(nameContains(".reflectasm."))
                .or(nameContains("$$FastClassByGuice$$"))
                .or(nameStartsWith("sun.reflect")))
            .<TypeDescription>or(isSynthetic()));
    }


    public void transform(Instrumentation instrumentation) {
        try {
            // Lambda has no class file, no need to transform, just return.
            if (logClassTransform) {

            }

            extensionTransformletManager.collectExtensionTransformlet();
            AgentBuilder agentBuilder = this.agentBuilder;
            for (TtlTransformlet transformlet : transformletList) {
                agentBuilder = transformlet.doTransform(agentBuilder);
            }
            extensionTransformletManager.extensionTransformletDoTransform(agentBuilder);
            agentBuilder.installOn(instrumentation);
        } catch (Throwable t) {
            String msg = "[TtlTransformer] fail to transform class " + ", cause: " + t.toString();
            logger.error(msg, t);
            throw new IllegalStateException(msg, t);
        }
    }

    /**
     * 类增强日志监听器
     */
    class TransformLoggingListener implements AgentBuilder.Listener {

        @Override
        public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded, Throwable throwable) {
            logger.error("[TtlTransformer] fail to transform class " + typeName
                + " from classloader " + classLoader, throwable);
        }

        @Override
        public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded,
                                     DynamicType dynamicType) {
            logger.info("[TtlTransformer] transformed " + typeDescription.getName()
                + " from classloader " + classLoader);
        }

        @Override
        public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded) {
        }

        @Override
        public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
            // 对每个加载的类都会匹配无论是否匹配成功，直接忽略
        }

        @Override
        public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
            if (logClassTransform) {
                logger.info("[TtlTransformer] transforming " + typeName
                    + " from classloader " + classLoader);
            }
        }
    }
}
