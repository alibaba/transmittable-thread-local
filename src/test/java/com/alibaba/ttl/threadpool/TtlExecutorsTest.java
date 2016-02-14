package com.alibaba.ttl.threadpool;

import org.junit.Test;

import static org.junit.Assert.assertNull;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class TtlExecutorsTest {
    @Test
    public void test_null() throws Exception {
        assertNull(TtlExecutors.getTtlExecutor(null));
        assertNull(TtlExecutors.getTtlExecutorService(null));
        assertNull(TtlExecutors.getTtlScheduledExecutorService(null));
    }
}
