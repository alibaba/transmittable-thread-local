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
    fun `runnable wrap extension function `() {
        val task = Task("1")
        val ttlRunnable = task.wrap()
        Assert.assertSame(task, ttlRunnable.runnable)
    }

    @Test
    fun `runnable wrap extension function multiple times`() {
        val task = Task("1").wrap()
        try {
            task.wrap()
            Assert.fail()
        } catch (e: IllegalStateException) {
            Assert.assertThat<String>(e.message, CoreMatchers.containsString("Already TtlRunnable"))
        }

    }

    @Test
    fun `list of runnable wrap extension function`() {
        val taskList = listOf(Task("1"), Task("2"), Task("3")).wrap()

        Assert.assertEquals(3, taskList.size)
        taskList.forEach {
            Assert.assertThat(it, CoreMatchers.instanceOf(TtlRunnable::class.java))
        }
    }
}