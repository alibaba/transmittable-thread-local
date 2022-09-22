package com.alibaba.ttl.threadpool

import com.alibaba.ttl.TtlUnwrap
import io.kotest.core.spec.style.AnnotationSpec
import org.junit.Assert.*
import java.util.concurrent.ForkJoinPool

class TtlForkJoinPoolHelperTest : AnnotationSpec() {
    @Test
    fun test_DisableInheritableForkJoinWorkerThreadFactory() {
        TtlForkJoinPoolHelper.getDefaultDisableInheritableForkJoinWorkerThreadFactory().let {
            assertTrue(it is DisableInheritableForkJoinWorkerThreadFactory)
            assertTrue(TtlForkJoinPoolHelper.isDisableInheritableForkJoinWorkerThreadFactory(it))

            assertSame(ForkJoinPool.defaultForkJoinWorkerThreadFactory, TtlForkJoinPoolHelper.unwrap(it))
            assertSame(ForkJoinPool.defaultForkJoinWorkerThreadFactory, TtlUnwrap.unwrap(it))
        }
    }

    @Test
    fun test_null() {
        assertFalse(TtlForkJoinPoolHelper.isDisableInheritableForkJoinWorkerThreadFactory(null))
        assertNull(TtlForkJoinPoolHelper.unwrap(null))
    }
}
