@file:JvmName("TimerAgentCheck")

package com.alibaba.ttl.threadpool.agent.check.timer

import com.alibaba.*
import com.alibaba.ttl.testmodel.Task
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.TtlTimerTaskTransformlet
 */
fun main() {
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
    // child do not effect parent
    assertParentTtlValues(copyTtlValues(ttlInstances))

    printHead("TimerAgentCheck OK!")
}

