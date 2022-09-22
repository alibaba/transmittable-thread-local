package com.alibaba.crr.composite;

import com.alibaba.crr.CrrTransmitCallback;
import edu.umd.cs.findbugs.annotations.NonNull;

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
public class CompositeCrrTransmitCallback implements CrrTransmitCallback {
    private static final Logger logger = Logger.getLogger(CompositeCrrTransmitCallback.class.getName());

    private final Set<CrrTransmitCallback> registeredCrrTransmitCallbackSet = new CopyOnWriteArraySet<>();

    @Override
    public void beforeReplay() {
        for (CrrTransmitCallback callback : registeredCrrTransmitCallbackSet) {
            try {
                callback.beforeReplay();
            } catch (Throwable t) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "exception when beforeReplay for crrTransmitCallback " + callback +
                            "(class " + callback.getClass().getName() + "), just ignored; cause: " + t, t);
                }
            }
        }
    }

    @Override
    public void afterReplay() {
        for (CrrTransmitCallback callback : registeredCrrTransmitCallbackSet) {
            try {
                callback.afterReplay();
            } catch (Throwable t) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "exception when afterReplay for crrTransmitCallback " + callback +
                            "(class " + callback.getClass().getName() + "), just ignored; cause: " + t, t);
                }
            }
        }
    }

    @Override
    public void beforeRestore() {
        for (CrrTransmitCallback callback : registeredCrrTransmitCallbackSet) {
            try {
                callback.beforeRestore();
            } catch (Throwable t) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "exception when beforeRestore for crrTransmitCallback " + callback +
                            "(class " + callback.getClass().getName() + "), just ignored; cause: " + t, t);
                }
            }
        }
    }

    @Override
    public void afterRestore() {
        for (CrrTransmitCallback callback : registeredCrrTransmitCallbackSet) {
            try {
                callback.afterRestore();
            } catch (Throwable t) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, "exception when afterRestore for crrTransmitCallback " + callback +
                            "(class " + callback.getClass().getName() + "), just ignored; cause: " + t, t);
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
