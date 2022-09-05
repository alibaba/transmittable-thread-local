package com.alibaba.ttl3

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldBeTypeOf
import java.util.*

@Suppress("DEPRECATION")
class TtlTimerTaskTest : AnnotationSpec() {
    @Test
    fun test_get() {
        TtlTimerTask.get(null).shouldBeNull()

        val timerTask = object : TimerTask() {
            override fun run() {}
        }

        val ttlTimerTask = TtlTimerTask.get(timerTask)
        ttlTimerTask.shouldBeTypeOf<TtlTimerTask>()


        shouldThrow<IllegalStateException> {
            TtlTimerTask.get(ttlTimerTask)
        }.message shouldBe "Already TtlTimerTask!"
    }

    @Test
    fun test_unwrap() {
        TtlTimerTask.unwrap(null).shouldBeNull()

        val timerTask = object : TimerTask() {
            override fun run() {}
        }

        val ttlTimerTask = TtlTimerTask.get(timerTask)
        TtlTimerTask.unwrap(timerTask) shouldBeSameInstanceAs timerTask
        TtlTimerTask.unwrap(ttlTimerTask) shouldBeSameInstanceAs timerTask
    }
}
