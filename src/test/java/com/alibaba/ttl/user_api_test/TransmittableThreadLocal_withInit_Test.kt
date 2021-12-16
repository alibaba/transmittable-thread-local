package com.alibaba.ttl.user_api_test

import com.alibaba.expandThreadPool
import com.alibaba.support.junit.conditional.BelowJava8
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule.ConditionalIgnore
import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.ttl.threadpool.TtlExecutors
import org.junit.AfterClass
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

class TransmittableThreadLocal_withInit_Test {
    @Rule
    @JvmField
    val rule = ConditionalIgnoreRule()

    @Test
    @ConditionalIgnore(condition = BelowJava8::class)
    fun test_withInit() {
        val ttl: TransmittableThreadLocal<Int> = TransmittableThreadLocal.withInitial { 42 }

        assertNotNull(ttl)
        assertEquals(42, ttl.get())

        val atomicInteger = AtomicInteger(-1)
        thread {
            atomicInteger.set(ttl.get())
        }.join()
        assertEquals(42, atomicInteger.get())

        atomicInteger.set(-1)
        executorService.submit {
            atomicInteger.set(ttl.get())
        }.get()
        assertEquals(42, atomicInteger.get())
    }

    @Test
    @ConditionalIgnore(condition = BelowJava8::class)
    fun test_withInitialAndCopier_2() {
        val ttl = TransmittableThreadLocal.withInitialAndCopier(
            { 42 },
            { it + 100 },
        )
        assertNotNull(ttl)
        assertEquals(42, ttl.get())

        val atomicInteger = AtomicInteger(-1)
        thread {
            atomicInteger.set(ttl.get())
        }.join()
        assertEquals(142, atomicInteger.get())

        atomicInteger.set(-1)
        executorService.submit {
            atomicInteger.set(ttl.get())
        }.get()
        assertEquals(142, atomicInteger.get())
    }

    @Test
    @ConditionalIgnore(condition = BelowJava8::class)
    fun test_withInitialAndCopier_3() {
        val ttl = TransmittableThreadLocal.withInitialAndCopier(
            { 42 },
            { it + 100 },
            { it + 1000 },
        )
        assertNotNull(ttl)
        assertEquals(42, ttl.get())

        val atomicInteger = AtomicInteger(-1)
        thread {
            atomicInteger.set(ttl.get())
        }.join()
        assertEquals(142, atomicInteger.get())

        atomicInteger.set(-1)
        executorService.submit {
            atomicInteger.set(ttl.get())
        }.get()
        assertEquals(1042, atomicInteger.get())
    }

    companion object {
        private val executorService: ExecutorService = Executors.newFixedThreadPool(3).let {
            expandThreadPool(it)
            TtlExecutors.getTtlExecutorService(it)!!
        }

        @AfterClass
        @JvmStatic
        @Suppress("unused")
        fun afterClass() {
            executorService.shutdown()
            Assert.assertTrue(
                "Fail to shutdown thread pool",
                executorService.awaitTermination(100, TimeUnit.MILLISECONDS)
            )
        }
    }
}
