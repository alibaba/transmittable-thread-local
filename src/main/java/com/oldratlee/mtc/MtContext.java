package com.oldratlee.mtc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link MtContext} maintains the multi-thread context.
 *
 * @author ding.lid
 */
public final class MtContext implements Serializable {
    private static final long serialVersionUID = -6658225204997224329L;

    private Map<String, Object> content = new HashMap<String, Object>();

    public Map<String, Object> get() {
        return content;
    }

    public Object get(String key) {
        return content.get(key);
    }

    public void set(Map<String, Object> context) {
        if (null == context) {
            throw new NullPointerException("context argument is null!");
        }
        this.content = new HashMap<String, Object>(context); // shallow copied map!!
    }

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
            MtContext ret = new MtContext();
            ret.content = new HashMap<String, Object>(parentValue.content); // shallow copied map!!
            return ret;
        }
    };

    public static MtContext getContext() {
        return contextHolder.get();
    }
}
