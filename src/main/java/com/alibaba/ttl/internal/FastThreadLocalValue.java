package com.alibaba.ttl.internal;

import io.netty.util.internal.FastThreadLocal;

/**
 * {@link FastThreadLocal} implementation for {@link com.alibaba.ttl.TransmittableThreadLocal} value holder
 *
 * @author Yang Fang (snoop dot fy at gmail dot com)
 * @since 2.7.0
 */
public class FastThreadLocalValue<T> implements TtlValue<T> {

    private FastThreadLocal<T> holder = new FastThreadLocal<T>();

    @Override
    public T get() {
        return holder.get();
    }

    @Override
    public void set(T t) {
        holder.set(t);
    }

    @Override
    public void remove() {
        holder.remove();
    }
}
