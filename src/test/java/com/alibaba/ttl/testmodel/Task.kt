package com.alibaba.ttl.testmodel

import com.alibaba.createChildTtlInstancesAndModifyParentTtlInstances
import com.alibaba.ttl.TransmittableThreadLocal
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class Task(
    private val tag: String,
    private val ttlInstances: ConcurrentMap<String, TransmittableThreadLocal<String>> = ConcurrentHashMap()
) : Runnable {

    private val queue = ArrayBlockingQueue<Map<String, String>>(1);

    val copied: Map<String, String>
        get() = queue.poll(100, TimeUnit.MILLISECONDS)!!

    override fun run() {
        val map = createChildTtlInstancesAndModifyParentTtlInstances(tag, ttlInstances)
        queue.put(map)
    }
}
