package com.alibaba.crr;

/**
 * The callback of {@link Transmittable} process.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see Transmittable
 */
public interface TransmitCallback {
    /**
     * @see Transmittable#replay(Object)
     */
    default void beforeReplay() {
    }

    /**
     * @see Transmittable#replay(Object)
     */
    default void afterReplay() {
    }

    /**
     * @see Transmittable#restore(Object)
     */
    default void beforeRestore() {
    }

    /**
     * @see Transmittable#restore(Object)
     */
    default void afterRestore() {
    }
}
