package com.alibaba.demo.cow

import com.alibaba.expandThreadPool
import com.alibaba.ttl3.TransmittableThreadLocal
import com.alibaba.ttl3.executor.TtlExecutors
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun main() {
    val threadPool: ExecutorService = Executors.newCachedThreadPool().let {
        expandThreadPool(it)
        TtlExecutors.getTtlExecutorService(it)
    }!!

    traceContext.print()
    threadPool.execute {
        traceContext.print()
        traceContext.increaseSpan()
        traceContext.print()

        threadPool.execute {
            traceContext.print()
            traceContext.increaseSpan()
            traceContext.print()
        }
    }

    Thread.sleep(100)
    threadPool.shutdown()
    threadPool.awaitTermination(1, TimeUnit.SECONDS)
}

private val traceContext = object : TransmittableThreadLocal<Trace>() {
    override fun initialValue(): Trace = Trace("init", Span("first", 0))
    override fun transmitteeValue(parentValue: Trace): Trace = parentValue.copy() // shadow copy Trace, this is fast
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


private fun TransmittableThreadLocal<Trace>.print() {
    println("${Thread.currentThread().name}: $this")
}

private data class Trace(var name: String, var span: Span)

private data class Span(val id: String, val counter: Int)
