package com.alibaba.ttl.reported_bugs

import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.ttl.TtlRunnable
import org.junit.Test

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask
import java.util.concurrent.atomic.AtomicReference

import org.junit.Assert.assertEquals

/**
 * Bug URL: https://github.com/alibaba/transmittable-thread-local/issues/70
 * Reporter: @aftersss
 */
class Bug70_Test {

    @Test
    fun test_bug70() {
        val hello = "hello"

        val executorService = Executors.newSingleThreadExecutor()
        val threadLocal = TransmittableThreadLocal<String>()

        threadLocal.set(hello)
        assertEquals(hello, threadLocal.get())

        val task1 = FutureTask<String> { threadLocal.get() }
        executorService.submit(TtlRunnable.get(task1))
                .get()
        assertEquals(hello, task1.get())

        val taskRef = AtomicReference<FutureTask<String>>()
        val thread = Thread {
            val task2 = FutureTask<String> { threadLocal.get() }
            val runnable = TtlRunnable.get(task2, false, false)
            executorService.submit(runnable)
            taskRef.set(task2)
        }
        thread.start()
        thread.join()
        assertEquals(hello, taskRef.get().get())
    }
}
