package com.alibaba.ttl.forkjoin.recursive_task

import com.alibaba.*
import com.alibaba.support.junit.conditional.BelowJava7
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule.ConditionalIgnore
import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.ttl.TtlRecursiveTask
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit

private val pool = ForkJoinPool()
private val singleThreadPool = ForkJoinPool(1)

/**
 * @author LNAmp
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class TtlRecursiveTaskTest {
    @Rule
    @JvmField
    val rule = ConditionalIgnoreRule()

    @Test
    @ConditionalIgnore(condition = BelowJava7::class)
    fun test_TtlRecursiveTask_asyncWith_ForkJoinPool() {
        run_test_with_pool(pool)
    }

    @Test
    @ConditionalIgnore(condition = BelowJava7::class)
    fun test_TtlRecursiveTask_asyncWith_SingleThreadForkJoinPool() {
        run_test_with_pool(singleThreadPool)
    }

    companion object {
        @AfterClass
        @Suppress("unused")
        fun afterClass() {
            pool.shutdown()
            if (!pool.awaitTermination(100, TimeUnit.MILLISECONDS)) fail("Fail to shutdown thread pool")

            singleThreadPool.shutdown()
            if (!singleThreadPool.awaitTermination(100, TimeUnit.MILLISECONDS)) fail("Fail to shutdown thread pool")
        }

    }
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
    assertTtlValues(
            mapOf(PARENT_CREATE_UNMODIFIED_IN_CHILD to PARENT_CREATE_UNMODIFIED_IN_CHILD,
                    PARENT_CREATE_MODIFIED_IN_CHILD to PARENT_CREATE_MODIFIED_IN_CHILD /* Not change*/),
            sumTask.copied
    )

    // left grand Task Inheritable, changed value
    assertTtlValues(
            mapOf(PARENT_CREATE_UNMODIFIED_IN_CHILD to PARENT_CREATE_UNMODIFIED_IN_CHILD,
                    PARENT_CREATE_MODIFIED_IN_CHILD to PARENT_CREATE_MODIFIED_IN_CHILD + SumTask.CHANGE_POSTFIX /* CHANGED */),
            sumTask.leftSubTask.copied
    )

    // right grand Task Inheritable, not change value
    assertTtlValues(
            mapOf(PARENT_CREATE_UNMODIFIED_IN_CHILD to PARENT_CREATE_UNMODIFIED_IN_CHILD,
                    PARENT_CREATE_MODIFIED_IN_CHILD to PARENT_CREATE_MODIFIED_IN_CHILD /* Not change*/),
            sumTask.rightSubTask.copied
    )

    // child do not effect parent
    assertTtlValues(
            mapOf(PARENT_CREATE_UNMODIFIED_IN_CHILD to PARENT_CREATE_UNMODIFIED_IN_CHILD,
                    PARENT_CREATE_MODIFIED_IN_CHILD to PARENT_CREATE_MODIFIED_IN_CHILD,
                    PARENT_CREATE_AFTER_CREATE_CHILD to PARENT_CREATE_AFTER_CREATE_CHILD),
            copyTtlValues(ttlInstances)
    )
}


/**
 * A test demo class
 *
 * @author LNAmp
 * @see com.alibaba.ttl.TtlRecursiveTask
 */
private class SumTask(private val numbers: IntRange,
                      private val ttlMap: ConcurrentMap<String, TransmittableThreadLocal<String>>, private val changeTtlValue: Boolean = false) : TtlRecursiveTask<Int>() {

    lateinit var copied: Map<String, Any>
    lateinit var leftSubTask: SumTask
    lateinit var rightSubTask: SumTask

    override fun compute(): Int {
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
