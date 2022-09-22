package com.alibaba.ttl.forkjoin.recursive_task

import com.alibaba.*
import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.ttl.TtlRecursiveTask
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import java.util.concurrent.*

private val pool = ForkJoinPool()
private val singleThreadPool = ForkJoinPool(1)

/**
 * @author LNAmp
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class TtlRecursiveTaskTest : AnnotationSpec() {
    @Test
    fun test_TtlRecursiveTask_asyncWith_ForkJoinPool() {
        run_test_with_pool(pool)
    }

    @Test
    fun test_TtlRecursiveTask_asyncWith_SingleThreadForkJoinPool() {
        run_test_with_pool(singleThreadPool)
    }
}

private fun run_test_with_pool(forkJoinPool: ForkJoinPool) {
    val ttlInstances = createParentTtlInstances()

    val numbers = 0..42
    val sumTask: ForkJoinTask<Int> =
        if (noTtlAgentRun()) TtlSumTask(numbers, ttlInstances) else SumTask(numbers, ttlInstances)

    // create after new Task, won't see parent value in in task!
    createParentTtlInstancesAfterCreateChild(ttlInstances)


    val future = forkJoinPool.submit(sumTask)
    future.get() shouldBe numbers.sum()

    // child Inheritable
    assertTtlValues(
        mapOf(
            PARENT_CREATE_UNMODIFIED_IN_CHILD to PARENT_CREATE_UNMODIFIED_IN_CHILD,
            PARENT_CREATE_MODIFIED_IN_CHILD to PARENT_CREATE_MODIFIED_IN_CHILD /* Not change*/
        ),
        (sumTask as Getter).getcopied()
    )

    // left grand Task Inheritable, changed value
    assertTtlValues(
        mapOf(
            PARENT_CREATE_UNMODIFIED_IN_CHILD to PARENT_CREATE_UNMODIFIED_IN_CHILD,
            PARENT_CREATE_MODIFIED_IN_CHILD to PARENT_CREATE_MODIFIED_IN_CHILD + CHANGE_POSTFIX /* CHANGED */
        ),
        sumTask.getLeftSubTask().getcopied()
    )

    // right grand Task Inheritable, not change value
    assertTtlValues(
        mapOf(
            PARENT_CREATE_UNMODIFIED_IN_CHILD to PARENT_CREATE_UNMODIFIED_IN_CHILD,
            PARENT_CREATE_MODIFIED_IN_CHILD to PARENT_CREATE_MODIFIED_IN_CHILD /* Not change*/
        ),
        sumTask.getRightSubTask().getcopied()
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


private interface Getter {
    fun getcopied(): Map<String, Any>
    fun getLeftSubTask(): Getter
    fun getRightSubTask(): Getter
}

/**
 * A test demo class
 *
 * @author LNAmp
 * @see com.alibaba.ttl.TtlRecursiveTask
 */
private open class TtlSumTask(
    private val numbers: IntRange,
    private val ttlMap: ConcurrentMap<String, TransmittableThreadLocal<String>>,
    private val changeTtlValue: Boolean = false
) : TtlRecursiveTask<Int>(), Getter {

    lateinit var copied: Map<String, Any>
    lateinit var leftSubTask: TtlSumTask
    lateinit var rightSubTask: TtlSumTask

    override fun compute(): Int {
        if (changeTtlValue) {
            modifyParentTtlInstances(CHANGE_POSTFIX, ttlMap)
        }

        try {
            return if (numbers.count() <= 10) {
                numbers.sum()
            } else {
                val mid = numbers.first + numbers.count() / 2

                // left -> change! right -> not change.
                val left = TtlSumTask(numbers.first until mid, ttlMap, true)
                val right = TtlSumTask(mid..numbers.last, ttlMap, false)
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

    override fun getcopied(): Map<String, Any> = copied

    override fun getLeftSubTask(): Getter = leftSubTask

    override fun getRightSubTask(): Getter = rightSubTask
}


/**
 * A test demo class
 */
private class SumTask(
    private val numbers: IntRange,
    private val ttlMap: ConcurrentMap<String, TransmittableThreadLocal<String>>,
    private val changeTtlValue: Boolean = false
) : RecursiveTask<Int>(), Getter {

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
                val mid = numbers.first + numbers.count() / 2

                // left -> change! right -> not change.
                val left = SumTask(this.numbers.first until mid, ttlMap, true)
                val right = SumTask(mid..numbers.last, ttlMap, false)
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

    override fun getcopied(): Map<String, Any> = copied

    override fun getLeftSubTask(): Getter = leftSubTask

    override fun getRightSubTask(): Getter = rightSubTask
}

const val CHANGE_POSTFIX = " + 1"
