package com.alibaba.ttl.spi;

/**
 * The TTL marker/tag interface for in-place enhanced classes, usually modify the enhanced class byte code by Java Agent.
 * <p>
 * For example:
 * <ul>
 * <li>{@link java.util.concurrent.ForkJoinTask} class is in-place enhanced
 *     by {@link com.alibaba.ttl.threadpool.agent.transformlet.internal.ForkJoinTtlTransformlet}.</li>
 * <li>{@link java.util.concurrent.ThreadPoolExecutor} class is in-place enhanced
 *     by {@link com.alibaba.ttl.threadpool.agent.transformlet.internal.JdkExecutorTtlTransformlet}.</li>
 * <li>{@link java.util.TimerTask} class is in-place enhanced
 *     by {@link com.alibaba.ttl.threadpool.agent.transformlet.internal.TimerTaskTtlTransformlet}.</li>
 * </ul>
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see TtlEnhanced
 * @see com.alibaba.ttl.threadpool.agent.transformlet.internal.JdkExecutorTtlTransformlet
 * @see com.alibaba.ttl.threadpool.agent.transformlet.internal.ForkJoinTtlTransformlet
 * @see com.alibaba.ttl.threadpool.agent.transformlet.internal.TimerTaskTtlTransformlet
 * @since 2.13.0
 */
public interface TtlInPlaceEnhanced extends TtlEnhanced {
}
