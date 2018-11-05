package com.alibaba.ttl.threadpool

import com.alibaba.support.junit.conditional.BelowJava7
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.ForkJoinPool

class TtlForkJoinPoolTest {
    @Rule
    @JvmField
    val rule = ConditionalIgnoreRule()

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = BelowJava7::class)
    fun test_common_ForkJoinPool() {
        TtlForkJoinPool.getDefaultDisableInheritableForkJoinWorkerThreadFactory().let {
            assertTrue(it is DisableInheritableForkJoinWorkerThreadFactory)
            assertTrue(TtlForkJoinPool.isDisableInheritableForkJoinWorkerThreadFactory(it))

            assertSame(ForkJoinPool.defaultForkJoinWorkerThreadFactory, TtlForkJoinPool.unwrap(it))
        }
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = BelowJava7::class)
    fun test_null_ForkJoinPool() {
        assertFalse(TtlForkJoinPool.isDisableInheritableForkJoinWorkerThreadFactory(null))
        assertNull(TtlForkJoinPool.unwrap(null as? ForkJoinPool.ForkJoinWorkerThreadFactory))
    }
}
