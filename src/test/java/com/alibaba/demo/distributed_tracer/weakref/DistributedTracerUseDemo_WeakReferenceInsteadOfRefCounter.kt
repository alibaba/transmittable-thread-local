package com.alibaba.demo.distributed_tracer.weakref

import com.alibaba.expandThreadPool
import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.ttl.threadpool.TtlExecutors
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong


private val executorService = TtlExecutors.getTtlExecutorService(
        Executors.newFixedThreadPool(1, { r: Runnable ->
            val thread = Thread(r, "Executors")
            thread.isDaemon = true
            thread
        })
)

/**
 * DistributedTracer(DT) use demo.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
fun main(args: Array<String>) {
    // ensure threads in pool is pre-created.
    expandThreadPool(executorService)

    for (i in 0..42) {
        rpcInvokeIn()
    }

    println("WARN: finished rpc invocation")

    // help to check GC status
    Thread.sleep(200)
    println("WARN: Call System.gc")
    System.gc()
    println("WARN: Called System.gc")
    Thread.sleep(100)

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
    Thread(::syncMethod_ByNewThread, "Thread-by-new").start()

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
    @Suppress("unused")
    fun finalize() {
        System.out.printf("DEBUG: gc DtTransferInfo traceId %s in thread %s: %s%n",
                traceId, Thread.currentThread().name, this)
    }
}

private val transferInfo = TransmittableThreadLocal<DtTransferInfo>()

private fun increaseLeafSpanCurrentAndReturn(): Int = transferInfo.get()!!.leafSpanIdInfo.current.getAndIncrement()

