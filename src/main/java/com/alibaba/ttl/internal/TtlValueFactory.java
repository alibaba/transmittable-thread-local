package com.alibaba.ttl.internal;

/**
 * {@link com.alibaba.ttl.TransmittableThreadLocal} value holder factory.
 *
 * <p>
 * If there is netty in the runtime and
 * {@link io.netty.util.internal.FastThreadLocal} is supported. You can just set
 *
 * <pre>-Dttl.fastthreadlocal.enable=true</pre>
 *
 * to enable FastThreadLocal mode for better performance.
 * </p>
 *
 * <p>
 * Caution: If FastThreadLocal mode is enabled, {@link com.alibaba.ttl.TransmittableThreadLocal}
 * will NEVER be inheritable!
 * </p>
 *
 * @author Yang Fang (snoop dot fy at gmail dot com)
 * @since 2.7.0
 */
public class TtlValueFactory {

    private static final String FAST_THREAD_LOCAL_ENABLE = "ttl.fastthreadlocal.enable";

    private static final boolean IS_FAST_THREAD_LOCAL_SUPPORT = isClassPresent("io.netty.util.internal.FastThreadLocal");

    public static <T> TtlValue<T> create() {
        if (isFastThreadLocalEnabled() && IS_FAST_THREAD_LOCAL_SUPPORT) {
            return new FastThreadLocalValue<T>();
        } else {
            return null;
        }
    }

    public static boolean isFastThreadLocalEnabled() {
        return Boolean.parseBoolean(System.getProperty(FAST_THREAD_LOCAL_ENABLE, "false"));
    }

    private static boolean isClassPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
