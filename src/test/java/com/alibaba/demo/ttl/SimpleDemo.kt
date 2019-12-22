package com.alibaba.demo.ttl

import com.alibaba.ttl.TransmittableThreadLocal
import kotlin.concurrent.thread

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
fun main() {
    val ttlContext = TransmittableThreadLocal<String>()

    ttlContext.set("value-set-in-parent")
    println("[parent thread] set ${ttlContext.get()}")

    /////////////////////////////////////
    // create sub-thread
    /////////////////////////////////////
    thread {
        val value = ttlContext.get()
        println("[child thread] get $value")
    }.join()

    println("[parent thread] get ${ttlContext.get()}")
}
