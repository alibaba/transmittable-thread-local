package com.alibaba.demo.forkjoinpool

import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RecursiveTask
import java.util.concurrent.TimeUnit

import org.junit.Assert.fail

/**
 * ForkJoinPool use demo.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
fun main(args: Array<String>) {
    val pool = ForkJoinPool()

    val result = pool.invoke(SumTask(1L..100000L))

    println(result) // result is 5000050000

    pool.shutdown()
    if (!pool.awaitTermination(100, TimeUnit.MILLISECONDS)) fail("Fail to shutdown thread pool")
}

internal class SumTask(private val numbers: LongRange) : RecursiveTask<Long>() {
    override fun compute(): Long? =
            if (numbers.count() <= 16) {
                // compute directly
                numbers.asSequence().sum()
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
