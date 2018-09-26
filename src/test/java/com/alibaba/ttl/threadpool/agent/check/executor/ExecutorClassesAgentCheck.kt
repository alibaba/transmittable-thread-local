@file:JvmName("ExecutorClassesAgentCheck")

package com.alibaba.ttl.threadpool.agent.check.executor


import com.alibaba.*
import com.alibaba.ttl.testmodel.Task
import org.junit.Assert.*
import java.util.concurrent.*

private const val POOL_SIZE = 3

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @author wuwen5 (wuwen.55 at aliyun dot com)
 * @see com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.TtlExecutorTransformlet
 */
fun main(args: Array<String>) {
    val threadFactory = ThreadFactory { Thread(it).apply { isDaemon = true } }

    val executorService = ThreadPoolExecutor(POOL_SIZE, POOL_SIZE,
            10L, TimeUnit.SECONDS,
            LinkedBlockingQueue(), threadFactory)

    val scheduledExecutorService = ScheduledThreadPoolExecutor(POOL_SIZE, threadFactory)

    expandThreadPool(executorService)
    expandThreadPool(scheduledExecutorService)


    checkExecutorService(executorService)
    checkThreadPoolExecutorForRemoveMethod(executorService)
    checkScheduledExecutorService(scheduledExecutorService)


    executorService.shutdown()
    scheduledExecutorService.shutdown()
    if (!executorService.awaitTermination(100, TimeUnit.MILLISECONDS)) fail("Fail to shutdown thread pool")
    if (!scheduledExecutorService.awaitTermination(100, TimeUnit.MILLISECONDS))
        fail("Fail to shutdown thread pool")


    printHead("ExecutorClassesAgentCheck OK!")
}

private fun checkExecutorService(executorService: ExecutorService) {
    printHead("checkExecutorService")
    val ttlInstances = createParentTtlInstances(ConcurrentHashMap())

    val tag = "1"
    val task = Task(tag, ttlInstances)
    val future = executorService.submit(task)

    // create after new Task, won't see parent value in in task!
    createParentTtlInstancesAfterCreateChild(ttlInstances)


    future.get(100, TimeUnit.MILLISECONDS)


    // child Inheritable
    assertChildTtlValues(tag, task.copied)
    // child do not effect parent
    assertParentTtlValues(copyTtlValues(ttlInstances))
}

private fun checkThreadPoolExecutorForRemoveMethod(executor: ThreadPoolExecutor) {
    printHead("checkThreadPoolExecutorForRemoveMethod")

    val futures = (0 until POOL_SIZE * 2).map {
        executor.submit { Thread.sleep(10) }
    }

    Runnable {
        println("Task should be removed!")
    }.let {
        executor.execute(it)
        // Does ThreadPoolExecutor#remove method take effect?
        assertTrue(executor.remove(it))
        assertFalse(executor.remove(it))
    }

    // wait sleep task finished.
    futures.forEach { it.get(100, TimeUnit.MILLISECONDS) }
}

private fun checkScheduledExecutorService(scheduledExecutorService: ScheduledExecutorService) {
    printHead("checkScheduledExecutorService")
    val ttlInstances = createParentTtlInstances(ConcurrentHashMap())

    val tag = "2"
    val task = Task(tag, ttlInstances)
    val future = scheduledExecutorService.schedule(task, 100, TimeUnit.MILLISECONDS)

    // create after new Task, won't see parent value in in task!
    createParentTtlInstancesAfterCreateChild(ttlInstances)


    future.get(200, TimeUnit.MILLISECONDS)


    // child Inheritable
    assertChildTtlValues(tag, task.copied)
    // child do not effect parent
    assertParentTtlValues(copyTtlValues(ttlInstances))
}
