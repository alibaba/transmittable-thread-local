package com.alibaba.ttl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @deprecated Use {@link com.alibaba.ttl.spi.TtlWrapper}, {@link com.alibaba.ttl.spi.TtlInheritanceEnhanced}
 * or {@link com.alibaba.ttl.spi.TtlInPlaceEnhanced} instead.
 */
@Deprecated
@SuppressFBWarnings({"NM_SAME_SIMPLE_NAME_AS_INTERFACE"})
//   [ERROR] The class name com.alibaba.ttl.TtlEnhanced shadows
//   the simple name of implemented interface com.alibaba.ttl.spi.TtlEnhanced [com.alibaba.ttl.TtlEnhanced]
public interface TtlEnhanced extends com.alibaba.ttl.spi.TtlEnhanced {
}
