package com.alibaba.ttl

import com.alibaba.support.junit.conditional.ConditionalIgnoreRule
import com.alibaba.ttl.testmodel.Task
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class TtlRunnableExtKtTest {

    @Rule
    @JvmField
    val rule = ConditionalIgnoreRule()

    @Test
    fun `runnable wrapTtl extension function `() {
        val task = Task("1")
        val ttlRunnable = task.wrapTtl()
        Assert.assertSame(task, ttlRunnable.runnable)
    }

    @Test
    fun `runnable wrapTtl extension function multiple times`() {
        val task = Task("1").wrapTtl()
        try {
            task.wrapTtl()
            Assert.fail()
        } catch (e: IllegalStateException) {
            Assert.assertThat<String>(e.message, CoreMatchers.containsString("Already TtlRunnable"))
        }

    }

    @Test
    fun `list of runnable wrapTtl extension function`() {
        val taskList = listOf(Task("1"), Task("2"), Task("3")).wrapTtl()

        Assert.assertEquals(3, taskList.size)
        taskList.forEach {
            Assert.assertThat(it, CoreMatchers.instanceOf(TtlRunnable::class.java))
        }
    }
}