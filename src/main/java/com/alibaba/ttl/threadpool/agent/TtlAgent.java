package com.alibaba.ttl.threadpool.agent;

import com.alibaba.ttl.threadpool.agent.internal.logging.Logger;
import com.alibaba.ttl.threadpool.agent.internal.transformlet.JavassistTransformlet;
import com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.TtlExecutorTransformlet;
import com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.TtlForkJoinTransformlet;
import com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.TtlFutureTransformlet;
import com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.TtlTimerTaskTransformlet;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * TTL Java Agent.
 *
 * <h2>The configuration/arguments for TTL agent</h2>
 * <p>
 * Configure TTL agent via agent arguments, format is {@code k1:v1,k2:v2}. Below is available configuration keys.
 *
 * <h3>Disable inheritable for thread pool</h3>
 * <p>
 * Enable "disable inheritable" for thread pool, config by key {@code ttl.agent.disable.inheritable.for.thread.pool}.
 * When no configuration for this key, default does <b>not</b> enabled. Since version {@code 2.10.1}.
 *
 * <ul>
 * <li>rewrite the {@link java.util.concurrent.ThreadFactory} constructor parameter
 * of {@link java.util.concurrent.ThreadPoolExecutor}
 * to {@link com.alibaba.ttl.threadpool.DisableInheritableThreadFactory}
 * by util method {@link com.alibaba.ttl.threadpool.TtlExecutors#getDisableInheritableThreadFactory(java.util.concurrent.ThreadFactory)}.
 * </li>
 * <li>rewrite the {@link java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory} constructor parameter
 * of {@link java.util.concurrent.ForkJoinPool}
 * to {@link com.alibaba.ttl.threadpool.DisableInheritableForkJoinWorkerThreadFactory}
 * by util method {@link com.alibaba.ttl.threadpool.TtlForkJoinPoolHelper#getDisableInheritableForkJoinWorkerThreadFactory(java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory)}.
 * </li>
 * </ul>
 * More info about "disable inheritable" see {@link com.alibaba.ttl.TransmittableThreadLocal}.
 * <p>
 * Configuration example:<br>
 * {@code -javaagent:/path/to/transmittable-thread-local-2.x.x.jar=ttl.agent.disable.inheritable.for.thread.pool:true}
 *
 * <h3>The log configuration</h3>
 * The log of TTL Java Agent is config by key {@code ttl.agent.logger}. Since version {@code 2.6.0}.
 *
 * <ul>
 * <li>{@code ttl.agent.logger : STDERR}<br>
 * only log to {@code stderr} when error.
 * This is <b>default</b>, when no/unrecognized configuration for key {@code ttl.agent.logger}.</li>
 * <li>{@code ttl.agent.logger : STDOUT}<br>
 * Log to {@code stdout}, more info than {@code ttl.agent.logger:STDERR}; This is needed when developing.</li>
 * </ul>
 * <p>
 * configuration example:
 * <ul>
 * <li>{@code -javaagent:/path/to/transmittable-thread-local-2.x.x.jar}</li>
 * <li>{@code -javaagent:/path/to/transmittable-thread-local-2.x.x.jar=ttl.agent.logger:STDOUT}</li>
 * </ul>
 *
 * <h3>Enable/disable TimerTask class decoration</h3>
 * Enable/disable TimerTask class decoration is config by key {@code ttl.agent.enable.timer.task}.
 * Since version {@code 2.7.0}.
 * <p>
 * When no configuration for this key, default is <b>enabled</b>.<br>
 * <b><i>Note</i></b>: Since version {@code 2.11.2} the default value is {@code true}(enable TimerTask class decoration);
 * Before version {@code 2.11.1} default value is {@code false}.
 * <p>
 * Configuration example:
 * <ul>
 * <li>{@code -javaagent:/path/to/transmittable-thread-local-2.x.x.jar=ttl.agent.enable.timer.task:false}</li>
 * <li>{@code -javaagent:/path/to/transmittable-thread-local-2.x.x.jar=ttl.agent.enable.timer.task:true}</li>
 * </ul>
 *
 * <h3>Multi key configuration example</h3>
 * {@code -javaagent:/path/to/transmittable-thread-local-2.x.x.jar=ttl.agent.logger:STDOUT,ttl.agent.disable.inheritable.for.thread.pool:true}
 *
 * <h2>About boot classpath for TTL agent</h2>
 * <b><i>NOTE:</i></b> Since {@code v2.6.0}, TTL agent jar will auto add self to {@code boot classpath}.
 * But you <b>should <i>NOT</i></b> modify the downloaded TTL jar file name in the maven repo(eg: {@code transmittable-thread-local-2.x.x.jar}).<br>
 * if you modified the downloaded TTL agent jar file name(eg: {@code ttl-foo-name-changed.jar}),
 * you must add TTL agent jar to {@code boot classpath} manually
 * by java option {@code -Xbootclasspath/a:path/to/ttl-foo-name-changed.jar}.
 * <p>
 * The implementation of auto adding self agent jar to {@code boot classpath} use
 * the {@code Boot-Class-Path} property of manifest file({@code META-INF/MANIFEST.MF}) in the TTL Java Agent Jar:
 * <blockquote>
 * <dl>
 * <dt>Boot-Class-Path</dt>
 * <dd>
 * A list of paths to be searched by the bootstrap class loader. Paths represent directories or libraries (commonly referred to as JAR or zip libraries on many platforms).
 * These paths are searched by the bootstrap class loader after the platform specific mechanisms of locating a class have failed. Paths are searched in the order listed.
 * </dd>
 * </dl>
 * </blockquote>
 * <p>
 * More info about {@code Boot-Class-Path} see
 * <a href="https://docs.oracle.com/javase/10/docs/api/java/lang/instrument/package-summary.html">The mechanism for instrumentation</a>.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see Instrumentation
 * @see <a href="https://docs.oracle.com/javase/10/docs/api/java/lang/instrument/package-summary.html">The mechanism for instrumentation</a>
 * @see <a href="https://docs.oracle.com/javase/10/docs/specs/jar/jar.html#jar-manifest">JAR File Specification - JAR Manifest</a>
 * @see <a href="https://docs.oracle.com/javase/tutorial/deployment/jar/manifestindex.html">Working with Manifest Files - The Java™ TutorialsHide</a>
 * @see com.alibaba.ttl.TransmittableThreadLocal
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see java.util.concurrent.ScheduledThreadPoolExecutor
 * @see java.util.concurrent.ForkJoinPool
 * @see java.util.TimerTask
 * @since 0.9.0
 */
public final class TtlAgent {
    /**
     * Entrance method of TTL Java Agent.
     *
     * @see Logger
     * @see Logger#TTL_AGENT_LOGGER_KEY
     * @see Logger#STDERR
     * @see Logger#STDOUT
     */
    public static void premain(final String agentArgs, @NonNull final Instrumentation inst) {
        kvs = splitCommaColonStringToKV(agentArgs);

        Logger.setLoggerImplType(getLogImplTypeFromAgentArgs(kvs));
        final Logger logger = Logger.getLogger(TtlAgent.class);

        try {
            logger.info("[TtlAgent.premain] begin, agentArgs: " + agentArgs + ", Instrumentation: " + inst);
            final boolean disableInheritableForThreadPool = isDisableInheritableForThreadPool();

            final List<JavassistTransformlet> transformletList = new ArrayList<JavassistTransformlet>();
            transformletList.add(new TtlExecutorTransformlet(disableInheritableForThreadPool));
            transformletList.add(new TtlForkJoinTransformlet(disableInheritableForThreadPool));
            transformletList.add(new TtlFutureTransformlet());
            if (isEnableTimerTask()) transformletList.add(new TtlTimerTaskTransformlet());

            final ClassFileTransformer transformer = new TtlTransformer(transformletList);
            inst.addTransformer(transformer, true);
            logger.info("[TtlAgent.premain] addTransformer " + transformer.getClass() + " success");

            logger.info("[TtlAgent.premain] end");

            ttlAgentLoaded = true;
        } catch (Exception e) {
            String msg = "Fail to load TtlAgent , cause: " + e.toString();
            logger.log(Level.SEVERE, msg, e);
            throw new IllegalStateException(msg, e);
        }
    }

    private static String getLogImplTypeFromAgentArgs(@NonNull final Map<String, String> kvs) {
        return kvs.get(Logger.TTL_AGENT_LOGGER_KEY);
    }

    private static volatile Map<String, String> kvs;

    private static volatile boolean ttlAgentLoaded = false;

    /**
     * Whether TTL agent is loaded.
     *
     * @since 2.9.0
     */
    public static boolean isTtlAgentLoaded() {
        return ttlAgentLoaded;
    }

    private static final String TTL_AGENT_ENABLE_TIMER_TASK_KEY = "ttl.agent.enable.timer.task";

    private static final String TTL_AGENT_DISABLE_INHERITABLE_FOR_THREAD_POOL = "ttl.agent.disable.inheritable.for.thread.pool";

    /**
     * Whether disable inheritable for thread pool is enhanced by ttl agent, check {@link #isTtlAgentLoaded()} first.
     *
     * @see com.alibaba.ttl.threadpool.TtlExecutors#getDefaultDisableInheritableThreadFactory()
     * @see com.alibaba.ttl.threadpool.TtlExecutors#getDisableInheritableThreadFactory(java.util.concurrent.ThreadFactory)
     * @see com.alibaba.ttl.threadpool.DisableInheritableThreadFactory
     * @see com.alibaba.ttl.threadpool.TtlForkJoinPoolHelper#getDefaultDisableInheritableForkJoinWorkerThreadFactory()
     * @see com.alibaba.ttl.threadpool.TtlForkJoinPoolHelper#getDisableInheritableForkJoinWorkerThreadFactory(java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory)
     * @see com.alibaba.ttl.threadpool.DisableInheritableForkJoinWorkerThreadFactory
     * @since 2.10.1
     */
    public static boolean isDisableInheritableForThreadPool() {
        return isBooleanOptionSet(kvs, TTL_AGENT_DISABLE_INHERITABLE_FOR_THREAD_POOL, false);
    }

    /**
     * Whether timer task is enhanced by ttl agent, check {@link #isTtlAgentLoaded()} first.
     *
     * @since 2.10.1
     */
    public static boolean isEnableTimerTask() {
        return isBooleanOptionSet(kvs, TTL_AGENT_ENABLE_TIMER_TASK_KEY, true);
    }

    private static boolean isBooleanOptionSet(@Nullable final Map<String, String> kvs, @NonNull String key, boolean defaultValue) {
        if (null == kvs) return defaultValue;

        final boolean containsKey = kvs.containsKey(key);
        if (!containsKey) return defaultValue;

        return !"false".equalsIgnoreCase(kvs.get(key));
    }

    /**
     * Split to {@code json} like String({@code "k1:v1,k2:v2"}) to KV map({@code "k1"->"v1", "k2"->"v2"}).
     */
    @NonNull
    static Map<String, String> splitCommaColonStringToKV(@Nullable final String commaColonString) {
        final Map<String, String> ret = new HashMap<String, String>();
        if (commaColonString == null || commaColonString.trim().length() == 0) return ret;

        final String[] splitKvArray = commaColonString.trim().split("\\s*,\\s*");
        for (String kvString : splitKvArray) {
            final String[] kv = kvString.trim().split("\\s*:\\s*");
            if (kv.length == 0) continue;

            if (kv.length == 1) ret.put(kv[0], "");
            else ret.put(kv[0], kv[1]);
        }

        return ret;
    }

    private TtlAgent() {
        throw new InstantiationError("Must not instantiate this class");
    }
}
