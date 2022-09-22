package com.alibaba.ttl.spi;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * The TTL attachments for TTL tasks,
 * eg: {@link com.alibaba.ttl.TtlRunnable}, {@link com.alibaba.ttl.TtlCallable}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 2.11.0
 */
public interface TtlAttachments extends TtlEnhanced {
    /**
     * set the TTL attachments for TTL tasks
     *
     * @param key   attachment key
     * @param value attachment value
     * @since 2.11.0
     */
    void setTtlAttachment(@NonNull String key, Object value);

    /**
     * get the TTL attachment for TTL tasks
     *
     * @param key attachment key
     * @since 2.11.0
     */
    <T> T getTtlAttachment(@NonNull String key);

    /**
     * The attachment key of TTL task, weather this task is a auto wrapper task.
     * <p>
     * so the value of this attachment is a {@code boolean}.
     *
     * @since 2.11.0
     */
    String KEY_IS_AUTO_WRAPPER = "ttl.is.auto.wrapper";
}
