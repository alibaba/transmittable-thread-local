package com.alibaba.ttl.testmodel

import com.alibaba.createChildTtlInstancesAndModifyParentTtlInstances
import com.alibaba.ttl.TransmittableThreadLocal
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class Task(private val tag: String, private val ttlInstances: ConcurrentMap<String, TransmittableThreadLocal<String>> = ConcurrentHashMap()) : Runnable {

    lateinit var copied: Map<String, String>

    override fun run() {
        copied = createChildTtlInstancesAndModifyParentTtlInstances(tag, ttlInstances)
    }
}
