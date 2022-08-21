package com.alibaba.user_api_test.ttl3

import com.alibaba.expandThreadPool
import com.alibaba.ttl3.TransmittableThreadLocal
import com.alibaba.ttl3.executor.TtlExecutors
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

class TransmittableThreadLocal_withInit_Test : AnnotationSpec() {

    @Test
    fun test_withInit() {
        val ttl: TransmittableThreadLocal<Int> = TransmittableThreadLocal.withInitial { 42 }

        ttl.shouldNotBeNull()
        ttl.get() shouldBe 42

        val atomicInteger = AtomicInteger(-1)
        thread {
            atomicInteger.set(ttl.get())
        }.join()
        atomicInteger.get() shouldBe 42

        atomicInteger.set(-1)
        executorService.submit {
            atomicInteger.set(ttl.get())
        }.get()
        atomicInteger.get() shouldBe 42
    }

    @Test
    fun test_withInitialAndCopier_2() {
        val ttl = TransmittableThreadLocal.withInitialAndCopier(
            { 42 },
            { it + 100 },
        )
        ttl.shouldNotBeNull()
        ttl.get() shouldBe 42

        val atomicInteger = AtomicInteger(-1)
        thread {
            atomicInteger.set(ttl.get())
        }.join()
        atomicInteger.get() shouldBe 142

        atomicInteger.set(-1)
        executorService.submit {
            atomicInteger.set(ttl.get())
        }.get()
        atomicInteger.get() shouldBe 142
    }

    @Test
    fun test_withInitialAndCopier_3() {
        val ttl = TransmittableThreadLocal.withInitialAndCopier(
            { 42 },
            { it + 100 },
            { it + 1000 },
        )
        ttl.shouldNotBeNull()
        ttl.get() shouldBe 42

        val atomicInteger = AtomicInteger(-1)
        thread {
            atomicInteger.set(ttl.get())
        }.join()
        atomicInteger.get() shouldBe 142

        atomicInteger.set(-1)
        executorService.submit {
            atomicInteger.set(ttl.get())
        }.get()
        atomicInteger.get() shouldBe 1042
    }

    @AfterAll
    fun afterClass() {
        executorService.shutdown()
        executorService.awaitTermination(1, TimeUnit.SECONDS).shouldBeTrue()
    }

    companion object {
        private val executorService: ExecutorService = Executors.newFixedThreadPool(3).let {
            expandThreadPool(it)
            TtlExecutors.getTtlExecutorService(it)!!
        }
    }
}
