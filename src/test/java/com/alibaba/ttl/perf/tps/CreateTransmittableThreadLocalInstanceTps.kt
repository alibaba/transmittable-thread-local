@file:JvmName("CreateTransmittableThreadLocalInstanceTps")

package com.alibaba.ttl.perf.tps

import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.ttl.perf.getRandomString

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
fun main(args: Array<String>) {
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
