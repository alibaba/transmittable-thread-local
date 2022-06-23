package com.alibaba.it

import com.alibaba.*
import com.alibaba.ttl.testmodel.Task
import com.alibaba.ttl.threadpool.agent.TtlAgent
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.core.test.config.TestCaseConfig
import io.kotest.matchers.booleans.shouldBeTrue
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.TtlTimerTaskTransformlet
 */
class TimerAgentCheckTest : AnnotationSpec() {
    @Suppress("OVERRIDE_DEPRECATION")
    override fun defaultTestCaseConfig(): TestCaseConfig =
        TestCaseConfig(enabled = hasTtlAgentRunWithEnableTimerTask())

    @Test
    fun check() {
        val timer = Timer(true)

        printHead("TimerAgentCheck")

        val ttlInstances = createParentTtlInstances(ConcurrentHashMap())

        val tag = "1"
        val task = Task(tag, ttlInstances)

        val timerTask = object : TimerTask() {
            override fun run() {
                task.run()
            }
        }
        timer.schedule(timerTask, 0)

        // create after new Task, won't see parent value in in task!
        createParentTtlInstancesAfterCreateChild(ttlInstances)

        // child Inheritable
        assertChildTtlValues(tag, task.copied)
        // child do not affect parent
        assertParentTtlValues(copyTtlValues(ttlInstances))

        printHead("TimerAgentCheck OK!")
    }
}
