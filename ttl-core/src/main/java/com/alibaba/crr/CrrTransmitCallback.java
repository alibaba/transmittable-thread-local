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
    void beforeReplay();

    /**
     * @see CrrTransmit#replay(Object)
     */
    void afterReplay();

    /**
     * @see CrrTransmit#restore(Object)
     */
    void beforeRestore();

    /**
     * @see CrrTransmit#restore(Object)
     */
    void afterRestore();
}
