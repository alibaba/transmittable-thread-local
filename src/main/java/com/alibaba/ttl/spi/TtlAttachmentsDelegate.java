package com.alibaba.ttl.spi;

import com.alibaba.ttl.TtlUnwrap;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

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

    // ======== AutoWrapper Util Methods ========

    /**
     * @see TtlAttachments#KEY_IS_AUTO_WRAPPER
     * @since 2.13.0
     */
    public static boolean isAutoWrapper(@Nullable Object ttlAttachments) {
        if (!(ttlAttachments instanceof TtlAttachments)) return false;

        final Boolean value = ((TtlAttachments) ttlAttachments).getTtlAttachment(KEY_IS_AUTO_WRAPPER);
        if (value == null) return false;

        return value;
    }

    /**
     * @see TtlAttachments#KEY_IS_AUTO_WRAPPER
     * @since 2.13.0
     */
    public static void setAutoWrapperAttachment(@Nullable Object ttlAttachment) {
        if (!(ttlAttachment instanceof TtlAttachments)) return;

        ((TtlAttachments) ttlAttachment).setTtlAttachment(TtlAttachments.KEY_IS_AUTO_WRAPPER, true);
    }

    /**
     * @see TtlAttachments#KEY_IS_AUTO_WRAPPER
     * @since 2.13.0
     */
    @Nullable
    public static <T> T unwrapIfIsAutoWrapper(@Nullable T obj) {
        if (isAutoWrapper(obj)) return TtlUnwrap.unwrap(obj);
        else return obj;
    }
}
