package com.alibaba.mtc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    protected T copiedMtContextValue() {
        return get();
    }

    @Override
    public T get() {
        T value = super.get();
        addMtContextThreadLocal();
        return value;
    }

    @Override
    public void set(T value) {
        super.set(value);
        addMtContextThreadLocal();
    }

    @Override
    public void remove() {
        removeMtContextThreadLocal();
        super.remove();
    }

    private static final Object VALUE = new Object();
    final static Map<MtContextThreadLocal<?>, Object> holder = new ConcurrentHashMap<MtContextThreadLocal<?>, Object>(); // FIXME Use WeakReference so as to avoid memory leak 

    void addMtContextThreadLocal() {
        holder.put(this, VALUE);
    }

    void removeMtContextThreadLocal() {
        holder.remove(this);
    }
}
