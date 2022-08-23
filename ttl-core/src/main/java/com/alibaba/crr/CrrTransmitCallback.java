package com.alibaba.crr;

/**
 * The callback of {@link CrrTransmit} process.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see CrrTransmit
 */
public interface CrrTransmitCallback {
    /**
     * @see CrrTransmit#replay(Object)
     */
    default void beforeReplay() {
    }

    /**
     * @see CrrTransmit#replay(Object)
     */
    default void afterReplay() {
    }

    /**
     * @see CrrTransmit#restore(Object)
     */
    default void beforeRestore() {
    }

    /**
     * @see CrrTransmit#restore(Object)
     */
    default void afterRestore() {
    }
}
