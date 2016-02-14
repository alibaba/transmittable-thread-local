package com.alibaba.ttl.testmodel;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class DeepCopyFooTransmittableThreadLocal extends TransmittableThreadLocal<FooPojo> {
    @Override
    protected FooPojo copy(FooPojo parentValue) {
        if (parentValue == null) return null;
        return new FooPojo(parentValue.getName(), parentValue.getAge());
    }
}
