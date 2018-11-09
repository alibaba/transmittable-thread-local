package com.alibaba.ttl.forkjoin

import com.alibaba.expandThreadPool
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule.ConditionalIgnore
import com.alibaba.support.junit.conditional.IsAgentRunOrBelowJava8
import com.alibaba.support.junit.conditional.NoAgentRunOrBelowJava8
import com.alibaba.ttl.TransmittableThreadLocal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.ForkJoinPool


private const val hello = "hello"

class ForkJoinPool4StreamTest {
    @Rule
    @JvmField
    val rule = ConditionalIgnoreRule()

    @Test
    @ConditionalIgnore(condition = NoAgentRunOrBelowJava8::class)
    fun test_stream_with_agent() {
        expandThreadPool(ForkJoinPool.commonPool())

        val ttl = TransmittableThreadLocal<String?>()
        ttl.set(hello)

        (0..100).map {
            ForkJoinPool.commonPool().submit {
                assertEquals(hello, ttl.get())
            }
        }.forEach { it.get() }

        (0..1000).toList().stream().parallel().mapToInt {
            assertEquals(hello, ttl.get())

            it
        }.sum().let {
            assertEquals((0..1000).sum(), it)
        }
    }

    @Test
    @ConditionalIgnore(condition = IsAgentRunOrBelowJava8::class)
    fun test_stream_no_agent() {
        val name = Thread.currentThread().name
        expandThreadPool(ForkJoinPool.commonPool())

        val ttl = TransmittableThreadLocal<String?>()
        ttl.set(hello)

        (0..100).map {
            ForkJoinPool.commonPool().submit {
                if (Thread.currentThread().name == name) assertEquals(hello, ttl.get())
                else assertNull(ttl.get())
            }
        }.forEach { it.get() }

        (0..1000).toList().stream().parallel().mapToInt {
            if (Thread.currentThread().name == name) assertEquals(hello, ttl.get())
            else assertNull(ttl.get())

            it
        }.sum().let {
            assertEquals((0..1000).sum(), it)
        }
    }
}
