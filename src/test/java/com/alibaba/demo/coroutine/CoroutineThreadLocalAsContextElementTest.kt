package com.alibaba.demo.coroutine

import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class CoroutineThreadLocalAsContextElementTest {
    @Test
    fun oneThreadContextElement(): Unit = runBlocking {
        val mainValue = "main-${System.currentTimeMillis()}"
        val launchValue = "launch-${System.currentTimeMillis()}"
        val testThread = Thread.currentThread()

        val threadLocal = ThreadLocal<String?>() // declare thread-local variable
        threadLocal.set(mainValue)
        println("test thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")

        val job = launch(Dispatchers.Default + threadLocal.asContextElement(value = launchValue)) {
            println("Launch start, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
            assertEquals(launchValue, threadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())

            delay(5)

            println("After delay, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
            assertEquals(launchValue, threadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())

            val reset = "job-reset-${threadLocal.get()}"
            threadLocal.set(reset)
            assertEquals(reset, threadLocal.get())

            delay(5)

            println("After delay set reset, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
            // !!! After suspended delay function, reset ThreadLocal value is lost !!!
            // assertEquals(reset, threadLocal.get())
            assertEquals(launchValue, threadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())
        }
        job.join()

        println("after launch, test thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()}")
        assertEquals(mainValue, threadLocal.get())
    }

    @Test
    fun twoThreadContextElement(): Unit = runBlocking {
        val mainValue = "main-a-${System.currentTimeMillis()}"
        val testThread = Thread.currentThread()
        val anotherMainValue = "main-another-${System.currentTimeMillis()}"

        val threadLocal = ThreadLocal<String?>() // declare thread-local variable
        val anotherThreadLocal = ThreadLocal<String?>() // declare thread-local variable

        threadLocal.set(mainValue)
        anotherThreadLocal.set(anotherMainValue)
        println("test thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()} | ${anotherThreadLocal.get()}")

        println()
        val launch1Value = "aLaunch1"
        launch(Dispatchers.Default + threadLocal.asContextElement(value = launch1Value)) {
            println("Launch start, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()} | ${anotherThreadLocal.get()}")
            assertEquals(launch1Value, threadLocal.get())
            assertNull(anotherThreadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())

            delay(5)

            println("After delay, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()} | ${anotherThreadLocal.get()}")
            assertEquals(launch1Value, threadLocal.get())
            assertNull(anotherThreadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())

            val resetA = "job-reset-${threadLocal.get()}"
            threadLocal.set(resetA)
            val resetAnother = "job-reset-${anotherThreadLocal.get()}"
            anotherThreadLocal.set(resetAnother)
            println("Before delay set reset, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()} | ${anotherThreadLocal.get()}")

            delay(5)

            println("After delay set reset, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()} | ${anotherThreadLocal.get()}")
            // !!! After suspended delay function, reset ThreadLocal value is lost !!!
            // assertEquals(resetA, threadLocal.get())
            assertEquals(launch1Value, threadLocal.get())
            // !!! After suspended delay, ThreadLocal without ThreadContextElement is not clear !!!
            assertEquals(resetAnother, anotherThreadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())
        }.join()

        println("after launch1, test thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()} | ${anotherThreadLocal.get()}")
        assertEquals(mainValue, threadLocal.get())
        assertEquals(anotherMainValue, anotherThreadLocal.get())

        println()
        val launch2Value = "aLaunch2"
        val anotherLaunch2Value = "anotherLaunch2"
        launch(Dispatchers.Default + threadLocal.asContextElement(value = launch2Value) + anotherThreadLocal.asContextElement(value = anotherLaunch2Value)) {
            println("Launch start, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()} | ${anotherThreadLocal.get()}")
            assertEquals(launch2Value, threadLocal.get())
            assertEquals(anotherLaunch2Value, anotherThreadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())

            delay(5)

            println("After delay, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()} | ${anotherThreadLocal.get()}")
            assertEquals(launch2Value, threadLocal.get())
            assertEquals(anotherLaunch2Value, anotherThreadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())

            val resetA = "job-reset-${threadLocal.get()}"
            threadLocal.set(resetA)
            val resetAnother = "job-reset-${anotherThreadLocal.get()}"
            anotherThreadLocal.set(resetAnother)
            println("Before delay set reset, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()} | ${anotherThreadLocal.get()}")

            delay(5)

            println("After delay set reset, current thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()} | ${anotherThreadLocal.get()}")
            // !!! After suspended delay function, reset ThreadLocal value is lost !!!
            // assertEquals(resetA, threadLocal.get())
            // assertEquals(resetAnother, anotherThreadLocal.get())
            assertEquals(launch2Value, threadLocal.get())
            assertEquals(anotherLaunch2Value, anotherThreadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())
        }.join()

        println("after launch2, test thread: ${Thread.currentThread()}, thread local value: ${threadLocal.get()} | ${anotherThreadLocal.get()}")
        assertEquals(mainValue, threadLocal.get())
        assertEquals(anotherMainValue, anotherThreadLocal.get())
    }
}
