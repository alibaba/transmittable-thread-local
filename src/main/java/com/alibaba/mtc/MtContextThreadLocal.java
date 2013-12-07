package com.alibaba.mtc;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ding.lid
 * @since 0.10.0
 */
public class MtContextThreadLocal<T> extends InheritableThreadLocal<T> {
    /**
     * Override this method to have an initial value other than <tt>null</tt>.
     *
     * @see {@link ThreadLocal#initialValue()}
     */
    @Override
    protected T initialValue() {
        return super.initialValue();
    }

    /**
     * @see {@link java.lang.InheritableThreadLocal#childValue(Object)}
     */
    @Override
    protected T childValue(T parentValue) {
        return super.childValue(parentValue);
    }

    /**
     * Computes the context value for this multi-thread thread-local variable
     * as a function of the source thread's value at the time the task
     * Object is created.  This method is called from {@link com.alibaba.mtc.MtContextRunnable} or
     * {@link com.alibaba.mtc.MtContextCallable} when it create, before the task is started.
     * <p/>
     * This method merely returns reference of its source thread value, and should be overridden
     * if a different behavior is desired.
     */
    protected T copyMtContextValue() {
        return get();
    }

    @Override
    public final T get() {
        T value = super.get();
        addMtContextThreadLocal();
        return value;
    }

    @Override
    public final void set(T value) {
        super.set(value);
        addMtContextThreadLocal();
    }

    @Override
    public final void remove() {
        removeMtContextThreadLocal();
        super.remove();
    }

    static ThreadLocal<Map<MtContextThreadLocal<?>, Object>> mtContextThreadLocalHolder = new ThreadLocal<Map<MtContextThreadLocal<?>, Object>>() {
        @Override
        protected Map<MtContextThreadLocal<?>, Object> initialValue() {
            return new HashMap<MtContextThreadLocal<?>, Object>();
        }
    };
    private static final Object VALUE = new Object();

    void addMtContextThreadLocal() {
        mtContextThreadLocalHolder.get().put(this, VALUE);
    }

    void removeMtContextThreadLocal() {
        mtContextThreadLocalHolder.get().remove(this);
    }

    static Map<MtContextThreadLocal<?>, Object> copy() {
        Map<MtContextThreadLocal<?>, Object> copy = new HashMap<MtContextThreadLocal<?>, Object>(mtContextThreadLocalHolder.get().size());
        for (Map.Entry<MtContextThreadLocal<?>, Object> entry : mtContextThreadLocalHolder.get().entrySet()) {
            MtContextThreadLocal<?> threadLocal = entry.getKey();
            copy.put(threadLocal, threadLocal.copyMtContextValue());
        }
        return copy;
    }

    static Map<MtContextThreadLocal<?>, Object> backupAndSet(Map<MtContextThreadLocal<?>, Object> set) {
        // backup MtContext
        Map<MtContextThreadLocal<?>, Object> backup = new HashMap<MtContextThreadLocal<?>, Object>(mtContextThreadLocalHolder.get().size());
        for (Map.Entry<MtContextThreadLocal<?>, Object> entry : set.entrySet()) {
            @SuppressWarnings("unchecked")
            MtContextThreadLocal<Object> threadLocal = (MtContextThreadLocal<Object>) entry.getKey();
            backup.put(threadLocal, threadLocal.get());

            threadLocal.set(entry.getValue());
        }
        return backup;
    }

    static void restore(Map<MtContextThreadLocal<?>, Object> backup) {
        // restore MtContext
        for (Map.Entry<MtContextThreadLocal<?>, Object> entry : backup.entrySet()) {
            @SuppressWarnings("unchecked")
            MtContextThreadLocal<Object> threadLocal = (MtContextThreadLocal<Object>) entry.getKey();
            threadLocal.set(entry.getValue());
        }
    }
}
