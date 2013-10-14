package com.alibaba.mtc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link MtContext} maintains the multi-thread context.
 * <p/>
 * Use method {@link #get()} to get {@link MtContext} instance.
 *
 * @author ding.lid
 * @since 0.9.0
 */
public final class MtContext implements Serializable {
    private static final long serialVersionUID = -6658225204997224329L;

    private Map<String, Object> content = new HashMap<String, Object>();

    MtContext() {
    }

    MtContext(MtContext context) {
        this.content.putAll(context.content); // shallow copied map!!
    }

    /**
     * Get the whole context content.
     */
    public Map<String, Object> get() {
        return content;
    }

    /**
     * Get the value of key in context.
     */
    @SuppressWarnings("unchecked")
    public <V> V get(String key) {
        return (V) content.get(key);
    }

    /**
     * reset context content.
     */
    public void set(Map<String, Object> context) {
        if (null == context) {
            throw new NullPointerException("context argument is null!");
        }
        this.content.clear();
        this.content.putAll(context); // shallow copied map!!
    }

    /**
     * set the value of key.
     */
    public void set(String key, Object value) {
        content.put(key, value);
    }

    private static InheritableThreadLocal<MtContext> contextHolder = new InheritableThreadLocal<MtContext>() {
        @Override
        protected MtContext initialValue() {
            return new MtContext();
        }

        @Override
        protected MtContext childValue(MtContext parentValue) {
            return new MtContext(parentValue);
        }
    };

    /**
     * Get the context of current thread.
     */
    public static MtContext getContext() {
        return contextHolder.get();
    }
}
