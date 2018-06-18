package com.alibaba.ttl.testmodel

import com.alibaba.ttl.TransmittableThreadLocal

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class DeepCopyFooTransmittableThreadLocal : TransmittableThreadLocal<FooPojo>() {
    override fun copy(parentValue: FooPojo?): FooPojo? =
            if (parentValue == null) null else FooPojo(parentValue.name, parentValue.age)

}
