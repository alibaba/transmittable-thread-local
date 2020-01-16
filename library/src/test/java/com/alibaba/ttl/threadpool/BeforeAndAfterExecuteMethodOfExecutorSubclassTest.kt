package com.alibaba.ttl.threadpool

import com.alibaba.support.junit.conditional.ConditionalIgnoreRule
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule.ConditionalIgnore
import com.alibaba.support.junit.conditional.IsAgentRun
import com.alibaba.support.junit.conditional.NoAgentRun
import com.alibaba.ttl.TtlRunnable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class MyThreadPoolExecutor : ThreadPoolExecutor(10, 20, 2, TimeUnit.SECONDS, LinkedBlockingQueue()) {
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

    @Test
    @ConditionalIgnore(condition = IsAgentRun::class)
    fun noAgent() {
        val myThreadPoolExecutor = MyThreadPoolExecutor()

        val ttlExecutorService = myThreadPoolExecutor.let {
            it.setKeepAliveTime(10, TimeUnit.SECONDS)
            TtlExecutors.getTtlExecutorService(it)
        }!!

        (0 until 10).map {
            ttlExecutorService.execute(MyRunnable())
        }

        Thread.sleep(100)

        assertEquals(20, myThreadPoolExecutor.runnableList.size)
        assertTrue(myThreadPoolExecutor.runnableList.all { it is TtlRunnable })
    }

    @Rule
    @JvmField
    val rule = ConditionalIgnoreRule()
}
