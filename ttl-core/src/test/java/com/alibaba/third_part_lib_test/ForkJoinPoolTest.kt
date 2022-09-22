package com.alibaba.third_part_lib_test

import com.alibaba.shutdownForTest
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RecursiveTask
import java.util.concurrent.atomic.AtomicInteger

class ForkJoinPoolTest : AnnotationSpec() {
    @Test
    fun test_sameTaskDirectReturn_onlyExec1Time_ifHaveRun() {
        val pool = ForkJoinPool()

        val numbers = 1L..100L
        val sumTask = SumTask(numbers)

        // same task instance run 10 times
        for (i in 0..9) {
            pool.invoke(sumTask) shouldBe numbers.sum()
        }

        sumTask.execCounter.get() shouldBe 1

        pool.shutdownForTest()
    }
}

private class SumTask(private val numbers: LongRange) : RecursiveTask<Long>() {
    val execCounter = AtomicInteger(0)

    override fun compute(): Long {
        execCounter.incrementAndGet()

        return if (numbers.count() <= 16) {
            // compute directly
            numbers.sum()
        } else {
            // split task
            val middle = numbers.first + numbers.count() / 2

            val taskLeft = SumTask(numbers.first until middle)
            val taskRight = SumTask(middle..numbers.last)

            taskLeft.fork()
            taskRight.fork()
            taskLeft.join() + taskRight.join()
        }
    }
}
