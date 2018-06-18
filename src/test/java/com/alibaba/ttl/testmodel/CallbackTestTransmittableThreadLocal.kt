package com.alibaba.ttl.testmodel

import com.alibaba.ttl.TransmittableThreadLocal

import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class CallbackTestTransmittableThreadLocal : TransmittableThreadLocal<FooPojo>() {
    var copyCounter = AtomicInteger()
    var beforeExecuteCounter = AtomicInteger()
    var afterExecuteCounter = AtomicInteger()

    override fun copy(parentValue: FooPojo): FooPojo {
        copyCounter.incrementAndGet()
        return super.copy(parentValue)
    }

    override fun beforeExecute() {
        beforeExecuteCounter.incrementAndGet()
        super.beforeExecute()
    }

    override fun afterExecute() {
        afterExecuteCounter.incrementAndGet()
        super.afterExecute()
    }
}
