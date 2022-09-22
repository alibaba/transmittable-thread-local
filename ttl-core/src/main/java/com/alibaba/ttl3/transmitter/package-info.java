/**
 * The base(under layer) API for {@code TTL} developer to integrate with {@code TTL};
 * You will <b><i>never</i></b> use this package in the <i>biz/application codes</i>.
 * <p>
 * Provide:
 *
 * <ul>
 *   <li>Integration entrance({@link com.alibaba.ttl3.transmitter.Transmitter})
 *       for executors.</li>
 *   <li>Extension point({@link com.alibaba.ttl3.transmitter.TransmitteeRegistry}
 *       for other {@code ThreadLocal}s.</li>
 * </ul>
 * <p>
 * {@code JDK} {@link java.lang.ThreadLocal} is builtin supported
 * by {@link com.alibaba.ttl3.transmitter.ThreadLocalTransmitRegistry}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see com.alibaba.ttl3.transmitter.Transmitter
 * @see com.alibaba.ttl3.transmitter.TransmitteeRegistry
 * @see com.alibaba.ttl3.transmitter.Transmittee
 * @see com.alibaba.ttl3.transmitter.ThreadLocalTransmitRegistry
 */
package com.alibaba.ttl3.transmitter;
