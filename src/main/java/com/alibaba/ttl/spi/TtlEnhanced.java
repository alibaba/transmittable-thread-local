package com.alibaba.ttl.spi;

/**
 * The TTL marker/tag interface, for ttl enhanced class.
 * <p>
 * There are 3 enhance type:
 * <ul>
 *     <li>enhanced by wrapper, marked by subtype {@link TtlWrapper}</li>
 *     <li>enhanced by inheritance, marked by subtype {@link TtlInheritanceEnhanced}</li>
 *     <li>in-place enhanced(for example, modify class byte code by Java Agent), marked by subtype {@link TtlInPlaceEnhanced}</li>
 * </ul>
 *
 * <B><I>Caution:</I></B><br>
 * Do <b><i>NOT</i></b> extends this tag interface for TTL enhanced classes,
 * <b>MUST</b> use above subtypes instead!
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see TtlWrapper
 * @see TtlInheritanceEnhanced
 * @see TtlInPlaceEnhanced
 * @since 2.11.0
 */
public interface TtlEnhanced {
}
