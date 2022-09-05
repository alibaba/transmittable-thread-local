package com.alibaba.user_api_test.ttl3

import com.alibaba.expandThreadPool
import com.alibaba.getForTest
import com.alibaba.shutdownForTest
import com.alibaba.ttl3.TransmittableThreadLocal
import com.alibaba.ttl3.transmitter.Transmitter
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Test [Transmitter] from user code(different package)
 */
class TransmittableThreadLocal_Transmitter_UserTest : AnnotationSpec() {

    @Test
    fun test_crr() {
        val ttl = TransmittableThreadLocal<String>()
        ttl.set(parentValue)

        val capture = Transmitter.capture()

        val future = executorService.submit {
            ttl.set(childValue)

            val backup = Transmitter.replay(capture)

            ttl.get() shouldBe parentValue

            Transmitter.restore(backup)

            ttl.get() shouldBe childValue
        }

        ttl.get() shouldBe parentValue

        future.getForTest()

        ttl.get() shouldBe parentValue
    }

    @Test
    fun test_clear_restore() {
        val ttl = TransmittableThreadLocal<String>()
        ttl.set(parentValue)

        val future = executorService.submit {
            ttl.set(childValue)

            val backup = Transmitter.clear()


            ttl.get().shouldBeNull()

            Transmitter.restore(backup)

            ttl.get() shouldBe childValue
        }

        ttl.get() shouldBe parentValue

        future.getForTest()

        ttl.get() shouldBe parentValue
    }

    @Test
    fun test_runSupplierWithCaptured() {
        val ttl = TransmittableThreadLocal<String>()
        ttl.set(parentValue)

        val capture = Transmitter.capture()

        val future = executorService.submit {
            ttl.set("child")
            Transmitter.runSupplierWithCaptured(capture) {
                ttl.get() shouldBe parentValue
                ttl.get()
            }
        }

        ttl.get() shouldBe parentValue

        future.getForTest()

        ttl.get() shouldBe parentValue
    }

    @Test
    fun test_runSupplierWithClear() {
        val ttl = TransmittableThreadLocal<String>()
        ttl.set(parentValue)

        val future = executorService.submit {
            ttl.set("child")
            Transmitter.runSupplierWithClear {
                ttl.get().shouldBeNull()
                ttl.get()
            }
        }

        ttl.get() shouldBe parentValue

        future.getForTest()

        ttl.get() shouldBe parentValue
    }

    @Test
    fun test_runCallableWithCaptured() {
        val ttl = TransmittableThreadLocal<String>()
        ttl.set(parentValue)

        val capture = Transmitter.capture()

        val future = executorService.submit {
            ttl.set("child")
            try {
                Transmitter.runCallableWithCaptured(capture) {
                    ttl.get() shouldBe parentValue
                    ttl.get()
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

        ttl.get() shouldBe parentValue

        future.getForTest()

        ttl.get() shouldBe parentValue
    }

    @Test
    fun test_runCallableWithClear() {
        val ttl = TransmittableThreadLocal<String>()
        ttl.set(parentValue)

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

        ttl.get() shouldBe parentValue

        future.getForTest()

        ttl.get() shouldBe parentValue
    }

    private val parentValue = "parent: " + Date()
    private val childValue = "child: " + Date()
    private lateinit var executorService: ExecutorService

    @BeforeAll
    fun beforeAll() {
        executorService = Executors.newFixedThreadPool(3).also { expandThreadPool(it) }
    }

    @AfterAll
    fun afterAll() {
        executorService.shutdownForTest()
    }
}
