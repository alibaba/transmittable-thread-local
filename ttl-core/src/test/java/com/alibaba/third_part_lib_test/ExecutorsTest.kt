package com.alibaba.third_part_lib_test

import com.alibaba.shutdownForTest
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor


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

            threadPool.remove(it).shouldBeTrue()
            threadPool.remove(it).shouldBeFalse()
        }

        // wait sleep task finished.
        futures.forEach { it.get() }

        threadPool.shutdownForTest()
    }
}


