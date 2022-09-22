package com.alibaba.ttl.forkjoin

import com.alibaba.expandThreadPool
import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.ttl.threadpool.agent.TtlAgent
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.util.concurrent.ForkJoinPool


private const val hello = "hello"

class ForkJoinPool4StreamTest : AnnotationSpec() {

    @Test
    fun test_stream_with_agent() {
        if (!TtlAgent.isTtlAgentLoaded()) return

        expandThreadPool(ForkJoinPool.commonPool())

        val ttl = TransmittableThreadLocal<String?>()
        ttl.set(hello)

        (0..100).map {
            ForkJoinPool.commonPool().submit {
                ttl.get() shouldBe hello
            }
        }.forEach { it.get() }

        (0..1000).toList().stream().parallel().mapToInt {
            ttl.get() shouldBe hello

            it
        }.sum() shouldBe (0..1000).sum()
    }

    @Test
    fun test_stream_no_agent() {
        if (TtlAgent.isTtlAgentLoaded()) return

        val name = Thread.currentThread().name
        expandThreadPool(ForkJoinPool.commonPool())

        val ttl = TransmittableThreadLocal<String?>()
        ttl.set(hello)

        (0..100).map {
            ForkJoinPool.commonPool().submit {
                if (Thread.currentThread().name == name) ttl.get() shouldBe hello
                else ttl.get().shouldBeNull()
            }
        }.forEach { it.get() }

        (0..1000).toList().stream().parallel().mapToInt {
            if (Thread.currentThread().name == name) ttl.get() shouldBe hello
            else ttl.get().shouldBeNull()

            it
        }.sum() shouldBe (0..1000).sum()
    }
}
