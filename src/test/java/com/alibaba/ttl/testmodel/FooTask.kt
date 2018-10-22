package com.alibaba.ttl.testmodel

import com.alibaba.CHILD_CREATE
import com.alibaba.PARENT_CREATE_MODIFIED_IN_CHILD
import com.alibaba.copyTtlValues
import com.alibaba.printTtlInstances
import com.alibaba.ttl.TransmittableThreadLocal
import java.util.concurrent.ConcurrentMap

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class FooTask(private val value: String, private val ttlInstances: ConcurrentMap<String, TransmittableThreadLocal<FooPojo>>) : Runnable {

    @Volatile
    lateinit var copied: Map<String, FooPojo>

    override fun run() {
        try {
            // Add new
            val child = DeepCopyFooTransmittableThreadLocal()
            child.set(FooPojo(CHILD_CREATE + value, 3))
            ttlInstances[CHILD_CREATE + value] = child

            // modify the parent key
            ttlInstances[PARENT_CREATE_MODIFIED_IN_CHILD]!!.get()!!.name = ttlInstances[PARENT_CREATE_MODIFIED_IN_CHILD]!!.get()!!.name + value

            copied = copyTtlValues(ttlInstances)

            println("Task $value finished!")
        } catch (e: Throwable) {
            e.printStackTrace()
        }

    }
}
