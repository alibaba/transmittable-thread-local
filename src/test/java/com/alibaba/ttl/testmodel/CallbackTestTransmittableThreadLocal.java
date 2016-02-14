package com.alibaba.ttl.testmodel;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class CallbackTestTransmittableThreadLocal extends TransmittableThreadLocal<FooPojo> {
    public AtomicInteger copyCounter = new AtomicInteger();
    public AtomicInteger beforeExecuteCounter = new AtomicInteger();
    public AtomicInteger afterExecuteCounter = new AtomicInteger();

    @Override
    protected FooPojo copy(FooPojo parentValue) {
        copyCounter.incrementAndGet();
        return super.copy(parentValue);
    }

    @Override
    protected void beforeExecute() {
        beforeExecuteCounter.incrementAndGet();
        super.beforeExecute();
    }

    @Override
    protected void afterExecute() {
        afterExecuteCounter.incrementAndGet();
        super.afterExecute();
    }
}
