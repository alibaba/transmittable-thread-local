@file:JvmName("NoMemoryLeak_ThreadLocal_NoRemove")

package com.alibaba.ttl.perf.memoryleak

import com.alibaba.ttl.perf.getRandomString

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
fun main(args: Array<String>) {
    var counter: Long = 0
    while (true) {
        val threadLocal = ThreadLocal<String>()
        threadLocal.set(getRandomString())

        if (counter % 1000 == 0L)
            System.out.printf("%05dK%n", counter / 1000)
        counter++
    }
}
