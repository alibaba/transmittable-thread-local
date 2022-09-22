package com.alibaba

import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeTrue
import java.time.Duration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


/**
 * Expand thread pool, to pre-create and cache threads.
 */
fun expandThreadPool(executor: ExecutorService) {
    val cpuCountX2 = Runtime.getRuntime().availableProcessors() * 2

    val count = if (executor is ThreadPoolExecutor) {
        (executor.maximumPoolSize * 2).coerceAtMost(cpuCountX2)
    } else cpuCountX2

    (0 until count).map {
        executor.submit { Thread.sleep(10) }
    }.forEach { it.getForTest() }
}

////////////////////////////////////////////////////////////////////////////////
// shutdown/await util methods for test
////////////////////////////////////////////////////////////////////////////////

private val timeout = Duration.ofSeconds(3)

fun ExecutorService.shutdownForTest() {
    shutdown()
    withClue("Fail to shutdown thread pool") {
        awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS).shouldBeTrue()
    }
}

fun <T> Future<T>.getForTest(): T = this.get(timeout.toMillis(), TimeUnit.MILLISECONDS)
