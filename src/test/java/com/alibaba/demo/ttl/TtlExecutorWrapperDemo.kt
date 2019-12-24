package com.alibaba.demo.ttl

import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.ttl.threadpool.TtlExecutors
import java.util.concurrent.Callable
import java.util.concurrent.Executors

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
fun main() {
    val ttlExecutorService = Executors.newCachedThreadPool().let {
        // return TTL wrapper from normal ExecutorService
        TtlExecutors.getTtlExecutorService(it)
    }!!
    val context = TransmittableThreadLocal<String>()

    context.set("value-set-in-parent")
    println("[parent thread] set ${context.get()}")

    /////////////////////////////////////
    // Runnable
    /////////////////////////////////////
    val task = Runnable { println("[child thread] get ${context.get()} in Runnable") }
    ttlExecutorService.submit(task).get()

    /////////////////////////////////////
    // Callable
    /////////////////////////////////////
    val call = Callable {
        println("[child thread] get ${context.get()} in Callable")
        42
    }
    ttlExecutorService.submit(call).get()

    /////////////////////////////////////
    // cleanup
    /////////////////////////////////////
    ttlExecutorService.shutdown()
}
