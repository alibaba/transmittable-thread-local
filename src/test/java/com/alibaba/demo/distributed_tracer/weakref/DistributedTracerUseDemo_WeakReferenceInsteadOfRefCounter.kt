package com.alibaba.demo.distributed_tracer.weakref

import com.alibaba.expandThreadPool
import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.ttl.threadpool.TtlExecutors
import java.lang.Thread.sleep
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread

private val executorService: ExecutorService = Executors.newFixedThreadPool(1) { r: Runnable ->
    Thread(r, "Executors").apply { isDaemon = true }
}.let {
    // ensure threads in pool is pre-created.
    expandThreadPool(it)
    TtlExecutors.getTtlExecutorService(it)
}!!

/**
 * DistributedTracer(DT) use demo.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
fun main() {
    for (i in 0..42) {
        rpcInvokeIn()
    }

    println("WARN: finished rpc invocation")

    // help to check GC status
    sleep(200)
    println("WARN: Call System.gc")
    System.gc()
    println("WARN: Called System.gc")
    sleep(100)

    println("Exit Main.")
}

///////////////////////////////////////////////////////////////////////

private fun rpcInvokeIn() {
    ////////////////////////////////////////////////
    // DistributedTracer Framework Code
    ////////////////////////////////////////////////

    // Get Trace Id and Span Id from RPC Context
    val traceId = "traceId_XXXYYY" + traceIdCounter.getAndIncrement()
    val baseSpanId = "1.1"

    val leafSpanIdInfo = LeafSpanIdInfo()
    transferInfo.set(DtTransferInfo(traceId, baseSpanId, leafSpanIdInfo))


    ////////////////////////////////////////////////
    // Biz Code
    ////////////////////////////////////////////////
    syncMethod()


    ////////////////////////////////////////////////
    // DistributedTracer Framework Code
    ////////////////////////////////////////////////
    System.out.printf("Finished Rpc call %s with span %s.%n", traceId, leafSpanIdInfo)

    // release context in ThreadLocal, avoid to be hold by thread, GC friendly.
    transferInfo.remove()
}

private fun syncMethod() {
    // async call by TTL Executor, Test OK!
    executorService.submit { asyncMethod() }

    // async call by new Thread
    thread(name = "Thread-by-new") { syncMethod_ByNewThread() }

    invokeServerWithRpc("server 1")
}

private fun asyncMethod() {
    Thread.sleep(3)
    invokeServerWithRpc("server 2")
}

private fun syncMethod_ByNewThread() {
    Thread.sleep(2)
    invokeServerWithRpc("server 3")
}


// RPC invoke
private fun invokeServerWithRpc(server: String) {
    ////////////////////////////////////////////////
    // DistributedTracer Framework Code
    ////////////////////////////////////////////////

    val leafSpanCurrent = increaseLeafSpanCurrentAndReturn()

    // Set RpcContext
    // Mocked, should use RPC util to get Rpc Context instead
    val rpcContext = ConcurrentHashMap<String, String>()

    rpcContext["traceId"] = transferInfo.get()!!.traceId
    rpcContext["spanId"] = transferInfo.get()!!.baseSpanId + "." + leafSpanCurrent

    // Do Rpc
    // ...
    System.out.printf("Do Rpc invocation to server %s with %s%n", server, rpcContext)
}

///////////////////////////////////////////////////////////////////////
// Span id management
///////////////////////////////////////////////////////////////////////

private val traceIdCounter = AtomicLong()

internal data class LeafSpanIdInfo(val current: AtomicInteger = AtomicInteger(1))

internal data class DtTransferInfo(val traceId: String, val baseSpanId: String, val leafSpanIdInfo: LeafSpanIdInfo) {
    // Output GC operation
    // How to implement finalize() in kotlin? - https://stackoverflow.com/questions/43784161
    @Suppress("unused", "ProtectedInFinal")
    protected fun finalize() {
        System.out.printf("DEBUG: gc DtTransferInfo traceId %s in thread %s: %s%n",
                traceId, Thread.currentThread().name, this)
    }
}

private val transferInfo = TransmittableThreadLocal<DtTransferInfo>()

private fun increaseLeafSpanCurrentAndReturn(): Int = transferInfo.get()!!.leafSpanIdInfo.current.getAndIncrement()

