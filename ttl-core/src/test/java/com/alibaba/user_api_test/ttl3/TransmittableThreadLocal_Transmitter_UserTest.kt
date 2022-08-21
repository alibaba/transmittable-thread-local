package com.alibaba.user_api_test.ttl3

import com.alibaba.expandThreadPool
import com.alibaba.ttl3.TransmittableThreadLocal
import com.alibaba.ttl3.transmitter.Transmitter
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.Assert.assertNull
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Test [Transmitter] from user code(different package)
 */
class TransmittableThreadLocal_Transmitter_UserTest : AnnotationSpec() {

    @Test
    fun test_crr() {
        val ttl = TransmittableThreadLocal<String>()
        ttl.set(PARENT)

        val capture = Transmitter.capture()

        val future = executorService.submit {
            ttl.set(CHILD)

            val backup = Transmitter.replay(capture)

            ttl.get() shouldBe PARENT

            Transmitter.restore(backup)

            ttl.get() shouldBe CHILD
        }

        ttl.get() shouldBe PARENT

        future.get(1, TimeUnit.SECONDS)

        ttl.get() shouldBe PARENT
    }

    @Test
    fun test_clear_restore() {
        val ttl = TransmittableThreadLocal<String>()
        ttl.set(PARENT)

        val future = executorService.submit {
            ttl.set(CHILD)

            val backup = Transmitter.clear()


            ttl.get().shouldBeNull()

            Transmitter.restore(backup)

            ttl.get() shouldBe CHILD
        }

        ttl.get() shouldBe PARENT

        future.get(1, TimeUnit.SECONDS)

        ttl.get() shouldBe PARENT
    }

    @Test
    fun test_runSupplierWithCaptured() {
        val ttl = TransmittableThreadLocal<String>()
        ttl.set(PARENT)

        val capture = Transmitter.capture()

        val future = executorService.submit {
            ttl.set("child")
            Transmitter.runSupplierWithCaptured(capture) {
                ttl.get() shouldBe PARENT
                ttl.get()
            }
        }

        ttl.get() shouldBe PARENT

        future.get(1, TimeUnit.SECONDS)

        ttl.get() shouldBe PARENT
    }

    @Test
    fun test_runSupplierWithClear() {
        val ttl = TransmittableThreadLocal<String>()
        ttl.set(PARENT)

        val future = executorService.submit {
            ttl.set("child")
            Transmitter.runSupplierWithClear {
                assertNull(ttl.get())
                ttl.get()
            }
        }

        ttl.get() shouldBe PARENT

        future.get(1, TimeUnit.SECONDS)

        ttl.get() shouldBe PARENT
    }

    @Test
    fun test_runCallableWithCaptured() {
        val ttl = TransmittableThreadLocal<String>()
        ttl.set(PARENT)

        val capture = Transmitter.capture()

        val future = executorService.submit {
            ttl.set("child")
            try {
                Transmitter.runCallableWithCaptured(capture) {
                    ttl.get() shouldBe PARENT
                    ttl.get()
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

        ttl.get() shouldBe PARENT

        future.get(1, TimeUnit.SECONDS)

        ttl.get() shouldBe PARENT
    }

    @Test
    fun test_runCallableWithClear() {
        val ttl = TransmittableThreadLocal<String>()
        ttl.set(PARENT)

        val future = executorService.submit {
            ttl.set("child")
            try {
                Transmitter.runCallableWithClear {
                    ttl.get().shouldBeNull()
                    ttl.get()
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

        ttl.get() shouldBe PARENT

        future.get(1, TimeUnit.SECONDS)

        ttl.get() shouldBe PARENT
    }


    @AfterAll
    fun afterAll() {
        executorService.shutdown()
        executorService.awaitTermination(1, TimeUnit.SECONDS).shouldBeTrue()
    }

    companion object {
        private val PARENT = "parent: " + Date()
        private val CHILD = "child: " + Date()

        private val executorService: ExecutorService = Executors.newFixedThreadPool(3).also { expandThreadPool(it) }
    }
}
