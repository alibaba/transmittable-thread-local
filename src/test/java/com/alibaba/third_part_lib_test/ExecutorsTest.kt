package com.alibaba.third_part_lib_test

import io.kotest.core.spec.style.AnnotationSpec
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


class ExecutorsTest : AnnotationSpec() {
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
        assertTrue("Fail to shutdown thread pool", threadPool.awaitTermination(100, TimeUnit.MILLISECONDS))
    }
}


