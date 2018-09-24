package com.alibaba.third_part_lib_test

import com.alibaba.support.junit.conditional.BelowJava7
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule.ConditionalIgnore
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RecursiveTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger


class ForkJoinPoolTest {
    @Rule
    @JvmField
    val rule = ConditionalIgnoreRule()

    @Test
    @ConditionalIgnore(condition = BelowJava7::class)
    fun test_sameTaskDirectReturn_onlyExec1Time_ifHaveRun() {
        val pool = ForkJoinPool()

        val numbers = 1L..100L
        val sumTask = SumTask(numbers)

        // same task instance run 10 times
        for (i in 0..9) {
            assertEquals(numbers.sum(), pool.invoke(sumTask).toLong())
        }

        assertEquals(1, sumTask.execCounter.get().toLong())

        // close
        pool.shutdown()
        if (!pool.awaitTermination(100, TimeUnit.MILLISECONDS)) fail("Fail to shutdown thread pool")
    }
}


internal class SumTask(private val numbers: LongRange) : RecursiveTask<Long>() {
    val execCounter = AtomicInteger(0)

    override fun compute(): Long {
        execCounter.incrementAndGet()

        return if (numbers.count() <= 16) {
            // compute directly
            numbers.sum()
        } else {
            // split task
            val middle = numbers.start + numbers.count() / 2

            val taskLeft = SumTask(numbers.start until middle)
            val taskRight = SumTask(middle..numbers.endInclusive)

            taskLeft.fork()
            taskRight.fork()
            taskLeft.join() + taskRight.join()
        }
    }
}
