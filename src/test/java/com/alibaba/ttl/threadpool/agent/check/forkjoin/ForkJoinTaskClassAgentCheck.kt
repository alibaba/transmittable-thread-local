@file:JvmName("ForkJoinTaskClassAgentCheck")

package com.alibaba.ttl.threadpool.agent.check.forkjoin

import com.alibaba.*
import com.alibaba.ttl.TransmittableThreadLocal
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RecursiveTask
import java.util.concurrent.TimeUnit


private val pool = ForkJoinPool()
private val singleThreadPool = ForkJoinPool(1)

/**
 * !! Quick and dirty: copy code from [com.alibaba.ttl.forkjoin.recursive_task.TtlRecursiveTaskTest] !!
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @author wuwen5 (wuwen.55 at aliyun dot com)
 * @see com.alibaba.ttl.threadpool.agent.TtlForkJoinTransformer
 */
fun main(args: Array<String>) {

    check_TtlRecursiveTask_asyncWith_ForkJoinPool()
    check_TtlRecursiveTask_asyncWith_SingleThreadForkJoinPool()


    pool.shutdown()
    if (!pool.awaitTermination(100, TimeUnit.MILLISECONDS)) fail("Fail to shutdown thread pool")

    singleThreadPool.shutdown()
    if (!singleThreadPool.awaitTermination(100, TimeUnit.MILLISECONDS)) fail("Fail to shutdown thread pool")

    printHead("ForkJoinTaskClassAgentCheck OK!")
}


private fun check_TtlRecursiveTask_asyncWith_ForkJoinPool() {
    printHead("check_TtlRecursiveTask_asyncWith_ForkJoinPool")
    run_test_with_pool(pool)
}

private fun check_TtlRecursiveTask_asyncWith_SingleThreadForkJoinPool() {
    printHead("check_TtlRecursiveTask_asyncWith_SingleThreadForkJoinPool")
    run_test_with_pool(singleThreadPool)
}

private fun run_test_with_pool(forkJoinPool: ForkJoinPool) {
    val ttlInstances = createParentTtlInstances()

    val numbers = 0..42
    val sumTask = SumTask(numbers, ttlInstances)

    // create after new Task, won't see parent value in in task!
    createParentTtlInstancesAfterCreateChild(ttlInstances)


    val future = forkJoinPool.submit(sumTask)
    assertEquals(numbers.sum(), future.get())


    // child Inheritable
    assertTtlValues(sumTask.copied,
            PARENT_CREATE_UNMODIFIED_IN_CHILD, PARENT_CREATE_UNMODIFIED_IN_CHILD,
            PARENT_CREATE_MODIFIED_IN_CHILD /* Not change*/, PARENT_CREATE_MODIFIED_IN_CHILD
    )

    // left grand Task Inheritable, changed value
    assertTtlValues(sumTask.leftSubTask.copied,
            PARENT_CREATE_UNMODIFIED_IN_CHILD, PARENT_CREATE_UNMODIFIED_IN_CHILD,
            PARENT_CREATE_MODIFIED_IN_CHILD + SumTask.CHANGE_POSTFIX /* CHANGED */, PARENT_CREATE_MODIFIED_IN_CHILD
    )

    // right grand Task Inheritable, not change value
    assertTtlValues(sumTask.rightSubTask.copied,
            PARENT_CREATE_UNMODIFIED_IN_CHILD, PARENT_CREATE_UNMODIFIED_IN_CHILD,
            PARENT_CREATE_MODIFIED_IN_CHILD /* Not change*/, PARENT_CREATE_MODIFIED_IN_CHILD
    )

    // child do not effect parent
    assertTtlValues(copyTtlValues(ttlInstances),
            PARENT_CREATE_UNMODIFIED_IN_CHILD, PARENT_CREATE_UNMODIFIED_IN_CHILD,
            PARENT_CREATE_MODIFIED_IN_CHILD, PARENT_CREATE_MODIFIED_IN_CHILD,
            PARENT_CREATE_AFTER_CREATE_CHILD, PARENT_CREATE_AFTER_CREATE_CHILD
    )
}


/**
 * A test demo class
 *
 * @author LNAmp
 * @see com.alibaba.ttl.TtlRecursiveTask
 */
private class SumTask(private val numbers: IntRange,
                      private val ttlMap: ConcurrentMap<String, TransmittableThreadLocal<String>>,
                      private val changeTtlValue: Boolean = false) : RecursiveTask<Int>() {

    lateinit var copied: Map<String, Any>
    lateinit var leftSubTask: SumTask
    lateinit var rightSubTask: SumTask

    override fun compute(): Int? {
        if (changeTtlValue) {
            modifyParentTtlInstances(CHANGE_POSTFIX, ttlMap)
        }

        try {
            return if (numbers.count() <= 10) {
                numbers.sum()
            } else {
                val mid = numbers.start + numbers.count() / 2

                // left -> change! right -> not change.
                val left = SumTask(numbers.start until mid, ttlMap, true)
                val right = SumTask(mid..numbers.endInclusive, ttlMap, false)
                this.leftSubTask = left
                this.rightSubTask = right

                left.fork()
                right.fork()
                left.join() + right.join()
            }
        } finally {
            this.copied = copyTtlValues(this.ttlMap)
        }
    }

    companion object {
        const val CHANGE_POSTFIX = " + 1"
    }
}
