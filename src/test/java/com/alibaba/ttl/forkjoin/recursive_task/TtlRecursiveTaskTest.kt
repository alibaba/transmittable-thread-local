package com.alibaba.ttl.forkjoin.recursive_task

import com.alibaba.support.junit.conditional.ConditionalIgnoreRule
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule.ConditionalIgnore
import com.alibaba.support.junit.conditional.BelowJava7
import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.ttl.TtlRecursiveTask
import com.alibaba.utils.Utils
import org.junit.AfterClass
import org.junit.Test

import java.util.concurrent.ConcurrentMap
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit

import com.alibaba.utils.Utils.*
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Rule

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
        @Suppress("unused")
        @AfterClass
        fun afterClass() {
            pool.shutdown()
            if (!pool.awaitTermination(100, TimeUnit.MILLISECONDS)) fail("Fail to shutdown thread pool")

            singleThreadPool.shutdown()
            if (!singleThreadPool.awaitTermination(100, TimeUnit.MILLISECONDS)) fail("Fail to shutdown thread pool")
        }

    }
}

private fun run_test_with_pool(forkJoinPool: ForkJoinPool) {
    val ttlInstances = createTestTtlValue()

    val numbers = 0..42
    val sumTask = SumTask(numbers, ttlInstances)

    val after = TransmittableThreadLocal<String>()
    after.set(PARENT_AFTER_CREATE_TTL_TASK)
    ttlInstances[PARENT_AFTER_CREATE_TTL_TASK] = after

    val future = forkJoinPool.submit(sumTask)
    assertEquals(numbers.sum(), future.get())

    // child Inheritable
    assertTtlInstances(sumTask.copied,
            PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
            PARENT_MODIFIED_IN_CHILD /* Not change*/, PARENT_MODIFIED_IN_CHILD
    )

    // left grand Task Inheritable, changed value
    assertTtlInstances(sumTask.leftSubTask.copied,
            PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
            PARENT_MODIFIED_IN_CHILD + SumTask.CHANGE_POSTFIX /* CHANGED */, PARENT_MODIFIED_IN_CHILD
    )

    // right grand Task Inheritable, not change value
    assertTtlInstances(sumTask.rightSubTask.copied,
            PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
            PARENT_MODIFIED_IN_CHILD /* Not change*/, PARENT_MODIFIED_IN_CHILD
    )

    // child do not effect parent
    assertTtlInstances(captured(ttlInstances),
            PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
            PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD,
            PARENT_AFTER_CREATE_TTL_TASK, PARENT_AFTER_CREATE_TTL_TASK
    )
}


/**
 * A test demo class
 *
 * @author LNAmp
 * @see com.alibaba.ttl.TtlRecursiveTask
 */
internal class SumTask(private val numbers: IntRange,
                       private val ttlMap: ConcurrentMap<String, TransmittableThreadLocal<String>>, private val changeTtlValue: Boolean = false) : TtlRecursiveTask<Int>() {

    @Volatile
    lateinit var copied: Map<String, Any>
    @Volatile
    lateinit var leftSubTask: SumTask
    @Volatile
    lateinit var rightSubTask: SumTask

    override fun compute(): Int? {
        if (changeTtlValue) {
            Utils.modifyValuesExistInTtlInstances(CHANGE_POSTFIX, ttlMap)
        }

        try {
            return if (numbers.count() <= 5) {
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
            this.copied = Utils.captured(this.ttlMap)
        }
    }

    companion object {
        const val CHANGE_POSTFIX = " + 1"
    }
}
