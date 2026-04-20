package com.alibaba.crr.composite;

import com.alibaba.crr.TransmitCallback;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.alibaba.ttl3.internal.util.Utils.propagateIfFatal;

/**
 * Composite TransmitCallback.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see TransmitCallback
 */
public final class CompositeTransmitCallback {
    private static final Logger logger = Logger.getLogger(CompositeTransmitCallback.class.getName());

    private final Set<TransmitCallback> registeredTransmitCallbackSet = new CopyOnWriteArraySet<>();

    Object beforeReplay() {
        Set<TransmitCallback> callbacks = new HashSet<>(registeredTransmitCallbackSet);
        for (TransmitCallback cb : callbacks) {
            try {
                cb.beforeReplay();
            } catch (Throwable t) {
                propagateIfFatal(t);
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "exception when beforeReplay for transmittableCallback " + cb +
                            "(class " + cb.getClass().getName() + "), just ignored; cause: " + t, t);
                }
            }
        }
        return callbacks;
    }

    Object afterReplay(Object data) {
        @SuppressWarnings("unchecked")
        Set<TransmitCallback> callbacks = (Set<TransmitCallback>) data;
        for (TransmitCallback cb : callbacks) {
            try {
                cb.afterReplay();
            } catch (Throwable t) {
                propagateIfFatal(t);
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "exception when afterReplay for transmittableCallback " + cb +
                            "(class " + cb.getClass().getName() + "), just ignored; cause: " + t, t);
                }
            }
        }
        return data;
    }

    Object beforeRestore(Object data) {
        @SuppressWarnings("unchecked")
        Set<TransmitCallback> callbacks = (Set<TransmitCallback>) data;
        for (TransmitCallback cb : callbacks) {
            try {
                cb.beforeRestore();
            } catch (Throwable t) {
                propagateIfFatal(t);
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "exception when beforeRestore for transmittableCallback " + cb +
                            "(class " + cb.getClass().getName() + "), just ignored; cause: " + t, t);
                }
            }
        }
        return data;
    }

    void afterRestore(Object data) {
        @SuppressWarnings("unchecked")
        Set<TransmitCallback> callbacks = (Set<TransmitCallback>) data;
        for (TransmitCallback cb : callbacks) {
            try {
                cb.afterRestore();
            } catch (Throwable t) {
                propagateIfFatal(t);
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "exception when afterRestore for transmittableCallback " + cb +
                            "(class " + cb.getClass().getName() + "), just ignored; cause: " + t, t);
                }
            }
        }
    }


    /**
     * Register the {@link TransmitCallback}.
     *
     * @return true if the input callback is not registered
     * @see #unregisterCallback(TransmitCallback)
     */
    public boolean registerCallback(@NonNull TransmitCallback callback) {
        return registeredTransmitCallbackSet.add(callback);
    }

    /**
     * Unregister the {@link TransmitCallback}.
     *
     * @return true if the input callback is registered
     * @see #registerCallback(TransmitCallback)
     */
    public boolean unregisterCallback(@NonNull TransmitCallback callback) {
        return registeredTransmitCallbackSet.remove(callback);
    }
}
