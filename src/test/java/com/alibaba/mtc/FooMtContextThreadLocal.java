package com.alibaba.mtc;

/**
 * @author ding.lid
 */
public class FooMtContextThreadLocal extends MtContextThreadLocal<FooPojo> {
    @Override
    protected FooPojo copy(FooPojo parentValue) {
        if (parentValue == null) return null;
        return new FooPojo(parentValue.name, parentValue.age);
    }
}
