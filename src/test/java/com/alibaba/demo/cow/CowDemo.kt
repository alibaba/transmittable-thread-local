package com.alibaba.demo.cow

import com.alibaba.expandThreadPool
import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.ttl.threadpool.TtlExecutors
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun main() {
    val threadPool = Executors.newCachedThreadPool().let {
        expandThreadPool(it)
        TtlExecutors.getTtlExecutorService(it)
    }!!

    val traceContext = object : TransmittableThreadLocal<Trace>() {
        override fun initialValue(): Trace = Trace("init", Span("first", 0))
        override fun copy(parentValue: Trace): Trace = parentValue.copy() // shadow copy Trace, this is fast
        override fun childValue(parentValue: Trace): Trace = parentValue.copy() // shadow copy Trace, this is fast

        fun increaseSpan() {
            get().run {
                // COW the Span object in Trace
                span = span.copy(id = "${span.id} + PONG", counter = span.counter + 1)
            }
        }

        override fun toString(): String {
            return "${get()}[${super.toString()}]"
        }
    }

    fun printTtlInfo() {
        println("${Thread.currentThread().name}: $traceContext")
    }

    printTtlInfo()
    threadPool.execute {
        printTtlInfo()
        traceContext.increaseSpan()
        printTtlInfo()

        threadPool.execute {
            printTtlInfo()
            traceContext.increaseSpan()
            printTtlInfo()
        }
    }

    Thread.sleep(100)
    threadPool.shutdown()
    threadPool.awaitTermination(1, TimeUnit.SECONDS)
}

private data class Trace(var name: String, var span: Span)

private data class Span(val id: String, val counter: Int)
