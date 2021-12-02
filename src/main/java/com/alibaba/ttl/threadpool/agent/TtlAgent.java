package com.alibaba.ttl.threadpool.agent;

import com.alibaba.ttl.threadpool.agent.logging.Logger;
import com.alibaba.ttl.threadpool.agent.transformlet.TtlTransformlet;
import com.alibaba.ttl.threadpool.agent.transformlet.internal.ForkJoinTtlTransformlet;
import com.alibaba.ttl.threadpool.agent.transformlet.internal.JdkExecutorTtlTransformlet;
import com.alibaba.ttl.threadpool.agent.transformlet.internal.TimerTaskTtlTransformlet;
import com.alibaba.ttl.threadpool.agent.transformlet.internal.PriorityBlockingQueueTtlTransformlet;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TTL Java Agent.
 *
 * <h2>The configuration for TTL agent</h2>
 * <p>
 * Configure TTL agent via {@code -D property}({@link System#getProperties()}) or TTL agent arguments.
 * <ol>
 *     <li>{@code -D property} config format is: {@code -Dkey1=v2 -Dkey2=v2}</li>
 *     <li>TTL agent arguments config format is {@code key1:v1,key2:v2}.<br>
 *         separate key-value pairs by {@code char ,}, and separate key-value by {@code char :}.<br></li>
 * </ol>
 * <B><I>NOTE about the config sources and the precedence:</I></B><br>
 * <ol>
 * <li>Read {@code -D property}({@link System#getProperties()}) first.</li>
 * <li>if no {@code -D property} configured(including empty property value configured by {@code -Dkey1}/{@code -Dkey1=}), read TTL Agent argument configuration.</li>
 * </ol>
 * Below is available TTL agent configuration keys.
 *
 * <h3>Configuration key: Log Type</h3>
 * <p>
 * The log type of TTL Java Agent is configured by key {@code ttl.agent.logger}. Since version {@code 2.6.0}.
 *
 * <ul>
 * <li>{@code ttl.agent.logger : STDERR}<br>
 * only log to {@code stderr} when error.
 * This is <b>default</b>, when no/unrecognized configuration for key {@code ttl.agent.logger}.</li>
 * <li>{@code ttl.agent.logger : STDOUT}<br>
 * Log to {@code stdout}, more info than {@code ttl.agent.logger:STDERR}; This is needed when developing.</li>
 * </ul>
 * <p>
 * Configuration example:
 *
 * <ol>
 * <li>{@code -Dttl.agent.logger=STDOUT}</li>
 * <li>{@code -javaagent:/path/to/transmittable-thread-local-2.x.y.jar=ttl.agent.logger:STDOUT}</li>
 * </ol>
 *
 * <h3>Configuration key: Disable inheritable for thread pool</h3>
 * <p>
 * Enable "disable inheritable" for thread pool, configured by key {@code ttl.agent.disable.inheritable.for.thread.pool}.
 * When no configuration for this key, default is {@code false}(aka. do <b>NOT</b> disable inheritable). Since version {@code 2.10.1}.
 *
 * <ul>
 * <li>rewrite the {@link java.util.concurrent.ThreadFactory} constructor parameter
 * of {@link java.util.concurrent.ThreadPoolExecutor}
 * to {@link com.alibaba.ttl.threadpool.DisableInheritableThreadFactory}
 * by util method {@link com.alibaba.ttl.threadpool.TtlExecutors#getDisableInheritableThreadFactory(java.util.concurrent.ThreadFactory) getDisableInheritableThreadFactory}.
 * </li>
 * <li>rewrite the {@link java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory} constructor parameter
 * of {@link java.util.concurrent.ForkJoinPool}
 * to {@link com.alibaba.ttl.threadpool.DisableInheritableForkJoinWorkerThreadFactory}
 * by util method {@link com.alibaba.ttl.threadpool.TtlForkJoinPoolHelper#getDisableInheritableForkJoinWorkerThreadFactory(java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory) getDisableInheritableForkJoinWorkerThreadFactory}.
 * </li>
 * </ul>
 * More info about "disable inheritable" see {@link com.alibaba.ttl.TransmittableThreadLocal}.
 * <p>
 * Configuration example:
 *
 * <ol>
 * <li>{@code -Dttl.agent.disable.inheritable.for.thread.pool=true}</li>
 * <li>{@code -javaagent:/path/to/transmittable-thread-local-2.x.y.jar=ttl.agent.disable.inheritable.for.thread.pool:true}</li>
 * </ol>
 *
 * <h3>Configuration key: Enable TimerTask class decoration</h3>
 * <p>
 * Enable TimerTask class decoration is configured by key {@code ttl.agent.enable.timer.task}.
 * Since version {@code 2.7.0}.
 * <p>
 * When no configuration for this key, default is {@code true}(aka. <b>enabled</b>).<br>
 * <b><i>Note</i></b>: Since version {@code 2.11.2} the default value is {@code true}(enable TimerTask class decoration);
 * Before version {@code 2.11.1} default value is {@code false}.
 * <p>
 * Configuration example:
 *
 * <ol>
 * <li>{@code -Dttl.agent.enable.timer.task=false}</li>
 * <li>{@code -javaagent:/path/to/transmittable-thread-local-2.x.y.jar=ttl.agent.enable.timer.task:false}</li>
 * </ol>
 *
 * <h3>Configuration key: logging the transform class received by TTL Agent</h3>
 * <p>
 * Enable logging the transform class received by TTL Agent by key {@code ttl.agent.log.class.transform},
 * default is {@code false}(aka. do <b>NOT</b> logging the transform class received by TTL Agent).
 * Since version {@code 2.13.0}.
 * <p>
 * Configuration example:
 *
 * <ol>
 * <li>{@code -Dttl.agent.log.class.transform=true}</li>
 * <li>{@code -javaagent:/path/to/transmittable-thread-local-2.x.y.jar=ttl.agent.log.class.transform:true}</li>
 * </ol>
 *
 * <h3>Multi key configuration example</h3>
 * <p>
 * For {@code -D property} config, simply specify multiply {@code -D property}, example:<br>
 * {@code -Dttl.agent.logger=STDOUT -Dttl.agent.disable.inheritable.for.thread.pool=true}
 * <p>
 * For TTL agent arguments config, example:<br>
 * {@code -javaagent:/path/to/transmittable-thread-local-2.x.y.jar=ttl.agent.logger:STDOUT,ttl.agent.disable.inheritable.for.thread.pool:true}
 *
 * <h2>About boot classpath for TTL agent</h2>
 * <p>
 * <b><i>NOTE:</i></b> Since {@code v2.6.0}, TTL agent jar will auto add self to {@code boot classpath}.<br>
 * But you <b>should <i>NOT</i></b> modify the downloaded TTL jar file name in the maven repo(eg: {@code transmittable-thread-local-2.x.y.jar}).<br>
 * if you modified the downloaded TTL agent jar file name(eg: {@code ttl-foo-name-changed.jar}),
 * you must add TTL agent jar to {@code boot classpath} manually
 * by java option {@code -Xbootclasspath/a:path/to/ttl-foo-name-changed.jar}.
 * <p>
 * The implementation of auto adding self agent jar to {@code boot classpath} use
 * the {@code Boot-Class-Path} property of manifest file({@code META-INF/MANIFEST.MF}) in the TTL Java Agent Jar:
 *
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
 * @see <a href="https://docs.oracle.com/javase/tutorial/deployment/jar/manifestindex.html">Working with Manifest Files - The Java™ Tutorials</a>
 * @see com.alibaba.ttl.TransmittableThreadLocal
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see java.util.concurrent.ScheduledThreadPoolExecutor
 * @see java.util.concurrent.ForkJoinPool
 * @see java.util.TimerTask
 * @since 0.9.0
 */
public final class TtlAgent {

    /**
     * the TTL agent configuration key: Log Type
     *
     * @see TtlAgent
     * @since 2.13.0
     */
    public static final String TTL_AGENT_LOGGER_KEY = "ttl.agent.logger";

    /**
     * the TTL agent configuration key: Disable inheritable for thread pool
     *
     * @see TtlAgent
     * @since 2.13.0
     */
    public static final String TTL_AGENT_DISABLE_INHERITABLE_FOR_THREAD_POOL_KEY = "ttl.agent.disable.inheritable.for.thread.pool";

    /**
     * the TTL agent configuration key: Enable TimerTask class decoration
     *
     * @see TtlAgent
     * @since 2.13.0
     */
    public static final String TTL_AGENT_ENABLE_TIMER_TASK_KEY = "ttl.agent.enable.timer.task";

    /**
     * the TTL agent configuration key: logging the transform class received by TTL Agent
     *
     * @see TtlAgent
     * @since 2.13.0
     */
    public static final String TTL_AGENT_LOG_CLASS_TRANSFORM_KEY = "ttl.agent.log.class.transform";


    // ======== TTL Agent internal States ========

    private static volatile Map<String, String> kvs;

    private static volatile boolean ttlAgentLoaded = false;

    /**
     * Entrance method of TTL Java Agent.
     *
     * @see TtlAgent
     */
    public static void premain(final String agentArgs, @NonNull final Instrumentation inst) {
        kvs = TtlAgentHelper.splitCommaColonStringToKV(agentArgs);

        Logger.setLoggerImplType(getLoggerType());
        final Logger logger = Logger.getLogger(TtlAgent.class);

        try {
            logger.info("[TtlAgent.premain] begin, agentArgs: " + agentArgs + ", Instrumentation: " + inst);

            logger.info(logTtlAgentConfig());

            final List<TtlTransformlet> transformletList = new ArrayList<TtlTransformlet>();

            transformletList.add(new JdkExecutorTtlTransformlet());
            transformletList.add(new PriorityBlockingQueueTtlTransformlet());

            transformletList.add(new ForkJoinTtlTransformlet());

            if (isEnableTimerTask()) transformletList.add(new TimerTaskTtlTransformlet());

            final ClassFileTransformer transformer = new TtlTransformer(transformletList, isLogClassTransform());
            inst.addTransformer(transformer, true);
            logger.info("[TtlAgent.premain] add Transformer " + transformer.getClass().getName() + " success");

            logger.info("[TtlAgent.premain] end");

            ttlAgentLoaded = true;
        } catch (Exception e) {
            String msg = "Fail to load TtlAgent , cause: " + e.toString();
            logger.error(msg, e);
            throw new IllegalStateException(msg, e);
        }
    }

    private static String logTtlAgentConfig() {
        return "TTL Agent configurations:"
            + "\n    " + TTL_AGENT_LOGGER_KEY + "=" + getLoggerType()
            + "\n    " + TTL_AGENT_LOG_CLASS_TRANSFORM_KEY + "=" + isLogClassTransform()
            + "\n    " + TTL_AGENT_DISABLE_INHERITABLE_FOR_THREAD_POOL_KEY + "=" + isDisableInheritableForThreadPool()
            + "\n    " + TTL_AGENT_ENABLE_TIMER_TASK_KEY + "=" + isEnableTimerTask();
    }

    /**
     * Whether TTL agent is loaded.
     *
     * @since 2.9.0
     */
    public static boolean isTtlAgentLoaded() {
        return ttlAgentLoaded;
    }

    /**
     * Whether disable inheritable for thread pool is enhanced by ttl agent, check {@link #isTtlAgentLoaded()} first.
     * <p>
     * Same as {@code isBooleanOptionSet(TTL_AGENT_DISABLE_INHERITABLE_FOR_THREAD_POOL_KEY)}.
     *
     * @see com.alibaba.ttl.threadpool.TtlExecutors#getDefaultDisableInheritableThreadFactory()
     * @see com.alibaba.ttl.threadpool.TtlExecutors#getDisableInheritableThreadFactory(java.util.concurrent.ThreadFactory)
     * @see com.alibaba.ttl.threadpool.DisableInheritableThreadFactory
     * @see com.alibaba.ttl.threadpool.TtlForkJoinPoolHelper#getDefaultDisableInheritableForkJoinWorkerThreadFactory()
     * @see com.alibaba.ttl.threadpool.TtlForkJoinPoolHelper#getDisableInheritableForkJoinWorkerThreadFactory(java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory)
     * @see com.alibaba.ttl.threadpool.DisableInheritableForkJoinWorkerThreadFactory
     * @see com.alibaba.ttl.TransmittableThreadLocal
     * @see TtlAgent
     * @see #isBooleanOptionSet(String)
     * @see #TTL_AGENT_DISABLE_INHERITABLE_FOR_THREAD_POOL_KEY
     * @since 2.10.1
     */
    public static boolean isDisableInheritableForThreadPool() {
        return isBooleanOptionSet(TTL_AGENT_DISABLE_INHERITABLE_FOR_THREAD_POOL_KEY);
    }

    /**
     * Whether timer task is enhanced by ttl agent, check {@link #isTtlAgentLoaded()} first.
     * <p>
     * Same as {@code isBooleanOptionSet(TTL_AGENT_ENABLE_TIMER_TASK_KEY, true)}.
     *
     * @see java.util.Timer
     * @see java.util.TimerTask
     * @see TtlAgent
     * @see #isBooleanOptionSet(String, boolean)
     * @see #TTL_AGENT_ENABLE_TIMER_TASK_KEY
     * @since 2.10.1
     */
    public static boolean isEnableTimerTask() {
        return isBooleanOptionSet(TTL_AGENT_ENABLE_TIMER_TASK_KEY, true);
    }

    /**
     * Whether logging the transform class received by {@link TtlAgent}.
     * <p>
     * Same as {@code isBooleanOptionSet(TTL_AGENT_LOG_CLASS_TRANSFORM_KEY)}.
     *
     * @see TtlAgent
     * @see #isBooleanOptionSet(String)
     * @see #TTL_AGENT_LOG_CLASS_TRANSFORM_KEY
     * @since 2.13.0
     */
    public static boolean isLogClassTransform() {
        return isBooleanOptionSet(TTL_AGENT_LOG_CLASS_TRANSFORM_KEY);
    }

    /**
     * Get the TTL Agent Log type.
     * <p>
     * Same as {@code getStringOptionValue(TTL_AGENT_LOGGER_KEY, Logger.STDERR)}.
     *
     * @see Logger
     * @see Logger#STDERR
     * @see Logger#STDOUT
     * @see TtlAgent
     * @see #getStringOptionValue(String, String)
     * @see #TTL_AGENT_LOGGER_KEY
     * @since 2.13.0
     */
    @NonNull
    public static String getLoggerType() {
        return getStringOptionValue(TTL_AGENT_LOGGER_KEY, Logger.STDERR);
    }

    // ======== Generic Option Getters ========

    /**
     * Generic Option Getters for {@code boolean type} option.
     * <p>
     * Same as {@code isBooleanOptionSet(key, false)}.
     *
     * @see #isBooleanOptionSet(String, boolean)
     * @see TtlAgent
     * @since 2.13.0
     */
    public static boolean isBooleanOptionSet(@NonNull String key) {
        return isBooleanOptionSet(key, false);
    }

    /**
     * Generic Option Getters for {@code boolean type} option.
     *
     * @see TtlAgent
     * @since 2.13.0
     */
    public static boolean isBooleanOptionSet(@NonNull String key, boolean defaultValueIfKeyAbsent) {
        return TtlAgentHelper.isBooleanOptionSet(kvs, key, defaultValueIfKeyAbsent);
    }

    /**
     * Generic Option Getters for {@code string type} option.
     * <p>
     * usage example:
     * <p>
     * if {@code -Dttl.agent.logger=STDOUT} or
     * TTL Agent configuration is {@code -javaagent:/path/to/transmittable-thread-local-2.x.y.jar=ttl.agent.logger:STDOUT},
     * {@code getOptionValue("ttl.agent.logger")} return {@code STDOUT}.
     *
     * @see TtlAgent
     * @since 2.13.0
     */
    @NonNull
    public static String getStringOptionValue(@NonNull String key, @NonNull String defaultValue) {
        return TtlAgentHelper.getStringOptionValue(kvs, key, defaultValue);
    }

    /**
     * Generic Option Getters for {@code string list type} option.
     * <p>
     * TTL configuration use {@code |} to separate items.
     * <p>
     * usage example:<br>
     * if {@code -Dfoo.list=v1|v2|v3} or
     * TTL Agent configuration is {@code -javaagent:/path/to/transmittable-thread-local-2.x.y.jar=foo.list:v1|v2|v3},
     * {@code getOptionValue("foo.list")} return {@code [v1, v2, v3]}.
     *
     * @see TtlAgent
     */
    @NonNull
    static List<String> getOptionStringListValues(@NonNull String key) {
        return TtlAgentHelper.getOptionStringListValues(kvs, key);
    }


    private TtlAgent() {
        throw new InstantiationError("Must not instantiate this class");
    }
}
