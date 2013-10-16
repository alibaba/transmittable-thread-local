package com.alibaba.mtc;

/**
 * If the Object in {@link MtContext} implements this interface,
 * {@link MtContext} will use {@link #copy()} to create the copy instance when context transmit to sub-thread.
 *
 * @author ding.lid
 * @since 0.9.1
 */
public interface Copyable<T extends Copyable<T>> {
    T copy();
}
