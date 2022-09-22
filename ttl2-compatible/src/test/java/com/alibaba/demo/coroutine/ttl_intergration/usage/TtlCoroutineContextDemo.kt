package com.alibaba.demo.coroutine.ttl_intergration.usage

import com.alibaba.demo.coroutine.ttl_intergration.ttlContext
import com.alibaba.ttl.TransmittableThreadLocal
import kotlinx.coroutines.*

private val threadLocal = TransmittableThreadLocal<String?>() // declare thread-local variable

/**
 * [Thread-local data - Coroutine Context and Dispatchers - Kotlin Programming Language](https://kotlinlang.org/docs/reference/coroutines/coroutine-context-and-dispatchers.html#thread-local-data)
 */
fun main(): Unit = runBlocking {
    val block: suspend CoroutineScope.() -> Unit = {
        println("Launch start, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
        threadLocal.set("!reset!")
        println("After reset, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
        delay(5)
        println("After yield, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
    }

    threadLocal.set("main")
    println("======================\nEmpty Coroutine Context\n======================")
    println("Pre-main, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
    launch(block = block).join()
    println("Post-main, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")

    threadLocal.set("main")
    println()
    println("======================\nTTL Coroutine Context\n======================")
    println("Pre-main, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
    launch(ttlContext(), block = block).join()
    println("Post-main, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")

    threadLocal.set("main")
    println()
    println("======================\nDispatchers.Default Coroutine Context\n======================")
    println("Pre-main, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
    launch(Dispatchers.Default, block = block).join()
    println("Post-main, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")

    threadLocal.set("main")
    println()
    println("======================\nDispatchers.Default + TTL Coroutine Context\n======================")
    println("Pre-main, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
    launch(Dispatchers.Default + ttlContext(), block = block).join()
    println("Post-main, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
}
