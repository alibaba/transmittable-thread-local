package com.alibaba.ttl.integration.spring.executors

import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.ttl.threadpool.agent.TtlAgent
import org.junit.Assert
import org.junit.Test
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.lang.Thread.sleep
import java.util.concurrent.Callable

class SpringExecutorsTest {
    @Test
    fun test_ThreadPoolTaskExecutor() {
        val ttl = TransmittableThreadLocal<String>()
        val initValue = "init_value"
        ttl.set(initValue)

        val coreSize = 2
        val executor = ThreadPoolTaskExecutor().apply {
            corePoolSize = coreSize
            initialize()
        }

        (0 until coreSize * 2).map {
            executor.submit { sleep(30) }
        }.forEach {
            it.get()
        }
        val resetValue = "value_reset"
        ttl.set(resetValue)

        val callable = Callable { ttl.get() }

        println("========================================");
        if (TtlAgent.isTtlAgentLoaded()) {
            println("Test **WITH** TTL Agent");
            Assert.assertEquals(resetValue, executor.submit(callable).get())
        } else {
            println("Test WITHOUT TTL Agent");
            Assert.assertEquals(initValue, executor.submit(callable).get())
        }
        println("========================================");

        executor.destroy()
    }
}
