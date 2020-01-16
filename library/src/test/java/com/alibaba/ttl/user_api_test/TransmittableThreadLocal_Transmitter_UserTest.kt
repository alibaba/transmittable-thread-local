package com.alibaba.ttl.user_api_test

import com.alibaba.expandThreadPool
import com.alibaba.support.junit.conditional.BelowJava8
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule.ConditionalIgnore
import com.alibaba.ttl.TransmittableThreadLocal
import org.junit.AfterClass
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Test [TransmittableThreadLocal.Transmitter] from user code(different package)
 */
class TransmittableThreadLocal_Transmitter_UserTest {
    @Rule
    @JvmField
    val rule = ConditionalIgnoreRule()

    @Test
    fun test_crr() {
        val ttl = TransmittableThreadLocal<String>()
        ttl.set(PARENT)

        val capture = TransmittableThreadLocal.Transmitter.capture()

        val future = executorService.submit {
            ttl.set(CHILD)

            val backup = TransmittableThreadLocal.Transmitter.replay(capture)

            assertEquals(PARENT, ttl.get())

            TransmittableThreadLocal.Transmitter.restore(backup)

            assertEquals(CHILD, ttl.get())
        }

        assertEquals(PARENT, ttl.get())

        future.get(100, TimeUnit.MILLISECONDS)

        assertEquals(PARENT, ttl.get())
    }

    @Test
    fun test_clear_restore() {
        val ttl = TransmittableThreadLocal<String>()
        ttl.set(PARENT)

        val future = executorService.submit {
            ttl.set(CHILD)

            val backup = TransmittableThreadLocal.Transmitter.clear()

            assertNull(ttl.get())

            TransmittableThreadLocal.Transmitter.restore(backup)

            assertEquals(CHILD, ttl.get())
        }

        assertEquals(PARENT, ttl.get())

        future.get(100, TimeUnit.MILLISECONDS)

        assertEquals(PARENT, ttl.get())
    }

    @Test
    @ConditionalIgnore(condition = BelowJava8::class)
    fun test_runSupplierWithCaptured() {
        val ttl = TransmittableThreadLocal<String>()
        ttl.set(PARENT)

        val capture = TransmittableThreadLocal.Transmitter.capture()

        val future = executorService.submit {
            ttl.set("child")
            TransmittableThreadLocal.Transmitter.runSupplierWithCaptured(capture) {
                assertEquals(PARENT, ttl.get())
                ttl.get()
            }
        }

        assertEquals(PARENT, ttl.get())

        future.get(100, TimeUnit.MILLISECONDS)

        assertEquals(PARENT, ttl.get())
    }

    @Test
    @ConditionalIgnore(condition = BelowJava8::class)
    fun test_runSupplierWithClear() {
        val ttl = TransmittableThreadLocal<String>()
        ttl.set(PARENT)

        val future = executorService.submit {
            ttl.set("child")
            TransmittableThreadLocal.Transmitter.runSupplierWithClear {
                assertNull(ttl.get())
                ttl.get()
            }
        }

        assertEquals(PARENT, ttl.get())

        future.get(100, TimeUnit.MILLISECONDS)

        assertEquals(PARENT, ttl.get())
    }

    @Test
    fun test_runCallableWithCaptured() {
        val ttl = TransmittableThreadLocal<String>()
        ttl.set(PARENT)

        val capture = TransmittableThreadLocal.Transmitter.capture()

        val future = executorService.submit {
            ttl.set("child")
            try {
                TransmittableThreadLocal.Transmitter.runCallableWithCaptured(capture) {
                    assertEquals(PARENT, ttl.get())
                    ttl.get()
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

        assertEquals(PARENT, ttl.get())

        future.get(100, TimeUnit.MILLISECONDS)

        assertEquals(PARENT, ttl.get())
    }

    @Test
    fun test_runCallableWithClear() {
        val ttl = TransmittableThreadLocal<String>()
        ttl.set(PARENT)

        val future = executorService.submit {
            ttl.set("child")
            try {
                TransmittableThreadLocal.Transmitter.runCallableWithClear {
                    assertNull(ttl.get())
                    ttl.get()
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

        assertEquals(PARENT, ttl.get())

        future.get(100, TimeUnit.MILLISECONDS)

        assertEquals(PARENT, ttl.get())
    }

    companion object {
        private val PARENT = "parent: " + Date()
        private val CHILD = "child: " + Date()

        private val executorService: ExecutorService = Executors.newFixedThreadPool(3).also { expandThreadPool(it) }

        @AfterClass
        @JvmStatic
        @Suppress("unused")
        fun afterClass() {
            executorService.shutdown()
            executorService.awaitTermination(100, TimeUnit.MILLISECONDS)
            if (!executorService.isTerminated) fail("Fail to shutdown thread pool")
        }
    }
}
