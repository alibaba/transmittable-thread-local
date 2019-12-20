package com.alibaba.demo.distributed_tracer.refcount

import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.ttl.threadpool.TtlExecutors
import java.lang.Thread.sleep
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

/**
 * DistributedTracer(DT) use demo.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
fun main() {
    rpcInvokeIn()

    sleep(100)
}

private fun rpcInvokeIn() {
    ////////////////////////////////////////////////
    // DistributedTracer Framework Code
    ////////////////////////////////////////////////

    // Get Trace Id and Span Id from RPC Context
    val traceId = "traceId_XXXYYY"
    val baseSpanId = "1.1"
    transferInfo.set(DtTransferInfo(traceId, baseSpanId))
    traceId2LeafSpanIdInfo[traceId] = LeafSpanIdInfo()

    increaseSpanIdRefCount()

    ////////////////////////////////////////////////
    // Biz Code
    ////////////////////////////////////////////////
    syncMethod()

    ////////////////////////////////////////////////
    // DistributedTracer Framework Code
    ////////////////////////////////////////////////
    decreaseSpanIdRefCount()
}

private val executorService = Executors.newFixedThreadPool(1) { r: Runnable ->
    Thread(r, "Executors").apply { isDaemon = true }
}.let { TtlExecutors.getTtlExecutorService(it) }!!

private fun syncMethod() {
    // async call by TTL Executor, Test OK!
    executorService.submit { asyncMethod() }

    // async call by new Thread
    // FIXME Bug!! 没有 Increase/Decrease reference counter操作!
    thread(name = "Thread-by-new") { syncMethod_ByNewThread() }

    invokeServerWithRpc("server 1")
}

private fun asyncMethod() {
    invokeServerWithRpc("server 2")
}

private fun syncMethod_ByNewThread() {
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

//////////////////////////////////////////////////////////////////////////////////////////////////////////

internal class DtTransferInfo(val traceId: String, val baseSpanId: String)

internal class LeafSpanIdInfo(val current: AtomicInteger = AtomicInteger(1),
                              val refCounter: AtomicInteger = AtomicInteger(0))

private val transferInfo = object : TransmittableThreadLocal<DtTransferInfo>() {
    /*
@Override
protected DtTransferInfo childValue(DtTransferInfo parentValue) {
    // **注意**：
    // 新建线程时，从父线程继承值时，计数加1
    // 对应线程结束时，没有回调以清理ThreadLocal中的Context！，Bug！！
    // InheritableThreadLocal 没有提供 对应的拦截方法。。。 计数不配对了。。。
    // 但是一个线程就一个Context没清，线程数有限，Context占用内存一般很小，可以接受。
    increaseSpanIdRefCount();

    return super.childValue(parentValue);
}
*/

    override fun beforeExecute() {
        super.beforeExecute()
        increaseSpanIdRefCount()
    }

    override fun afterExecute() {
        decreaseSpanIdRefCount()
    }
}

private val traceId2LeafSpanIdInfo = ConcurrentHashMap<String, LeafSpanIdInfo>()

private fun increaseSpanIdRefCount() {
    val traceId = transferInfo.get().traceId
    val refCounter = traceId2LeafSpanIdInfo[traceId]!!.refCounter.incrementAndGet()

    System.out.printf("DEBUG: Increase reference counter(%s) for traceId %s in thread %s%n", refCounter, traceId, Thread.currentThread().name)
}

private fun decreaseSpanIdRefCount() {
    val traceId = transferInfo.get().traceId
    val leafSpanIdInfo = traceId2LeafSpanIdInfo[traceId]

    val refCounter = leafSpanIdInfo!!.refCounter.decrementAndGet()
    System.out.printf("DEBUG: Decrease reference counter(%s) for traceId %s in thread %s%n", refCounter, traceId, Thread.currentThread().name)

    if (refCounter == 0) {
        traceId2LeafSpanIdInfo.remove(traceId)

        System.out.printf("DEBUG: Clear traceId2LeafSpanIdInfo for traceId %s in thread %s%n", traceId, Thread.currentThread().name)
    } else if (refCounter < 0) {
        throw IllegalStateException("Leaf Span Id Info Reference counter has Bug!!")
    }
}

private fun increaseLeafSpanCurrentAndReturn(): Int {
    val traceId = transferInfo.get()!!.traceId
    return traceId2LeafSpanIdInfo[traceId]!!.current.getAndIncrement()
}
