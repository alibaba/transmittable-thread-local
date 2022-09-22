package com.alibaba.ttl.forkjoin.recursive_action

import com.alibaba.*
import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.ttl.TtlRecursiveAction
import io.kotest.core.spec.style.AnnotationSpec
import mu.KotlinLogging
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.ForkJoinPool


private val pool = ForkJoinPool()
private val singleThreadPool = ForkJoinPool(1)

/**
 * TtlRecursiveAction test class
 *
 * @author LNAmp
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class TtlRecursiveActionTest : AnnotationSpec() {
    @Test
    fun test_TtlRecursiveTask_asyncWithForkJoinPool() {
        run_test_with_pool(pool)
    }

    @Test
    fun test_TtlRecursiveTask_asyncWithSingleThreadForkJoinPool_changeValue() {
        run_test_with_pool(singleThreadPool)
    }
}

private fun run_test_with_pool(forkJoinPool: ForkJoinPool) {
    val ttlInstances = createParentTtlInstances()

    val printAction = PrintAction(1..42, ttlInstances)

    // create after new Task, won't see parent value in in task!
    createParentTtlInstancesAfterCreateChild(ttlInstances)


    val future = forkJoinPool.submit(printAction)
    future.get()


    // child Inheritable
    assertTtlValues(
        mapOf(
            PARENT_CREATE_UNMODIFIED_IN_CHILD to PARENT_CREATE_UNMODIFIED_IN_CHILD,
            PARENT_CREATE_MODIFIED_IN_CHILD to PARENT_CREATE_MODIFIED_IN_CHILD /* Not change*/
        ),
        printAction.copied
    )

    // left grand Task Inheritable, changed value
    assertTtlValues(
        mapOf(
            PARENT_CREATE_UNMODIFIED_IN_CHILD to PARENT_CREATE_UNMODIFIED_IN_CHILD,
            PARENT_CREATE_MODIFIED_IN_CHILD to PARENT_CREATE_MODIFIED_IN_CHILD + PrintAction.CHANGE_POSTFIX /* CHANGED */
        ),
        printAction.leftSubAction.copied
    )

    // right grand Task Inheritable, not change value
    assertTtlValues(
        mapOf(
            PARENT_CREATE_UNMODIFIED_IN_CHILD to PARENT_CREATE_UNMODIFIED_IN_CHILD,
            PARENT_CREATE_MODIFIED_IN_CHILD to PARENT_CREATE_MODIFIED_IN_CHILD /* Not change*/
        ),
        printAction.rightSubAction.copied
    )

    // child do not affect parent
    assertTtlValues(
        mapOf(
            PARENT_CREATE_UNMODIFIED_IN_CHILD to PARENT_CREATE_UNMODIFIED_IN_CHILD,
            PARENT_CREATE_MODIFIED_IN_CHILD to PARENT_CREATE_MODIFIED_IN_CHILD,
            PARENT_CREATE_AFTER_CREATE_CHILD to PARENT_CREATE_AFTER_CREATE_CHILD
        ),
        copyTtlValues(ttlInstances)
    )
}


/**
 * A test demo class
 *
 * @author LNAmp
 */
private class PrintAction(
    private val numbers: IntRange,
    private val ttlMap: ConcurrentMap<String, TransmittableThreadLocal<String>>,
    private val changeTtlValue: Boolean = false
) : TtlRecursiveAction() {
    private val logger = KotlinLogging.logger {}


    lateinit var copied: Map<String, Any>
    lateinit var leftSubAction: PrintAction
    lateinit var rightSubAction: PrintAction

    override fun compute() {
        if (changeTtlValue) {
            modifyParentTtlInstances(CHANGE_POSTFIX, ttlMap)
        }

        try {
            if (numbers.count() <= 10) {
                logger.info { "print numbers: $numbers" }
            } else {
                val mid = numbers.first + numbers.count() / 2

                // left -> change! right -> not change.
                val left = PrintAction(numbers.first until mid, ttlMap, true)
                val right = PrintAction(mid..numbers.last, ttlMap, false)
                leftSubAction = left
                rightSubAction = right

                left.fork()
                right.fork()
                left.join()
                right.join()
            }
        } finally {
            this.copied = copyTtlValues(this.ttlMap)
        }
    }

    companion object {
        const val CHANGE_POSTFIX = " + 1"
    }
}
