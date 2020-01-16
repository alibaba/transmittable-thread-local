package com.alibaba.ttl.spi;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link TtlAttachments} delegate/implementation.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see com.alibaba.ttl.TtlRunnable
 * @see com.alibaba.ttl.TtlCallable
 * @since 2.11.0
 */
public class TtlAttachmentsDelegate implements TtlAttachments {
    private final ConcurrentMap<String, Object> attachments = new ConcurrentHashMap<String, Object>();

    @Override
    public void setTtlAttachment(@NonNull String key, Object value) {
        attachments.put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getTtlAttachment(@NonNull String key) {
        return (T) attachments.get(key);
    }
}
