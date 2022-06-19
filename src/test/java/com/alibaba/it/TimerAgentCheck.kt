package com.alibaba.test.ttl.threadpool.agent.check.timer

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
    override fun defaultTestCaseConfig(): TestCaseConfig {
        val testEnableKey = "run-ttl-test-under-agent-with-enable-timer-task"
        if (System.getProperties().containsKey(testEnableKey)) {
            TtlAgent.isTtlAgentLoaded().shouldBeTrue()
            TtlAgent.isEnableTimerTask().shouldBeTrue()
        }

        return TestCaseConfig(enabled = TtlAgent.isTtlAgentLoaded() && TtlAgent.isEnableTimerTask())
    }

    @Test
    fun check() {
        val timer = Timer(true)

        printHead("TimerAgentCheck")

        val ttlInstances = createParentTtlInstances(ConcurrentHashMap())

        val tag = "1"
        val task = Task(tag, ttlInstances)

        val latch = CountDownLatch(1)
        val timerTask = object : TimerTask() {
            override fun run() {
                task.run()
                latch.countDown()
            }
        }
        timer.schedule(timerTask, 0)

        // create after new Task, won't see parent value in in task!
        createParentTtlInstancesAfterCreateChild(ttlInstances)

        latch.await(100, TimeUnit.MILLISECONDS)

        // child Inheritable
        assertChildTtlValues(tag, task.copied)
        // child do not affect parent
        assertParentTtlValues(copyTtlValues(ttlInstances))

        printHead("TimerAgentCheck OK!")
    }
}
