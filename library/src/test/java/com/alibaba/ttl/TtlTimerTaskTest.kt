package com.alibaba.ttl

import org.junit.Assert.*
import org.junit.Test
import java.util.*

class TtlTimerTaskTest {
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
