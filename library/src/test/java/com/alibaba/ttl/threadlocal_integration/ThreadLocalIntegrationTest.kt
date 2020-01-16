package com.alibaba.ttl.threadlocal_integration

import com.alibaba.expandThreadPool
import com.alibaba.ttl.TransmittableThreadLocal.Transmitter.*
import com.alibaba.ttl.TtlCopier
import com.alibaba.ttl.threadpool.TtlExecutors
import org.junit.AfterClass
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class ThreadLocalIntegrationTest {
    @Test
    fun threadLocal_do_NOT_transmit() {
        val threadLocal = ThreadLocal<String>()
        unregisterThreadLocal(threadLocal).also { assertFalse(it) }
        threadLocal.set(PARENT)

        assertNotTransmit(threadLocal)
    }

    private fun assertNotTransmit(threadLocal: ThreadLocal<String>) {
        val future = executorService.submit {
            assertNull(threadLocal.get())
            threadLocal.set(CHILD)
        }

        assertEquals(PARENT, threadLocal.get())
        future.get(100, TimeUnit.MILLISECONDS)

        unregisterThreadLocal(threadLocal).also { assertFalse(it) }
    }

    @Test
    fun threadLocal_registerThreadLocalWithShadowCopier_do_transmit() {
        val threadLocal = ThreadLocal<String>()
        unregisterThreadLocal(threadLocal).also { assertFalse(it) }
        threadLocal.set(PARENT)

        registerThreadLocalWithShadowCopier(threadLocal).also { assertTrue(it) }
        assertTransmitShadowCopy(threadLocal)

        // Unregister
        unregisterThreadLocal(threadLocal).also { assertTrue(it) }
        assertNotTransmit(threadLocal)
    }


    private fun assertTransmitShadowCopy(threadLocal: ThreadLocal<String>) {
        val future = executorService.submit {
            assertEquals(PARENT, threadLocal.get())
            threadLocal.set(CHILD)
        }

        assertEquals(PARENT, threadLocal.get())
        future.get(100, TimeUnit.MILLISECONDS)
    }

    @Test
    fun threadLocal_registerThreadLocal_and_force() {
        val threadLocal = ThreadLocal<String>()
        unregisterThreadLocal(threadLocal).also { assertFalse(it) }
        registerThreadLocal(threadLocal, APPEND_SUFFIX_COPIER).also { assertTrue(it) }

        threadLocal.set(PARENT)
        assertTransmitSuffixCopy(threadLocal)

        registerThreadLocalWithShadowCopier(threadLocal, true).also { assertTrue(it) }
        // copier changed
        assertTransmitShadowCopy(threadLocal)

        registerThreadLocal(threadLocal, APPEND_SUFFIX_COPIER).also { assertFalse(it) }
        // copier do not change
        assertTransmitShadowCopy(threadLocal)

        registerThreadLocal(threadLocal, APPEND_SUFFIX_COPIER, true).also { assertTrue(it) }
        // copier changed
        assertTransmitSuffixCopy(threadLocal)

        // Unregister
        unregisterThreadLocal(threadLocal).also { assertTrue(it) }
        assertNotTransmit(threadLocal)
    }

    private fun assertTransmitSuffixCopy(threadLocal: ThreadLocal<String>) {
        val future = executorService.submit {
            assertEquals("$PARENT$COPY_SUFFIX", threadLocal.get())
            threadLocal.set(CHILD)
        }

        assertEquals(PARENT, threadLocal.get())
        future.get(100, TimeUnit.MILLISECONDS)
    }

    @Test
    fun test_clear() {
        val initValue = "init"

        val threadLocal = object : ThreadLocal<String>() {
            override fun initialValue(): String = initValue
        }
        assertEquals(initValue, threadLocal.get())
        threadLocal.set(PARENT)

        registerThreadLocalWithShadowCopier(threadLocal).also { assertTrue(it) }

        runCallableWithClear {
            val future = executorService.submit {
                assertEquals(initValue, threadLocal.get())
                threadLocal.set(CHILD)
            }

            assertEquals(initValue, threadLocal.get())
            future.get(100, TimeUnit.MILLISECONDS)
        }

        assertEquals(PARENT, threadLocal.get())
    }

    @Test
    fun register_ThreadLocal_can_NOT_Inheritable() {
        val initValue = "init"
        val threadLocal = object : ThreadLocal<String>() {
            override fun initialValue(): String? = initValue
        }
        threadLocal.set(PARENT)
        registerThreadLocalWithShadowCopier(threadLocal).also { assertTrue(it) }

        val blockingQueue = LinkedBlockingQueue<String>(1)
        thread {
            blockingQueue.add(threadLocal.get())
        }

        assertEquals(initValue, blockingQueue.take())
    }

    @Test
    fun register_InheritableThreadLocal_can_Inheritable() {
        val initValue = "init"
        val threadLocal = object : InheritableThreadLocal<String>() {
            override fun initialValue(): String? = initValue
        }
        threadLocal.set(PARENT)
        registerThreadLocalWithShadowCopier(threadLocal).also { assertTrue(it) }

        val blockingQueue = LinkedBlockingQueue<String>(1)
        thread {
            blockingQueue.add(threadLocal.get())
        }

        assertEquals(PARENT, blockingQueue.take())
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

        @AfterClass
        @JvmStatic
        @Suppress("unused")
        fun afterClass() {
            executorService.shutdown()
            executorService.awaitTermination(100, TimeUnit.MILLISECONDS)
            if (!executorService.isTerminated) Assert.fail("Fail to shutdown thread pool")
        }
    }
}
