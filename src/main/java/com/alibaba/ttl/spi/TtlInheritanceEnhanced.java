package com.alibaba.ttl.spi;

/**
 * The TTL marker/tag interface for ttl enhanced classes by inheritance.
 * <p>
 * for example:
 * <ul>
 *     <li>{@link com.alibaba.ttl.TtlRecursiveAction}</li>
 *     <li>{@link com.alibaba.ttl.TtlRecursiveTask}</li>
 * </ul>
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see TtlEnhanced
 * @see com.alibaba.ttl.TtlRecursiveAction
 * @see com.alibaba.ttl.TtlRecursiveTask
 * @since 2.13.0
 */
public interface TtlInheritanceEnhanced extends TtlEnhanced {
}
