package com.alibaba.mtc.testmodel;

import com.alibaba.mtc.MtContextThreadLocal;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class DeepCopyFooMtContextThreadLocal extends MtContextThreadLocal<FooPojo> {
    @Override
    protected FooPojo copy(FooPojo parentValue) {
        if (parentValue == null) return null;
        return new FooPojo(parentValue.getName(), parentValue.getAge());
    }
}
