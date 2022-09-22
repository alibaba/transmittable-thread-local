package com.alibaba.ttl

import io.kotest.core.spec.style.AnnotationSpec
import org.junit.Assert.*
import java.util.*

@Suppress("DEPRECATION")
class TtlTimerTaskTest : AnnotationSpec() {
    @Test
    fun test_get() {
        assertNull(TtlTimerTask.get(null))

        val timerTask = object : TimerTask() {
            override fun run() {}
        }

        val ttlTimerTask = TtlTimerTask.get(timerTask)
        assertTrue(ttlTimerTask is TtlTimerTask)
    }

    @Test
    fun test_unwrap() {
        assertNull(TtlTimerTask.unwrap(null))

        val timerTask = object : TimerTask() {
            override fun run() {}
        }
        val ttlTimerTask = TtlTimerTask.get(timerTask)


        assertSame(timerTask, TtlTimerTask.unwrap(timerTask))
        assertSame(timerTask, TtlTimerTask.unwrap(ttlTimerTask))


        assertEquals(listOf(timerTask), TtlTimerTask.unwraps(listOf(timerTask)))
        assertEquals(listOf(timerTask), TtlTimerTask.unwraps(listOf(ttlTimerTask)))
        assertEquals(listOf(timerTask, timerTask), TtlTimerTask.unwraps(listOf(ttlTimerTask, timerTask)))
        assertEquals(listOf<TimerTask>(), TtlTimerTask.unwraps(null))
    }
}
