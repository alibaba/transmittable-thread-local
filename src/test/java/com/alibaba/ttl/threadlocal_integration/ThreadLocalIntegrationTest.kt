package com.alibaba.ttl.threadlocal_integration

import com.alibaba.expandThreadPool
import com.alibaba.ttl.TransmittableThreadLocal.Transmitter.*
import com.alibaba.ttl.TtlCopier
import com.alibaba.ttl.threadpool.TtlExecutors
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class ThreadLocalIntegrationTest : AnnotationSpec() {
    @Test
    fun threadLocal_do_NOT_transmit() {
        val threadLocal = ThreadLocal<String>()
        unregisterThreadLocal(threadLocal).shouldBeFalse()
        threadLocal.set(PARENT)

        assertNotTransmit(threadLocal)
    }

    private fun assertNotTransmit(threadLocal: ThreadLocal<String>) {
        val future = executorService.submit {
            threadLocal.get().shouldBeNull()
            threadLocal.set(CHILD)
        }

        threadLocal.get() shouldBe PARENT
        future.get(1, TimeUnit.SECONDS)

        unregisterThreadLocal(threadLocal).shouldBeFalse()
    }

    @Test
    fun threadLocal_registerThreadLocalWithShadowCopier_do_transmit() {
        val threadLocal = ThreadLocal<String>()
        unregisterThreadLocal(threadLocal).shouldBeFalse()
        threadLocal.set(PARENT)

        registerThreadLocalWithShadowCopier(threadLocal).shouldBeTrue()
        assertTransmitShadowCopy(threadLocal)

        // Unregister
        unregisterThreadLocal(threadLocal).shouldBeTrue()
        assertNotTransmit(threadLocal)
    }


    private fun assertTransmitShadowCopy(threadLocal: ThreadLocal<String>) {
        val future = executorService.submit {
            threadLocal.get() shouldBe PARENT
            threadLocal.set(CHILD)
        }

        threadLocal.get() shouldBe PARENT
        future.get(1, TimeUnit.SECONDS)
    }

    @Test
    fun threadLocal_registerThreadLocal_and_force() {
        val threadLocal = ThreadLocal<String>()
        unregisterThreadLocal(threadLocal).shouldBeFalse()
        registerThreadLocal(threadLocal, APPEND_SUFFIX_COPIER).shouldBeTrue()

        threadLocal.set(PARENT)
        assertTransmitSuffixCopy(threadLocal)

        registerThreadLocalWithShadowCopier(threadLocal, true).shouldBeTrue()
        // copier changed
        assertTransmitShadowCopy(threadLocal)

        registerThreadLocal(threadLocal, APPEND_SUFFIX_COPIER).shouldBeFalse()
        // copier do not change
        assertTransmitShadowCopy(threadLocal)

        registerThreadLocal(threadLocal, APPEND_SUFFIX_COPIER, true).shouldBeTrue()
        // copier changed
        assertTransmitSuffixCopy(threadLocal)

        // Unregister
        unregisterThreadLocal(threadLocal).shouldBeTrue()
        assertNotTransmit(threadLocal)
    }

    private fun assertTransmitSuffixCopy(threadLocal: ThreadLocal<String>) {
        val future = executorService.submit {
            threadLocal.get() shouldBe "$PARENT$COPY_SUFFIX"
            threadLocal.set(CHILD)
        }

        threadLocal.get() shouldBe PARENT
        future.get(1, TimeUnit.SECONDS)
    }

    @Test
    fun test_clear() {
        val initValue = "init"

        val threadLocal = object : ThreadLocal<String>() {
            override fun initialValue(): String = initValue
        }
        threadLocal.get() shouldBe initValue
        threadLocal.set(PARENT)

        registerThreadLocalWithShadowCopier(threadLocal).shouldBeTrue()

        runCallableWithClear {
            val future = executorService.submit {
                threadLocal.get() shouldBe initValue
                threadLocal.set(CHILD)
            }

            threadLocal.get() shouldBe initValue
            future.get(1, TimeUnit.SECONDS)
        }

        threadLocal.get() shouldBe PARENT
    }

    @Test
    fun register_ThreadLocal_can_NOT_Inheritable() {
        val initValue = "init"
        val threadLocal = object : ThreadLocal<String>() {
            override fun initialValue(): String = initValue
        }
        threadLocal.set(PARENT)
        registerThreadLocalWithShadowCopier(threadLocal).shouldBeTrue()

        val blockingQueue = LinkedBlockingQueue<String>(1)
        thread {
            blockingQueue.add(threadLocal.get())
        }

        blockingQueue.take() shouldBe initValue
    }

    @Test
    fun register_InheritableThreadLocal_can_Inheritable() {
        val initValue = "init"
        val threadLocal = object : InheritableThreadLocal<String>() {
            override fun initialValue(): String = initValue
        }
        threadLocal.set(PARENT)
        registerThreadLocalWithShadowCopier(threadLocal).shouldBeTrue()

        val blockingQueue = LinkedBlockingQueue<String>(1)
        thread {
            blockingQueue.add(threadLocal.get())
        }

        blockingQueue.take() shouldBe PARENT
    }

    @AfterAll
    fun afterAll() {
        executorService.shutdown()
        // Fail to shut down thread pool
        executorService.awaitTermination(1, TimeUnit.SECONDS).shouldBeTrue()
    }

    companion object {
        private val PARENT = "parent: " + Date()
        private val CHILD = "child: " + Date()
        private const val COPY_SUFFIX = " 42"
        private val APPEND_SUFFIX_COPIER = TtlCopier<String> { "$it$COPY_SUFFIX" }

        private val executorService: ExecutorService = Executors.newFixedThreadPool(3).let {
            expandThreadPool(it)
            TtlExecutors.getTtlExecutorService(it)
        }!!
    }
}
