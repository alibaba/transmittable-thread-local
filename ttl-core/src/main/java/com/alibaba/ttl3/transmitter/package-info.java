/**
 * The base(under layer) API for {@code TTL} developer to integrate with {@code TTL};
 * You will <b><i>never</i></b> use this package in the <i>biz/application codes</i>.
 * <p>
 * Provide integration entrance({@link com.alibaba.ttl3.transmitter.Transmitter}) for executors
 * and extension point({@link com.alibaba.ttl3.transmitter.Transmittee} for other {@code ThreadLocal}s,
 * also provide a builtin support {@code JDK} {@link java.lang.ThreadLocal}({@link com.alibaba.ttl3.transmitter.ThreadLocalTransmitRegistry}).
 * <p>
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see com.alibaba.ttl3.transmitter.Transmitter
 * @see com.alibaba.ttl3.transmitter.ThreadLocalTransmitRegistry
 * @see com.alibaba.ttl3.transmitter.Transmittee
 */
package com.alibaba.ttl3.transmitter;
