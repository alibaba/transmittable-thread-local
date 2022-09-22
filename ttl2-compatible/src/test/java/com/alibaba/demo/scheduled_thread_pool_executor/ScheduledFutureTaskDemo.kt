package com.alibaba.demo.scheduled_thread_pool_executor

import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * ScheduledThreadPoolExecutor usage demo for Issue 148
 * https://github.com/alibaba/transmittable-thread-local/issues/148
 */
fun main() {
    val scheduledThreadPoolExecutor = ScheduledThreadPoolExecutor(10)

    val task = Runnable { println("I'm a Runnable task, I'm working...") }
    val scheduledFuture = scheduledThreadPoolExecutor.scheduleWithFixedDelay(task, 500, 500, TimeUnit.MILLISECONDS)

    Thread.sleep(2_000)

    println("cancel")
    val cancelResult = scheduledFuture.cancel(false)
    println("canceled: $cancelResult")  // scheduled task cancel success!

    Thread.sleep(2_000)
    scheduledThreadPoolExecutor.shutdown()
    println("Bye")
}
