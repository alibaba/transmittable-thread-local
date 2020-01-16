package com.alibaba.ttl.threadpool

import com.alibaba.support.junit.conditional.BelowJava7
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule.ConditionalIgnore
import com.alibaba.ttl.TtlUnwrap
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.ForkJoinPool

class TtlForkJoinPoolHelperTest {
    @Rule
    @JvmField
    val rule = ConditionalIgnoreRule()

    @Test
    @ConditionalIgnore(condition = BelowJava7::class)
    fun test_DisableInheritableForkJoinWorkerThreadFactory() {
        TtlForkJoinPoolHelper.getDefaultDisableInheritableForkJoinWorkerThreadFactory().let {
            assertTrue(it is DisableInheritableForkJoinWorkerThreadFactory)
            assertTrue(TtlForkJoinPoolHelper.isDisableInheritableForkJoinWorkerThreadFactory(it))

            assertSame(ForkJoinPool.defaultForkJoinWorkerThreadFactory, TtlForkJoinPoolHelper.unwrap(it))
            assertSame(ForkJoinPool.defaultForkJoinWorkerThreadFactory, TtlUnwrap.unwrap(it))
        }
    }

    @Test
    @ConditionalIgnore(condition = BelowJava7::class)
    fun test_null() {
        assertFalse(TtlForkJoinPoolHelper.isDisableInheritableForkJoinWorkerThreadFactory(null))
        assertNull(TtlForkJoinPoolHelper.unwrap(null as? ForkJoinPool.ForkJoinWorkerThreadFactory))
    }
}
