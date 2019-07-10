package com.alibaba.ttl.spi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TtlAttachmentsDelegate implements TtlAttachments {
    private volatile ConcurrentMap<String, Object> attachments = new ConcurrentHashMap<String, Object>();

    @Override
    public void setTtlAttachment(String key, Object value) {
        attachments.put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getTtlAttachment(String key) {
        return (T) attachments.get(key);
    }
}
