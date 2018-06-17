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
 * @see com.alibaba.ttl.threadpool.agent.TtlExecutorTransformer
 */
fun main(args: Array<String>) {
    val executorService = ThreadPoolExecutor(POOL_SIZE, POOL_SIZE,
            10L, TimeUnit.SECONDS,
            LinkedBlockingQueue())

    val scheduledExecutorService = ScheduledThreadPoolExecutor(POOL_SIZE)

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

    val sleepTasks: List<FutureTask<Any>> = (0 until POOL_SIZE * 2).map {
        val futureTask: FutureTask<Any> = FutureTask<Any>({
            Thread.sleep(100)
            println("Run sleep task!")
        }, null)
        executor.execute(futureTask)
        futureTask
    }.toList()

    val taskToRemove = FutureTask<Any>({ println("Run taskToRemove!") }, null)
    executor.execute(taskToRemove)
    executor.remove(taskToRemove)

    // wait sleep task finished.
    sleepTasks.forEach { it.get(300, TimeUnit.MILLISECONDS) }

    /////////////////////////////////////////////////////////////
    // Does ThreadPoolExecutor#remove method take effect?
    /////////////////////////////////////////////////////////////
    assertFalse(taskToRemove.isDone)
    assertFalse(taskToRemove.isCancelled) // task is directly removed from work queue, so not cancelled!

    println("executor.activeCount: ${executor.activeCount}")
    Thread.sleep(1)
    assertEquals(0L, executor.activeCount.toLong()) // No more task in executor, so remove op is success!

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
