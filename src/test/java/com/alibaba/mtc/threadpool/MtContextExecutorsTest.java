package com.alibaba.mtc.threadpool;

import org.junit.Test;

import static org.junit.Assert.assertNull;

/**
 * @author ding.lid
 */
public class MtContextExecutorsTest {
    @Test
    public void test_null() throws Exception {
        assertNull(MtContextExecutors.getMtcExecutor(null));
        assertNull(MtContextExecutors.getMtcExecutorService(null));
        assertNull(MtContextExecutors.getMtcScheduledExecutorService(null));
    }
}
