package com.alibaba.ttl.threadpool

import com.alibaba.support.junit.conditional.ConditionalIgnoreRule
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule.ConditionalIgnore
import com.alibaba.support.junit.conditional.IsAgentRun
import com.alibaba.ttl.threadpool.TtlExecutors.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.Executors.newScheduledThreadPool

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class TtlExecutorsTest {
    @Rule
    @JvmField
    val rule = ConditionalIgnoreRule()

    @Test
    @ConditionalIgnore(condition = IsAgentRun::class)
    fun test_common() {
        val newScheduledThreadPool = newScheduledThreadPool(3)

        getTtlExecutor(newScheduledThreadPool).let {
            assertTrue(it is ExecutorTtlWrapper)
            assertTrue(isTtlWrapper(it))

            assertSame(newScheduledThreadPool, unwrap(it))
        }
        getTtlExecutorService(newScheduledThreadPool).let {
            assertTrue(it is ExecutorServiceTtlWrapper)
            assertTrue(isTtlWrapper(it))

            assertSame(newScheduledThreadPool, unwrap(it))
        }
        getTtlScheduledExecutorService(newScheduledThreadPool).let {
            assertTrue(it is ScheduledExecutorServiceTtlWrapper)
            assertTrue(isTtlWrapper(it))

            assertSame(newScheduledThreadPool, unwrap(it))
        }
    }

    @Test
    fun test_null() {
        assertNull(getTtlExecutor(null))
        assertNull(getTtlExecutorService(null))
        assertNull(getTtlScheduledExecutorService(null))

        assertFalse(isTtlWrapper(null))
        assertNull(unwrap(null))
    }
}
