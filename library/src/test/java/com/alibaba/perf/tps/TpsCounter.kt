package com.alibaba.perf.tps

import org.junit.Assert.fail
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class TpsCounter internal constructor(private val threadCount: Int) {
    private val executorService: ExecutorService = Executors.newFixedThreadPool(threadCount)

    private val counter = AtomicLong()

    @Volatile
    private var stopped = false

    val count: Long
        get() = counter.get()

    internal fun setAction(runnable: Runnable) {
        val r = {
            while (!stopped) {
                runnable.run()
                counter.incrementAndGet()
            }
        }
        for (i in 0 until threadCount) {
            executorService.execute(r)
        }
    }

    fun stop() {
        stopped = true

        executorService.shutdown()
        executorService.awaitTermination(100, TimeUnit.MILLISECONDS)
        if (!executorService.isTerminated) fail("Fail to shutdown thread pool")
    }
}
