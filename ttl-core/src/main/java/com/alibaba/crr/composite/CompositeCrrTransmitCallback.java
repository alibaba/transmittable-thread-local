package com.alibaba.crr.composite;

import com.alibaba.crr.CrrTransmitCallback;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Composite CrrTransmitCallback.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see CrrTransmitCallback
 */
public final class CompositeCrrTransmitCallback {
    private static final Logger logger = Logger.getLogger(CompositeCrrTransmitCallback.class.getName());

    private final Set<CrrTransmitCallback> registeredCrrTransmitCallbackSet = new CopyOnWriteArraySet<>();

    Object beforeReplay() {
        Set<CrrTransmitCallback> callbacks = new HashSet<>(registeredCrrTransmitCallbackSet);
        for (CrrTransmitCallback cb : callbacks) {
            try {
                cb.beforeReplay();
            } catch (Throwable t) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "exception when beforeReplay for crrTransmitCallback " + cb +
                            "(class " + cb.getClass().getName() + "), just ignored; cause: " + t, t);
                }
            }
        }
        return callbacks;
    }

    Object afterReplay(Object data) {
        @SuppressWarnings("unchecked")
        Set<CrrTransmitCallback> callbacks = (Set<CrrTransmitCallback>) data;
        for (CrrTransmitCallback cb : callbacks) {
            try {
                cb.afterReplay();
            } catch (Throwable t) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "exception when afterReplay for crrTransmitCallback " + cb +
                            "(class " + cb.getClass().getName() + "), just ignored; cause: " + t, t);
                }
            }
        }
        return data;
    }

    Object beforeRestore(Object data) {
        @SuppressWarnings("unchecked")
        Set<CrrTransmitCallback> callbacks = (Set<CrrTransmitCallback>) data;
        for (CrrTransmitCallback cb : callbacks) {
            try {
                cb.beforeRestore();
            } catch (Throwable t) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "exception when beforeRestore for crrTransmitCallback " + cb +
                            "(class " + cb.getClass().getName() + "), just ignored; cause: " + t, t);
                }
            }
        }
        return data;
    }

    void afterRestore(Object data) {
        @SuppressWarnings("unchecked")
        Set<CrrTransmitCallback> callbacks = (Set<CrrTransmitCallback>) data;
        for (CrrTransmitCallback cb : callbacks) {
            try {
                cb.afterRestore();
            } catch (Throwable t) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "exception when afterRestore for crrTransmitCallback " + cb +
                            "(class " + cb.getClass().getName() + "), just ignored; cause: " + t, t);
                }
            }
        }
    }


    /**
     * Register the {@link CrrTransmitCallback}.
     *
     * @return true if the input callback is not registered
     * @see #unregisterCallback(CrrTransmitCallback)
     */
    public boolean registerCallback(@NonNull CrrTransmitCallback callback) {
        return registeredCrrTransmitCallbackSet.add(callback);
    }

    /**
     * Unregister the {@link CrrTransmitCallback}.
     *
     * @return true if the input callback is registered
     * @see #registerCallback(CrrTransmitCallback)
     */
    public boolean unregisterCallback(@NonNull CrrTransmitCallback callback) {
        return registeredCrrTransmitCallbackSet.remove(callback);
    }
}
