package com.alibaba.ttl.spi;

import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TtlAttachmentsDelegate implements TtlAttachments {
    private volatile ConcurrentMap<String, Object> attachment = new ConcurrentHashMap<String, Object>();

    @Override
    public void setTtlAttachment(String key, Object value) {
        attachment.put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getTtlAttachment(String key) {
        return (T) attachment.get(key);
    }

    public static void setAutoWrapper(Object ttlAttachment) {
        if (!(ttlAttachment instanceof TtlAttachments)) return;
        ((TtlAttachments) ttlAttachment).setTtlAttachment(TtlAttachments.KEY_IS_AUTO_WRAPPER, true);
    }

    public static boolean isAutoWrapper(@Nonnull TtlAttachments ttlAttachments) {
        return ttlAttachments.getTtlAttachment(TtlAttachments.KEY_IS_AUTO_WRAPPER);
    }
}
