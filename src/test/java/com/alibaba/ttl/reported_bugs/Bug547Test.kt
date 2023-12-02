package com.alibaba.ttl.reported_bugs

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Bug URL: https://github.com/alibaba/transmittable-thread-local/issues/547
 * Reporter: @robin-g-20230331
 */
class Bug547Test : AnnotationSpec() {
    private val scheduledThreadPoolExecutor = ScheduledThreadPoolExecutor(2).apply {
        removeOnCancelPolicy = true
    }

    @Test
    fun test_bug547() {
        scheduledThreadPoolExecutor.queue.size shouldBe 0

        val future = scheduledThreadPoolExecutor.schedule({}, 1, TimeUnit.DAYS)
        scheduledThreadPoolExecutor.queue.size shouldBe 1

        future.cancel(false)

        scheduledThreadPoolExecutor.queue.size shouldBe 0
    }

    @AfterAll
    fun afterAll() {
        scheduledThreadPoolExecutor.shutdown()
    }
}
