package com.alibaba.ttl.spi;

/**
 * The TTL attachments for TTL tasks, eg: {@link com.alibaba.ttl.TtlRunnable}, {@link com.alibaba.ttl.TtlCallable}.
 */
public interface TtlAttachments extends TtlEnhanced {
    void setTtlAttachment(String key, Object value);

    <T> T getTtlAttachment(String key);

    String KEY_IS_AUTO_WRAPPER = "ttl.is.auto.wrapper";
}
