package com.alibaba.third_part_lib_test

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


class ExecutorsTest {
    @Test
    fun test_remove_of_ThreadPoolExecutor() {
        val size = 2
        val threadPool = Executors.newFixedThreadPool(size) as ThreadPoolExecutor

        val futures = (0..size * 2).map {
            threadPool.submit {
                Thread.sleep(10)
            }
        }

        Runnable {
            println("Task should be removed!")
        }.let {
            threadPool.execute(it)
            assertTrue(threadPool.remove(it))
            assertFalse(threadPool.remove(it))
        }

        // wait sleep task finished.
        futures.forEach { it.get() }

        threadPool.shutdown()
        assertTrue(threadPool.awaitTermination(10, TimeUnit.MILLISECONDS))
    }
}


