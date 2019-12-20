@file:JvmName("NoMemoryLeak_TransmittableThreadLocal_NoRemove")

package com.alibaba.perf.memoryleak

import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.perf.getRandomString

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
fun main() {
    var counter: Long = 0
    while (true) {
        val threadLocal = TransmittableThreadLocal<String>()
        threadLocal.set(getRandomString())

        if (counter % 1000 == 0L)
            System.out.printf("%05dK%n", counter / 1000)
        counter++
    }
}
