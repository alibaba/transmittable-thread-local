package com.alibaba.demo.ttl

import com.alibaba.ttl.TransmittableThreadLocal
import kotlin.concurrent.thread

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
fun main() {
    val context = TransmittableThreadLocal<String>()

    context.set("value-set-in-parent")
    println("[parent thread] set ${context.get()}")

    /////////////////////////////////////
    // create sub-thread
    /////////////////////////////////////
    thread {
        val value = context.get()
        println("[child thread] get $value")
    }.join()

    println("[parent thread] get ${context.get()}")
}
