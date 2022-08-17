package com.alibaba.ttl3.spi;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * The TTL attachments for TTL tasks,
 * eg: {@link com.alibaba.ttl3.TtlRunnable}, {@link com.alibaba.ttl3.TtlCallable}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public interface TtlAttachments extends TtlEnhanced {
    /**
     * set the TTL attachments for TTL tasks
     *
     * @param key   attachment key
     * @param value attachment value
     */
    void setTtlAttachment(@NonNull String key, Object value);

    /**
     * get the TTL attachment for TTL tasks
     *
     * @param key attachment key
     */
    <T> T getTtlAttachment(@NonNull String key);

    /**
     * The attachment key of TTL task, weather this task is a auto wrapper task.
     * <p>
     * so the value of this attachment is a {@code boolean}.
     *
     */
    String KEY_IS_AUTO_WRAPPER = "ttl.is.auto.wrapper";
}
