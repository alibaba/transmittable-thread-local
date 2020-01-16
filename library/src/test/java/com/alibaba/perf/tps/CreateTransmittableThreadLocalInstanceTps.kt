@file:JvmName("CreateTransmittableThreadLocalInstanceTps")

package com.alibaba.perf.tps

import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.perf.getRandomString

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
fun main() {
    val tpsCounter = TpsCounter(2)

    tpsCounter.setAction(Runnable {
        val threadLocal = TransmittableThreadLocal<String>()
        threadLocal.set(getRandomString())
    })

    while (true) {
        val start = tpsCounter.count
        Thread.sleep(1000)
        System.out.printf("tps: %d\n", tpsCounter.count - start)
    }
}
