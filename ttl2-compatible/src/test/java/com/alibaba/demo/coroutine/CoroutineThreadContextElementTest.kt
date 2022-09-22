package com.alibaba.demo.coroutine

import io.kotest.core.spec.style.AnnotationSpec
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.junit.Assert.*

class CoroutineThreadContextElementTest : AnnotationSpec() {
    private val logger = KotlinLogging.logger {}

    @Test
    fun threadContextElement_passByValue(): Unit = runBlocking {
        val mainValue = "main-${System.currentTimeMillis()}"
        val launchValue = "launch-${System.currentTimeMillis()}"
        val testThread = Thread.currentThread()

        // String ThreadLocal, String is immutable value, can only be passed by value
        val threadLocal = ThreadLocal<String?>()
        threadLocal.set(mainValue)
        logger.info { "test thread - thread local value: ${threadLocal.get()}" }

        val job = launch(Dispatchers.Default + threadLocal.asContextElement(value = launchValue)) {
            logger.info { "launch thread - launch start, thread local value: ${threadLocal.get()}" }
            assertEquals(launchValue, threadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())

            delay(5)

            logger.info { "launch thread - after delay, thread local value: ${threadLocal.get()}" }
            assertEquals(launchValue, threadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())

            val reset = "job-reset-${threadLocal.get()}"
            threadLocal.set(reset)
            assertEquals(reset, threadLocal.get())

            delay(5)

            logger.info { "launch thread - after delay set reset, thread local value: ${threadLocal.get()}" }
            // !!! After suspended delay function, reset ThreadLocal value is lost !!!
            // assertEquals(reset, threadLocal.get())
            assertEquals(launchValue, threadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())
        }
        job.join()

        logger.info { "test thread - after launch, thread local value: ${threadLocal.get()}" }

        assertEquals(mainValue, threadLocal.get())
    }

    @Test
    fun threadContextElement_passByReference(): Unit = runBlocking {
        data class Reference(var data: Int = 42)

        val mainValue = Reference()
        val launchValue = Reference(4242)
        val testThread = Thread.currentThread()

        // Reference ThreadLocal, mutable value, pass by reference
        val threadLocal = ThreadLocal<Reference>() // declare thread-local variable
        threadLocal.set(mainValue)
        logger.info { "test thread - thread local value: ${threadLocal.get()}" }

        val job = launch(Dispatchers.Default + threadLocal.asContextElement(value = launchValue)) {
            logger.info { "launch thread - launch start, thread local value: ${threadLocal.get()}" }
            assertEquals(launchValue, threadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())

            delay(5)

            logger.info { "launch thread - after delay, thread local value: ${threadLocal.get()}" }
            assertEquals(launchValue, threadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())

            val reset = -42
            threadLocal.get().data = reset

            delay(5)

            logger.info { "launch thread - after delay set reset, thread local value: ${threadLocal.get()}" }
            assertEquals(Reference(reset), threadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())
        }
        job.join()

        logger.info { "test thread - after launch in test thread, thread local value: ${threadLocal.get()}" }
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
        logger.info { "test thread - thread local value: ${threadLocal.get()} | ${anotherThreadLocal.get()}" }

        val launch1Value = "aLaunch1"
        launch(Dispatchers.Default + threadLocal.asContextElement(value = launch1Value)) {
            assertEquals(launch1Value, threadLocal.get())
            assertNull(anotherThreadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())

            delay(5)

            logger.info { "launch thread - after delay, thread local value: ${threadLocal.get()} | ${anotherThreadLocal.get()}" }
            assertEquals(launch1Value, threadLocal.get())
            assertNull(anotherThreadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())

            val resetA = "job-reset-${threadLocal.get()}"
            threadLocal.set(resetA)
            val resetAnother = "job-reset-${anotherThreadLocal.get()}"
            anotherThreadLocal.set(resetAnother)
            logger.info { "launch thread - before delay set reset, thread local value: ${threadLocal.get()} | ${anotherThreadLocal.get()}" }

            delay(5)

            logger.info { "launch thread - after delay set reset, thread local value: ${threadLocal.get()} | ${anotherThreadLocal.get()}" }
            // !!! After suspended delay function, reset ThreadLocal value is lost !!!
            // assertEquals(resetA, threadLocal.get())
            assertEquals(launch1Value, threadLocal.get())
            // !!! After suspended delay, ThreadLocal without ThreadContextElement is not clear !!!
            assertEquals(resetAnother, anotherThreadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())
        }.join()

        logger.info { "test thread - after launch1, thread local value: ${threadLocal.get()} | ${anotherThreadLocal.get()}" }
        assertEquals(mainValue, threadLocal.get())
        assertEquals(anotherMainValue, anotherThreadLocal.get())

        val launch2Value = "aLaunch2"
        val anotherLaunch2Value = "anotherLaunch2"
        launch(
            Dispatchers.Default + threadLocal.asContextElement(value = launch2Value) + anotherThreadLocal.asContextElement(
                value = anotherLaunch2Value
            )
        ) {
            logger.info { "launch thread - launch start, thread local value: ${threadLocal.get()} | ${anotherThreadLocal.get()}" }
            assertEquals(launch2Value, threadLocal.get())
            assertEquals(anotherLaunch2Value, anotherThreadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())

            delay(5)

            logger.info { "launch thread - after delay, thread local value: ${threadLocal.get()} | ${anotherThreadLocal.get()}" }
            assertEquals(launch2Value, threadLocal.get())
            assertEquals(anotherLaunch2Value, anotherThreadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())

            val resetA = "job-reset-${threadLocal.get()}"
            threadLocal.set(resetA)
            val resetAnother = "job-reset-${anotherThreadLocal.get()}"
            anotherThreadLocal.set(resetAnother)
            logger.info { "launch thread - before delay set reset, thread local value: ${threadLocal.get()} | ${anotherThreadLocal.get()}" }

            delay(5)

            logger.info { "launch thread - after delay set reset, thread local value: ${threadLocal.get()} | ${anotherThreadLocal.get()}" }
            // !!! After suspended delay function, reset ThreadLocal value is lost !!!
            // assertEquals(resetA, threadLocal.get())
            // assertEquals(resetAnother, anotherThreadLocal.get())
            assertEquals(launch2Value, threadLocal.get())
            assertEquals(anotherLaunch2Value, anotherThreadLocal.get())
            assertNotEquals(testThread, Thread.currentThread())
        }.join()

        logger.info { "test thread - after launch2, thread local value: ${threadLocal.get()} | ${anotherThreadLocal.get()}" }
        assertEquals(mainValue, threadLocal.get())
        assertEquals(anotherMainValue, anotherThreadLocal.get())
    }
}
