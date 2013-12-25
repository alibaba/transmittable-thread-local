package com.alibaba.mtc;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

    static class Reference<T> extends WeakReference<MtContextThreadLocal<T>> {
        public Reference(MtContextThreadLocal<T> referent) {
            super(referent);
        }
    }

    static ThreadLocal<Set<Reference<?>>> holder =
            new ThreadLocal<Set<Reference<?>>>() {
                @Override
                protected Set<Reference<?>> initialValue() {
                    return new HashSet<Reference<?>>();
                }
            };

    void addMtContextThreadLocal() {
        for (Iterator<Reference<?>> iterator = holder.get().iterator(); iterator.hasNext(); ) {
            Reference<?> reference = iterator.next();
            MtContextThreadLocal<?> threadLocal = reference.get();
            if (null == threadLocal) {
                iterator.remove();
                continue;
            }
            if (threadLocal == this) {
                return;
            }
        }
        holder.get().add(new Reference<T>(this));
    }

    void removeMtContextThreadLocal() {
        for (Iterator<Reference<?>> iterator = holder.get().iterator(); iterator.hasNext(); ) {
            Reference<?> reference = iterator.next();
            MtContextThreadLocal<?> threadLocal = reference.get();
            if (null == threadLocal) {
                iterator.remove();
                continue;
            }
            if (threadLocal == this) {
                iterator.remove();
            }
        }
    }

    static Map<MtContextThreadLocal<?>, Object> copy() {
        Map<MtContextThreadLocal<?>, Object> copy = new HashMap<MtContextThreadLocal<?>, Object>();
        for (Iterator<Reference<?>> iterator = holder.get().iterator(); iterator.hasNext(); ) {
            Reference<?> reference = iterator.next();
            MtContextThreadLocal<?> threadLocal = reference.get();
            if (threadLocal == null) {
                iterator.remove();
            } else {
                copy.put(threadLocal, threadLocal.copyMtContextValue());
            }
        }
        return copy;
    }

    static Map<MtContextThreadLocal<?>, Object> backupAndSet(Map<MtContextThreadLocal<?>, Object> set) {
        // backup MtContext
        Map<MtContextThreadLocal<?>, Object> backup = new HashMap<MtContextThreadLocal<?>, Object>();
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
