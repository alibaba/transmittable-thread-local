package com.alibaba.ttl.reported_bugs

import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.ttl.TtlRunnable
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

/**
 * Bug URL: https://github.com/alibaba/transmittable-thread-local/issues/70
 * Reporter: @aftersss
 */
class Bug70_Test {

    @Test
    fun test_bug70() {
        val hello = "hello"
        val executorService = Executors.newSingleThreadExecutor()
        val threadLocal = TransmittableThreadLocal<String>().apply { set(hello) }
        assertEquals(hello, threadLocal.get())

        val futureTask = FutureTask<String> { threadLocal.get() }.also {
            executorService.submit(TtlRunnable.get(it))
                    .get()
        }
        assertEquals(hello, futureTask.get())

        val taskRef = AtomicReference<FutureTask<String>>()
        thread(name = "the thread for run executor action") {
            FutureTask<String> { threadLocal.get() }.also {
                executorService.submit(TtlRunnable.get(it, false, false))
                taskRef.set(it)
            }
        }.join()
        assertEquals(hello, taskRef.get().get())
    }
}
