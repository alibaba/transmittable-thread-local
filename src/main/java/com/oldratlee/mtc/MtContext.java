package com.oldratlee.mtc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ding.lid
 */
public final class MtContext implements Serializable {
    private static final long serialVersionUID = -6658225204997224329L;

    private Map<String, Object> context = new HashMap<String, Object>();

    public Map<String, Object> get() {
        return context;
    }

    public Object get(String key) {
        return context.get(key);
    }

    public void set(Map<String, Object> context) {
        if (null == context) {
            throw new NullPointerException("context argument is null!");
        }
        this.context = new HashMap<String, Object>(context); // shallow copied map!!
    }

    public void set(String key, Object value) {
        context.put(key, value);
    }

    private static InheritableThreadLocal<MtContext> contextHolder = new InheritableThreadLocal<MtContext>() {
        @Override
        protected MtContext initialValue() {
            return new MtContext();
        }

        @Override
        protected MtContext childValue(MtContext parentValue) {
            MtContext ret = new MtContext();
            ret.context = new HashMap<String, Object>(parentValue.context); // shallow copied map!!
            return ret;
        }
    };

    public static MtContext getContext() {
        return contextHolder.get();
    }
}
