@file:JvmName("GetFastTransmittableThreadLocalInstanceTps")

package com.alibaba.perf.tps

import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.perf.getRandomString

/**
 * @author bigbear07
 */
fun main() {
    val batchGetCount = 1000
    val threadCount = 16
    val tpsCounter = TpsCounter(threadCount)

    val threadLocal = TransmittableThreadLocal { getRandomString() }

    tpsCounter.setAction {
        for (i in 0 until batchGetCount) {
            threadLocal.get()
        }
    }

    System.out.printf("FastGet init, batch: %d, thread: %d\n", batchGetCount, threadCount)
    while (true) {
        val start = tpsCounter.count
        Thread.sleep(1000)
        System.out.printf("FastGet tps: %,d\n", (tpsCounter.count - start) * batchGetCount)
    }
}
