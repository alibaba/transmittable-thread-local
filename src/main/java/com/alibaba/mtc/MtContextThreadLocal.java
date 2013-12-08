package com.alibaba.mtc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * {@link MtContextThreadLocal} can transmit context from the thread of submitting task to the thread of executing task.
 * <p/>
 * Note: this class extends {@link java.lang.InheritableThreadLocal},
 * so {@link com.alibaba.mtc.MtContextThreadLocal} first is a {@link java.lang.InheritableThreadLocal}.
 *
 * @author ding.lid
 * @see MtContextRunnable
 * @see MtContextCallable
 * @since 0.10.0
 */
public class MtContextThreadLocal<T> extends InheritableThreadLocal<T> {
    /**
     * Computes the context value for this multi-thread context variable
     * as a function of the source thread's value at the time the task
     * Object is created.  This method is called from {@link com.alibaba.mtc.MtContextRunnable} or
     * {@link com.alibaba.mtc.MtContextCallable} when it create, before the task is started.
     * <p/>
     * This method merely returns reference of its source thread value, and should be overridden
     * if a different behavior is desired.
     *
     * @since 1.0.0
     */
    protected T copyValue(T parentValue) {
        return parentValue;
    }

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

    @Override
    public final T get() {
        T value = super.get();
        if (null != value) {
            addMtContextThreadLocal();
        }
        return value;
    }

    @Override
    public final void set(T value) {
        super.set(value);
        if (null == value) { // may set null to remove value
            removeMtContextThreadLocal();
        } else {
            addMtContextThreadLocal();
        }
    }

    @Override
    public final void remove() {
        removeMtContextThreadLocal();
        super.remove();
    }

    T copyMtContextValue() {
        return copyValue(get());
    }

    static ThreadLocal<Set<MtContextThreadLocal<?>>> mtContextThreadLocalHolder = new ThreadLocal<Set<MtContextThreadLocal<?>>>() {
        @Override
        protected Set<MtContextThreadLocal<?>> initialValue() {
            return new HashSet<MtContextThreadLocal<?>>();
        }
    };

    void addMtContextThreadLocal() {
        mtContextThreadLocalHolder.get().add(this);
    }

    void removeMtContextThreadLocal() {
        mtContextThreadLocalHolder.get().remove(this);
    }

    static Map<MtContextThreadLocal<?>, Object> copy() {
        Map<MtContextThreadLocal<?>, Object> copy = new HashMap<MtContextThreadLocal<?>, Object>(mtContextThreadLocalHolder.get().size());
        for (MtContextThreadLocal<?> entry : mtContextThreadLocalHolder.get()) {
            MtContextThreadLocal<?> threadLocal = entry;
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
