package com.alibaba.ttl.threadpool

import org.junit.Assert.assertNull
import org.junit.Test

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class TtlExecutorsTest {
    @Test
    fun test_null() {
        assertNull(TtlExecutors.getTtlExecutor(null))
        assertNull(TtlExecutors.getTtlExecutorService(null))
        assertNull(TtlExecutors.getTtlScheduledExecutorService(null))
    }
}
