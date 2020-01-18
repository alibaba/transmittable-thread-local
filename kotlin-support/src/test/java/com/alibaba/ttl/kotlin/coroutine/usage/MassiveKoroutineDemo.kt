package com.alibaba.ttl.koroutine

import kotlinx.coroutines.*
import kotlin.random.Random

class MassiveKoroutineDemo {
    companion object {
        @JvmStatic
        fun main(args: Array<String>): Unit = runBlocking<Unit>(Dispatchers.Default) {
            (0 until 1000).map { roundNum ->
                launch {
                    coroutineContext
                    println("main $roundNum".addThreadInfo())

                    delay(Random.nextLong(1, 5))

                    val deferred = async(Dispatchers.IO) {
                        println("async $roundNum".addThreadInfo())
                        "world"
                    }

                    println("Hello %s %s".format(deferred.await(), roundNum).addThreadInfo())
                }
            }.forEach {
                it.join()
            }
        }

        private fun String.addThreadInfo() = String.format(
                "%-45s %-3s : %s", Thread.currentThread().name, Thread.currentThread().id.toString(), this)

        init {
            // https://github.com/Kotlin/kotlinx.coroutines/blob/master/docs/debugging.md
            System.setProperty("kotlinx.coroutines.debug", "on")
        }
    }
}
