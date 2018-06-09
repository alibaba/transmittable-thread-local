package com.alibaba.ttl.forkjoin.recursive_action

import com.alibaba.support.junit.conditional.ConditionalIgnoreRule
import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.ttl.TtlRecursiveAction
import com.alibaba.utils.Utils
import org.junit.AfterClass
import org.junit.Test

import java.util.concurrent.ConcurrentMap
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit

import com.alibaba.utils.Utils.*
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule.ConditionalIgnore
import com.alibaba.support.junit.conditional.BelowJava7
import org.junit.Assert.fail
import org.junit.Rule


private val pool = ForkJoinPool()
private val singleThreadPool = ForkJoinPool(1)

/**
 * TtlRecursiveAction test class
 *
 * @author LNAmp
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class TtlRecursiveActionTest {
    @Rule
    @JvmField
    val rule = ConditionalIgnoreRule()

    @Test
    @ConditionalIgnore(condition = BelowJava7::class)
    fun test_TtlRecursiveTask_asyncWithForkJoinPool() {
        run_test_with_pool(pool)
    }

    @Test
    @ConditionalIgnore(condition = BelowJava7::class)
    fun test_TtlRecursiveTask_asyncWithSingleThreadForkJoinPool_changeValue() {
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

    val printAction = PrintAction(1..42, ttlInstances)

    val after = TransmittableThreadLocal<String>()
    after.set(PARENT_AFTER_CREATE_TTL_TASK)
    ttlInstances[PARENT_AFTER_CREATE_TTL_TASK] = after

    val future = forkJoinPool.submit(printAction)
    future.get()

    // child Inheritable
    assertTtlInstances(printAction.copied,
            PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
            PARENT_MODIFIED_IN_CHILD /* Not change*/, PARENT_MODIFIED_IN_CHILD
    )

    // left grand Task Inheritable, changed value
    assertTtlInstances(printAction.leftSubAction.copied,
            PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
            PARENT_MODIFIED_IN_CHILD + PrintAction.CHANGE_POSTFIX /* CHANGED */, PARENT_MODIFIED_IN_CHILD
    )

    // right grand Task Inheritable, not change value
    assertTtlInstances(printAction.rightSubAction.copied,
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
 */
internal class PrintAction(private val numbers: IntRange,
                           private val ttlMap: ConcurrentMap<String, TransmittableThreadLocal<String>>, private val changeTtlValue: Boolean = false) : TtlRecursiveAction() {

    @Volatile
    lateinit var copied: Map<String, Any>
    @Volatile
    lateinit var leftSubAction: PrintAction
    @Volatile
    lateinit var rightSubAction: PrintAction

    override fun compute() {
        if (changeTtlValue) {
            Utils.modifyValuesExistInTtlInstances(CHANGE_POSTFIX, ttlMap)
        }

        try {


            if (numbers.count() <= 5) {
                println("numbers: $numbers")

            } else {
                val mid = numbers.start + numbers.count() / 2

                // left -> change! right -> not change.
                val left = PrintAction(numbers.start until mid, ttlMap, true)
                val right = PrintAction(mid..numbers.endInclusive, ttlMap, false)
                leftSubAction = left
                rightSubAction = right

                left.fork()
                right.fork()
                left.join()
                right.join()
            }
        } finally {
            this.copied = Utils.captured(this.ttlMap)
        }
    }

    companion object {
        const val CHANGE_POSTFIX = " + 1"
    }
}
