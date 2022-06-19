package com.alibaba.user_api_test.ttl

import com.alibaba.expandThreadPool
import com.alibaba.ttl.TransmittableThreadLocal
import io.kotest.core.spec.style.AnnotationSpec
import org.junit.Assert.*
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Test [TransmittableThreadLocal.Transmitter] from user code(different package)
 */
class TransmittableThreadLocal_Transmitter_UserTest : AnnotationSpec() {

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


    @AfterAll
    fun afterAll() {
        executorService.shutdown()
        assertTrue("Fail to shutdown thread pool", executorService.awaitTermination(100, TimeUnit.MILLISECONDS))
    }

    companion object {
        private val PARENT = "parent: " + Date()
        private val CHILD = "child: " + Date()

        private val executorService: ExecutorService = Executors.newFixedThreadPool(3).also { expandThreadPool(it) }
    }
}
