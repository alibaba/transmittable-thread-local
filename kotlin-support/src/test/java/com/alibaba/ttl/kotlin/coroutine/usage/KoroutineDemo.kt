package com.alibaba.ttl.koroutine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking(Dispatchers.Default) {
    println("main".addThreadInfo())

    delay(3)

    val deferred = async(Dispatchers.IO) {
        println("async".addThreadInfo())
        "world"
    }

    println("Hello ${deferred.await()}!".addThreadInfo())
}

private fun String.addThreadInfo() = "[${Thread.currentThread().name} ${Thread.currentThread().id}] $this"
