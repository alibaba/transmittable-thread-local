package com.alibaba.demo.ttl

import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.ttl.TtlCallable
import com.alibaba.ttl.TtlRunnable
import java.util.concurrent.Callable
import java.util.concurrent.Executors

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
fun main() {
    val executorService = Executors.newCachedThreadPool()
    val context = TransmittableThreadLocal<String>()

    context.set("value-set-in-parent")
    println("[parent thread] set ${context.get()}")

    /////////////////////////////////////
    // Runnable / TtlRunnable
    /////////////////////////////////////
    val task = Runnable { println("[child thread] get ${context.get()} in Runnable") }
    val ttlRunnable = TtlRunnable.get(task)!!

    executorService.submit(ttlRunnable).get()

    /////////////////////////////////////
    // Callable / TtlCallable
    /////////////////////////////////////
    val call = Callable {
        println("[child thread] get ${context.get()} in Callable")
        42
    }
    val ttlCallable = TtlCallable.get(call)!!

    executorService.submit(ttlCallable).get()

    /////////////////////////////////////
    // cleanup
    /////////////////////////////////////
    executorService.shutdown()
}
