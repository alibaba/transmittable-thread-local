package com.alibaba.demo.forkjoinpool

import java.util.concurrent.ConcurrentSkipListSet

fun main() {
    println("availableProcessors: ${Runtime.getRuntime().availableProcessors()}")

    val threadNames: MutableSet<String> = ConcurrentSkipListSet()

    (0..100).toList().stream().parallel().mapToInt {
        threadNames.add(Thread.currentThread().name)
        Thread.sleep(10)
        println("${Thread.currentThread().name}: $it")
        it
    }.sum().let {
        println(it)
    }

    println("${threadNames.size}:\n$threadNames")
}
