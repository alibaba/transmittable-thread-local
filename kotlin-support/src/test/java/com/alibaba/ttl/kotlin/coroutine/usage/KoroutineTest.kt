package com.alibaba.ttl.kotlin.coroutine.usage

import com.alibaba.support.junit.conditional.ConditionalIgnoreRule
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule.ConditionalIgnore
import com.alibaba.support.junit.conditional.IsAgentRun
import com.alibaba.support.junit.conditional.NoAgentRun
import com.alibaba.ttl.TransmittableThreadLocal
import kotlinx.coroutines.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random

/**
 * [com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.TtlKoroutineTransformlet]
 */
class KoroutineTest {
    @Rule
    @JvmField
    val rule = ConditionalIgnoreRule()

    companion object {
        init {
            // https://github.com/Kotlin/kotlinx.coroutines/blob/master/docs/debugging.md
            System.setProperty("kotlinx.coroutines.debug", "on")

            KoroutineSchedulerExpander
        }
    }

    @Test
    @ConditionalIgnore(condition = IsAgentRun::class)
    fun testKoroutine_no_agent() = runBlocking(Dispatchers.Default) {
        (0 until 100).map { roundNum ->
            async {
                val mainValue = "main-${System.currentTimeMillis()}"
                val testThread = Thread.currentThread()

                // String ThreadLocal, String is immutable value, can only be passed by value
                val threadLocal = TransmittableThreadLocal<String?>()
                threadLocal.set(mainValue)
                println("[$roundNum] test thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")

                val task = async(Dispatchers.Default) {
                    println("[$roundNum] Launch start, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
                    when (Thread.currentThread()) {
                        testThread -> assertEquals("[$roundNum] - ${Thread.currentThread()}", mainValue, threadLocal.get())
                        else -> assertNull("[$roundNum] - ${Thread.currentThread()}", threadLocal.get())
                    }

                    delay(Random.nextLong(5))

                    println("[$roundNum] After delay, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
                    when (Thread.currentThread()) {
                        testThread -> assertEquals("[$roundNum] - ${Thread.currentThread()}", mainValue, threadLocal.get())
                        else -> assertNull("[$roundNum] - ${Thread.currentThread()}", threadLocal.get())
                    }

                    val reset = "${threadLocal.get()}-job-reset-${System.currentTimeMillis()}"
                    threadLocal.set(reset)
                    assertEquals("[$roundNum] - ${Thread.currentThread()}", reset, threadLocal.get())
                    val resetThread = Thread.currentThread()

                    delay(Random.nextLong(5))

                    println("[$roundNum] After delay set reset, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
                    when (Thread.currentThread()) {
                        resetThread -> assertEquals("[$roundNum] - ${Thread.currentThread()}", reset, threadLocal.get())
                        testThread -> assertEquals("[$roundNum] - ${Thread.currentThread()}", mainValue, threadLocal.get())
                        else -> assertNull("[$roundNum] - ${Thread.currentThread()}", threadLocal.get())
                    }

                    reset to resetThread
                }

                val (reset, resetThread) = task.await()

                println("[$roundNum] after launch, test thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
                when (Thread.currentThread()) {
                    resetThread -> assertEquals("[$roundNum] - ${Thread.currentThread()}", reset, threadLocal.get())
                    testThread -> assertEquals("[$roundNum] - ${Thread.currentThread()}", mainValue, threadLocal.get())
                    else -> assertNull("[$roundNum] - ${Thread.currentThread()}", threadLocal.get())
                }

                roundNum
            }
        }.map { it.await() }.let { println(it) }
    }

    @Test
    @ConditionalIgnore(condition = NoAgentRun::class)
    fun testKoroutine_with_agent(): Unit = runBlocking(Dispatchers.Default) {
        (0 until 100).map { roundNum ->
            async {
                val mainValue = "main-${System.currentTimeMillis()}"

                // String ThreadLocal, String is immutable value, can only be passed by value
                val threadLocal = TransmittableThreadLocal<String?>()
                threadLocal.set(mainValue)
                println("[$roundNum] test thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")

                launch(Dispatchers.Default) {
                    println("[$roundNum] Launch start, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
                    assertEquals("[$roundNum] - ${Thread.currentThread()} - launch start", mainValue, threadLocal.get())

                    delay(Random.nextLong(5))

                    println("[$roundNum] After delay, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
                    assertEquals("[$roundNum] - ${Thread.currentThread()} - After delay", mainValue, threadLocal.get())

                    val reset = "${threadLocal.get()}-job-reset-${System.currentTimeMillis()}"
                    threadLocal.set(reset)
                    assertEquals("[$roundNum] - ${Thread.currentThread()} - After reset", reset, threadLocal.get())

                    delay(Random.nextLong(5))

                    println("[$roundNum] After delay set reset, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
                    assertEquals("[$roundNum] - ${Thread.currentThread()} - After delay set reset", reset, threadLocal.get())
                }.join()

                println("[$roundNum] after launch, test thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
                assertEquals("[$roundNum] - ${Thread.currentThread()} - after launch", mainValue, threadLocal.get())

                roundNum
            }
        }.map { it.await() }.let { println(it) }
    }

    internal object KoroutineSchedulerExpander {
        init {
            // expand Koroutine scheduler
            runBlocking(Dispatchers.Default) {
                (0 until Runtime.getRuntime().availableProcessors() * 4).map {
                    async {
                        @Suppress("BlockingMethodInNonBlockingContext")
                        Thread.sleep(1)
                    }
                }.forEach { it.await() }
            }
        }
    }
}
