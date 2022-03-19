@file:Suppress("PackageDirectoryMismatch")

// Change the package out of com.alibaba.ttl
// so agent will transform MyThreadPoolExecutor
package com.alibaba.test.ttl.threadpool

import com.alibaba.support.junit.conditional.ConditionalIgnoreRule
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule.ConditionalIgnore
import com.alibaba.support.junit.conditional.IsAgentRun
import com.alibaba.support.junit.conditional.NoAgentRun
import com.alibaba.ttl.TtlRunnable
import com.alibaba.ttl.threadpool.TtlExecutors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class MyThreadPoolExecutor : ThreadPoolExecutor(10, 20, 10, TimeUnit.SECONDS, LinkedBlockingQueue()) {
    val runnableList = CopyOnWriteArrayList<Runnable>()

    override fun afterExecute(r: Runnable, t: Throwable?) {
        runnableList.add(r)
        super.afterExecute(r, t)
    }

    override fun beforeExecute(t: Thread, r: Runnable) {
        runnableList.add(r)
        super.beforeExecute(t, r)
    }
}

class MyRunnable : Runnable {
    override fun run() {
        Thread.sleep(1)
    }
}

class BeforeAndAfterExecuteMethodOfExecutorSubclassTest {
    @Test
    @ConditionalIgnore(condition = NoAgentRun::class)
    fun underAgent() {
        val myThreadPoolExecutor = MyThreadPoolExecutor()

        (0 until 10).map {
            myThreadPoolExecutor.execute(MyRunnable())
        }

        Thread.sleep(100)

        assertEquals(20, myThreadPoolExecutor.runnableList.size)
        assertTrue(myThreadPoolExecutor.runnableList.all { it is MyRunnable })
    }

    /**
     * for bug submitted by
     * https://github.com/alibaba/transmittable-thread-local/issues/133#issuecomment-1068793261
     */
    @Test
    @ConditionalIgnore(condition = NoAgentRun::class)
    fun underAgent_task_is_explicit_TtlRunnable__should_not_be_unwrapped() {
        val myThreadPoolExecutor = MyThreadPoolExecutor()

        (0 until 10).map {
            val r = TtlRunnable.get(MyRunnable())!!
            myThreadPoolExecutor.execute(r)
        }

        Thread.sleep(100)

        assertEquals(20, myThreadPoolExecutor.runnableList.size)
        assertTrue(myThreadPoolExecutor.runnableList.all { it is TtlRunnable })
    }

    @Test
    @ConditionalIgnore(condition = IsAgentRun::class)
    fun noAgent_task_is_TtlRunnable() {
        val myThreadPoolExecutor = MyThreadPoolExecutor()
        val ttlExecutorService = TtlExecutors.getTtlExecutorService(myThreadPoolExecutor)!!

        (0 until 10).map {
            ttlExecutorService.execute(MyRunnable())
        }

        Thread.sleep(100)

        assertEquals(20, myThreadPoolExecutor.runnableList.size)
        assertTrue(myThreadPoolExecutor.runnableList.all { it is TtlRunnable })
    }

    @Test
    @ConditionalIgnore(condition = IsAgentRun::class)
    fun noAgent_task_is_NOT_TtlRunnable() {
        val myThreadPoolExecutor = MyThreadPoolExecutor()

        (0 until 10).map {
            myThreadPoolExecutor.execute(MyRunnable())
        }

        Thread.sleep(100)

        assertEquals(20, myThreadPoolExecutor.runnableList.size)
        assertTrue(myThreadPoolExecutor.runnableList.all { it is MyRunnable })
    }

    @Rule
    @JvmField
    val rule = ConditionalIgnoreRule()
}
