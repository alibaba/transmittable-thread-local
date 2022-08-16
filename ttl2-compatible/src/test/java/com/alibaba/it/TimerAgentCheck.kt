package com.alibaba.it

import com.alibaba.*
import com.alibaba.ttl.testmodel.Task
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestScope
import io.kotest.engine.test.logging.info
import java.util.*
import java.util.concurrent.ConcurrentHashMap


/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.TtlTimerTaskTransformlet
 */
@ExperimentalKotest
class TimerAgentCheckTest : FunSpec({
    fun TestScope.printHead(title: String) {
        info { "======================================\n$title\n======================================" }
    }

    test("check").config(enabled = hasTtlAgentRunWithEnableTimerTask()) {
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
})
