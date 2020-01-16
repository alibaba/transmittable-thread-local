package com.alibaba.demo.coroutine.ttl_intergration.usage

import com.alibaba.demo.coroutine.ttl_intergration.ttlContext
import com.alibaba.ttl.TransmittableThreadLocal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TtlCoroutineContextTest {
    @Test
    fun threadContextElement_passByValue(): Unit = runBlocking {
        val mainValue = "main-${System.currentTimeMillis()}"
        val testThread = Thread.currentThread()

        // String ThreadLocal, String is immutable value, can only be passed by value
        val threadLocal = TransmittableThreadLocal<String?>()
        threadLocal.set(mainValue)
        println("test thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")

        val job = launch(Dispatchers.Default + ttlContext()) {
            println("Launch start, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
            assertEquals(mainValue, threadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())

            delay(5)

            println("After delay, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
            assertEquals(mainValue, threadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())

            val reset = "job-reset-${threadLocal.get()}"
            threadLocal.set(reset)
            assertEquals(reset, threadLocal.get())

            delay(5)

            println("After delay set reset, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
            assertEquals(reset, threadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())
        }
        job.join()

        println("after launch, test thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
        assertEquals(mainValue, threadLocal.get())
    }

    @Test
    fun threadContextElement_passByReference(): Unit = runBlocking {
        data class Reference(var data: Int = 42)

        val mainValue = Reference()
        val testThread = Thread.currentThread()

        // Reference ThreadLocal, mutable value, pass by reference
        val threadLocal = TransmittableThreadLocal<Reference>() // declare thread-local variable
        threadLocal.set(mainValue)
        println("test thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")

        val job = launch(Dispatchers.Default + ttlContext()) {
            println("Launch start, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
            assertEquals(mainValue, threadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())

            delay(5)

            println("After delay, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
            assertEquals(mainValue, threadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())

            val reset = -42
            threadLocal.get().data = reset

            delay(5)

            println("After delay set reset, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
            assertEquals(Reference(reset), threadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())
        }
        job.join()

        println("after launch, test thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
        assertEquals(mainValue, threadLocal.get())
    }

    @Test
    fun twoThreadContextElement(): Unit = runBlocking {
        val mainValue = "main-a-${System.currentTimeMillis()}"
        val anotherMainValue = "main-another-${System.currentTimeMillis()}"
        val testThread = Thread.currentThread()

        val threadLocal = TransmittableThreadLocal<String?>() // declare thread-local variable
        val anotherThreadLocal = TransmittableThreadLocal<String?>() // declare thread-local variable

        threadLocal.set(mainValue)
        anotherThreadLocal.set(anotherMainValue)
        println("test thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()} | ${anotherThreadLocal.get()}")

        println()
        launch(Dispatchers.Default + ttlContext()) {
            println("Launch start, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()} | ${anotherThreadLocal.get()}")
            assertEquals(mainValue, threadLocal.get())
            assertEquals(anotherMainValue, anotherThreadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())

            delay(5)

            println("After delay, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()} | ${anotherThreadLocal.get()}")
            assertEquals(mainValue, threadLocal.get())
            assertEquals(anotherMainValue, anotherThreadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())

            val resetA = "job-reset-${threadLocal.get()}"
            threadLocal.set(resetA)
            val resetAnother = "job-reset-${anotherThreadLocal.get()}"
            anotherThreadLocal.set(resetAnother)
            println("Before delay set reset, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()} | ${anotherThreadLocal.get()}")

            delay(5)

            println("After delay set reset, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()} | ${anotherThreadLocal.get()}")
            assertEquals(resetA, threadLocal.get())
            assertEquals(resetAnother, anotherThreadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())
        }.join()

        println("after launch2, test thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()} | ${anotherThreadLocal.get()}")
        assertEquals(mainValue, threadLocal.get())
        assertEquals(anotherMainValue, anotherThreadLocal.get())
    }
}
