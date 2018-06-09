@file:JvmName("CreateThreadLocalInstanceTps")

package com.alibaba.ttl.perf.tps

import com.alibaba.ttl.perf.Utils

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
fun main(args: Array<String>) {
    val tpsCounter = TpsCounter(2)

    tpsCounter.setAction(Runnable {
        val threadLocal = ThreadLocal<String>()
        threadLocal.set(Utils.getRandomString())
    })

    while (true) {
        val start = tpsCounter.count
        Thread.sleep(1000)
        System.out.printf("tps: %,d\n", tpsCounter.count - start)
    }
}
