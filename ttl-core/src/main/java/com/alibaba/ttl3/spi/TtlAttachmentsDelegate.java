package com.alibaba.ttl3.spi;

import com.alibaba.ttl3.TtlWrappers;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link TtlAttachments} delegate/implementation.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see com.alibaba.ttl3.TtlRunnable
 * @see com.alibaba.ttl3.TtlCallable
 */
public class TtlAttachmentsDelegate implements TtlAttachments {
    private final ConcurrentMap<String, Object> attachments = new ConcurrentHashMap<>();

    @Override
    public void setTtlAttachment(@NonNull String key, Object value) {
        attachments.put(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getTtlAttachment(@NonNull String key) {
        return (T) attachments.get(key);
    }

    // ======== AutoWrapper Util Methods ========

    /**
     * @see TtlAttachments#KEY_IS_AUTO_WRAPPER
     */
    public static boolean isAutoWrapper(@Nullable Object ttlAttachments) {
        if (!(ttlAttachments instanceof TtlAttachments)) return false;

        final Boolean value = ((TtlAttachments) ttlAttachments).getTtlAttachment(KEY_IS_AUTO_WRAPPER);
        if (value == null) return false;

        return value;
    }

    /**
     * @see TtlAttachments#KEY_IS_AUTO_WRAPPER
     */
    public static void setAutoWrapperAttachment(@Nullable Object ttlAttachment) {
        if (!(ttlAttachment instanceof TtlAttachments)) return;

        ((TtlAttachments) ttlAttachment).setTtlAttachment(TtlAttachments.KEY_IS_AUTO_WRAPPER, true);
    }

    /**
     * @see TtlAttachments#KEY_IS_AUTO_WRAPPER
     */
    @Nullable
    public static <T> T unwrapIfIsAutoWrapper(@Nullable T obj) {
        if (isAutoWrapper(obj)) return TtlWrappers.unwrap(obj);
        else return obj;
    }
}
