package com.alibaba.ttl.internal;

/**
 * Hold {@link com.alibaba.ttl.TransmittableThreadLocal} value, can be implemented in varied ways depending on the runtime.
 *
 * @author Yang Fang (snoop dot fy at gmail dot com)
 * @since 2.7.0
 */
public interface TtlValue<T> {

    T get();

    void set(T t);

    void remove();

}
