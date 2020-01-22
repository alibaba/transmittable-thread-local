package com.alibaba.ttl.spi;

import com.alibaba.ttl.TtlUnwrap;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Ttl Wrapper interface.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see TtlUnwrap#unwrap
 * @since 2.11.4
 */
public interface TtlWrapper<T> extends TtlEnhanced {
    /**
     * unwrap {@link TtlWrapper} to the original/underneath one.
     * <p>
     * this method is {@code null}-safe, when input {@code BiFunction} parameter is {@code null}, return {@code null};
     * if input parameter is not a {@code TtlWrapper} just return input.
     * <p>
     * so {@code unwrap} will always return the same input object.
     *
     * @see TtlUnwrap#unwrap(Object)
     */
    @NonNull
    T unwrap();
}
