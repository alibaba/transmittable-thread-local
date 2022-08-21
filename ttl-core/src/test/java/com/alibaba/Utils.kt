package com.alibaba

import java.lang.Thread.sleep
import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadPoolExecutor

/**
 * Expand thread pool, to pre-create and cache threads.
 */
fun expandThreadPool(executor: ExecutorService) {
    val cpuCountX2 = Runtime.getRuntime().availableProcessors() * 2

    val count = if (executor is ThreadPoolExecutor) {
        (executor.maximumPoolSize * 2).coerceAtMost(cpuCountX2)
    } else cpuCountX2

    (0 until count).map {
        executor.submit { sleep(10) }
    }.forEach { it.get() }
}
