package com.alibaba.ttl.testmodel

import com.alibaba.createChildTtlInstancesAndModifyParentTtlInstances
import com.alibaba.ttl.TransmittableThreadLocal
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class Call(private val tag: String, private val ttlInstances: ConcurrentMap<String, TransmittableThreadLocal<String>> = ConcurrentHashMap()) : Callable<String> {

    lateinit var copied: Map<String, String>

    val isCopied: Boolean
        get() = ::copied.isInitialized

    override fun call(): String {
        copied = createChildTtlInstancesAndModifyParentTtlInstances(tag, ttlInstances)
        return "ok"
    }
}
