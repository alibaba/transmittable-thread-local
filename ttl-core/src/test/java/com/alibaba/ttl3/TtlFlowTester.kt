package com.alibaba.ttl3

import com.alibaba.getForTest
import com.alibaba.noTtlAgentRun
import io.kotest.assertions.fail
import io.kotest.core.spec.style.scopes.FunSpecRootScope
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Future
import kotlin.random.Random


////////////////////////////////////////////////////////////////////////////////
// Entry Methods
////////////////////////////////////////////////////////////////////////////////

typealias CheckLogicInTask = () -> Unit

fun CheckLogicInTask.toRunnable() = Runnable { this() }

typealias TaskRunner = () -> Future<*>

/**
 * - create task immediately with input [CheckLogicInTask] parameter
 * - return task runner to run task later
 */
typealias TaskRunnerGenerator = (CheckLogicInTask) -> TaskRunner

enum class CaptureTime {
    TASK_CREATE,
    TASK_RUN,
}


fun FunSpecRootScope.ttlFlowTest(
    name: String,
    captureTime: CaptureTime = CaptureTime.TASK_CREATE,
    taskRunnerGenerator: TaskRunnerGenerator,
) {
    test(name) {
        runTtlFlowTest(captureTime, taskRunnerGenerator)
    }
}

val captureTimeByAgentRun =
    if (noTtlAgentRun()) CaptureTime.TASK_CREATE
    else CaptureTime.TASK_RUN

////////////////////////////////////////////////////////////////////////////////
// TTL Flow Tester
////////////////////////////////////////////////////////////////////////////////

private fun runTtlFlowTest(
    captureTime: CaptureTime = CaptureTime.TASK_CREATE,
    taskRunnerGenerator: TaskRunnerGenerator
) {
    TtlFlowTester(captureTime, taskRunnerGenerator).run()
}

private class TtlFlowTester(
    private val captureTime: CaptureTime = CaptureTime.TASK_CREATE,
    private val taskRunnerGenerator: TaskRunnerGenerator
) {
    fun run() {
        register(::StringTtlCreateAtParentBeginAndRemoveImmediately)
        register(::StringTtlCreateAtParentBegin.fill())

        register(::DeepCopyTtlCreateAtParentBeginUnmodifiedInChild)
        register(::DeepCopyTtlCreateAtParentBeginModifiedInChild)

        compositeTtlFlow.beginInParent()

        //////////////////////////////////////////////
        // create task
        //////////////////////////////////////////////
        val task = taskRunnerGenerator(::checkInTask)

        compositeTtlFlow.afterTaskCreatedInParent()

        register(::StringTtlCreateAtParentAfterCreateTask.fill())

        register(::DeepCopyTtlCreateAtParentAfterCreateTask)

        //////////////////////////////////////////////
        // start task
        //////////////////////////////////////////////
        val future: Future<*> = task()

        compositeTtlFlow.afterTaskStartedInParent()

        register(::StringTtlCreateAtParentAfterStartTask)

        //////////////////////////////////////////////
        // finish task
        //////////////////////////////////////////////
        future.getForTest()

        compositeTtlFlow.afterTaskFinishedInParent()
    }

    private fun checkInTask() {
        register(::StringTtlCreateAtChildBegin)
        register(::DeepCopyTtlCreateAtChildBegin)

        compositeTtlFlow.beginInChild()

        compositeTtlFlow.endInChild()
    }

    private val compositeTtlFlow = CompositeTtlFlow()

    private inline fun register(factory: () -> TtlFlow) {
        val ttlFlow = factory()
        compositeTtlFlow.register(ttlFlow)
        ttlFlow.settingAfterConstruct()
    }

    private fun ((CaptureTime) -> TtlFlow).fill(): () -> TtlFlow = { this(captureTime) }
}


private interface TtlFlow {
    fun settingAfterConstruct()

    fun beginInParent()

    fun afterTaskCreatedInParent()

    fun afterTaskStartedInParent()

    fun beginInChild()

    fun endInChild()

    fun afterTaskFinishedInParent()
}

/**
 * TTL instance create in parent thread when begin parent
 * and remove value immediately
 *
 * same as not set
 */
private class StringTtlCreateAtParentBeginAndRemoveImmediately :
    TransmittableThreadLocal<String>(), TtlFlow {
    override fun settingAfterConstruct() {
        set("init ${Random.nextLong()}")
    }

    override fun beginInParent() {
        remove()
    }

    override fun afterTaskCreatedInParent() {
    }

    override fun afterTaskStartedInParent() {
    }

    override fun beginInChild() {
        get().shouldBeNull()
    }

    override fun endInChild() {
        get().shouldBeNull()
        set(Random.nextLong().toString())
    }

    override fun afterTaskFinishedInParent() {
        get().shouldBeNull()
    }
}

/**
 * TTL instance create in parent thread when begin parent thread
 */
private class StringTtlCreateAtParentBegin(private val captureTime: CaptureTime) :
    TransmittableThreadLocal<String>(), TtlFlow {

    private val initValue = "init ${Random.nextLong()}"
    private val modifiedValue = "modified ${Random.nextLong()}"

    override fun settingAfterConstruct() {
        set(initValue)
    }

    override fun beginInParent() {
    }

    override fun afterTaskCreatedInParent() {
        get() shouldBe initValue
        set(modifiedValue)
    }

    override fun afterTaskStartedInParent() {
        get() shouldBe modifiedValue
    }

    override fun beginInChild() {
        if (captureTime == CaptureTime.TASK_CREATE) {
            get() shouldBe initValue
        } else {
            get() shouldBe modifiedValue
        }
    }

    override fun endInChild() {
        set("modified by child ${Random.nextLong()}")
    }

    override fun afterTaskFinishedInParent() {
        get() shouldBe modifiedValue
    }
}

/**
 * TTL instance create in parent thread after create task
 */
private class StringTtlCreateAtParentAfterCreateTask(
    private val captureTime: CaptureTime
) : TransmittableThreadLocal<String>(), TtlFlow {

    private val initValue = "init ${Random.nextLong()}"

    override fun settingAfterConstruct() {
        set(initValue)
    }

    override fun beginInParent() {
        fail("should never be called")
    }

    override fun afterTaskCreatedInParent() {
        fail("should never be called")
    }

    override fun afterTaskStartedInParent() {
        get() shouldBe initValue
    }

    override fun beginInChild() {
        if (captureTime == CaptureTime.TASK_CREATE) {
            get().shouldBeNull()
        } else {
            get() shouldBe initValue
        }
    }

    override fun endInChild() {
        set("modified by child ${Random.nextLong()}")
    }

    override fun afterTaskFinishedInParent() {
        get() shouldBe initValue
    }
}

/**
 * TTL instance create in parent thread after start task
 */
private class StringTtlCreateAtParentAfterStartTask : TransmittableThreadLocal<String>(), TtlFlow {
    private val initValue = "init ${Random.nextLong()}"

    override fun settingAfterConstruct() {
        set(initValue)
    }

    override fun beginInParent() {
        fail("should never be called")
    }

    override fun afterTaskCreatedInParent() {
        fail("should never be called")
    }

    override fun afterTaskStartedInParent() {
        fail("should never be called")
    }

    override fun beginInChild() {
        get().shouldBeNull()
    }

    override fun endInChild() {
        set("modified by child ${Random.nextLong()}")
    }

    override fun afterTaskFinishedInParent() {
        get() shouldBe initValue
    }
}

/**
 * TTL instance create in child thread when begin child task
 */
private class StringTtlCreateAtChildBegin : TransmittableThreadLocal<String>(), TtlFlow {

    private val initValue = "init ${Random.nextLong()}"

    override fun settingAfterConstruct() {
        set(initValue)
    }

    override fun beginInParent() {
        fail("should never be called")
    }

    override fun afterTaskCreatedInParent() {
        fail("should never be called")
    }

    override fun afterTaskStartedInParent() {
    }

    override fun beginInChild() {
    }

    override fun endInChild() {
        get() shouldBe initValue
    }

    override fun afterTaskFinishedInParent() {
        get().shouldBeNull()
    }
}

private class DeepCopyTtlCreateAtParentBeginUnmodifiedInChild : TransmittableThreadLocal<Pojo?>(), TtlFlow {
    override fun transmitteeValue(parentValue: Pojo?): Pojo? = parentValue?.copy()


    private val initName = "parent create unmodified in child ${Random.nextLong()}"
    private val initAge = Random.nextInt()

    override fun settingAfterConstruct() {
        set(Pojo(initName, initAge))
    }

    override fun beginInParent() {
    }

    override fun afterTaskCreatedInParent() {
    }

    override fun afterTaskStartedInParent() {
    }

    override fun beginInChild() {
        get() shouldBe Pojo(initName, initAge)
    }

    override fun endInChild() {
    }

    override fun afterTaskFinishedInParent() {
        get() shouldBe Pojo(initName, initAge)
    }
}

private class DeepCopyTtlCreateAtParentBeginModifiedInChild : TransmittableThreadLocal<Pojo?>(), TtlFlow {
    override fun transmitteeValue(parentValue: Pojo?): Pojo? = parentValue?.copy()


    private val initName = "parent create modified in child ${Random.nextLong()}"
    private val initAge = Random.nextInt()

    override fun settingAfterConstruct() {
        set(Pojo(initName, initAge))
    }

    override fun beginInParent() {
    }

    override fun afterTaskCreatedInParent() {
    }

    override fun afterTaskStartedInParent() {
    }

    override fun beginInChild() {
        get() shouldBe Pojo(initName, initAge)
        get()!!.name = "modified in child ${Random.nextLong()}"
        get()!!.age++
    }

    override fun endInChild() {
    }

    override fun afterTaskFinishedInParent() {
        get() shouldBe Pojo(initName, initAge)
    }
}

private class DeepCopyTtlCreateAtParentAfterCreateTask : TransmittableThreadLocal<Pojo?>(), TtlFlow {
    override fun transmitteeValue(parentValue: Pojo?): Pojo? = parentValue?.copy()

    private val initName = "parent create after create task ${Random.nextLong()}"
    private val initAge = Random.nextInt()

    override fun settingAfterConstruct() {
        set(Pojo(initName, initAge))
    }

    override fun beginInParent() {
        fail("should never be called")
    }

    override fun afterTaskCreatedInParent() {
        fail("should never be called")
    }

    override fun afterTaskStartedInParent() {
    }

    override fun beginInChild() {
        get().shouldBeNull()
    }

    override fun endInChild() {
    }

    override fun afterTaskFinishedInParent() {
        get() shouldBe Pojo(initName, initAge)
    }
}

private class DeepCopyTtlCreateAtChildBegin : TransmittableThreadLocal<Pojo?>(), TtlFlow {
    override fun transmitteeValue(parentValue: Pojo?): Pojo? = parentValue?.copy()

    private val initName = "create at child begin ${Random.nextLong()}"
    private val initAge = Random.nextInt()

    override fun settingAfterConstruct() {
        set(Pojo(initName, initAge))
    }

    override fun beginInParent() {
        fail("should never be called")
    }

    override fun afterTaskCreatedInParent() {
        fail("should never be called")

    }

    override fun afterTaskStartedInParent() {
    }

    override fun beginInChild() {
        get() shouldBe Pojo(initName, initAge)
    }

    override fun endInChild() {
    }

    override fun afterTaskFinishedInParent() {
        get().shouldBeNull()
    }
}


private class CompositeTtlFlow : TtlFlow {
    private val instances = CopyOnWriteArraySet<TtlFlow>()

    fun register(ttlFlow: TtlFlow) {
        instances.add(ttlFlow).shouldBeTrue()
    }

    override fun settingAfterConstruct() {
        fail("should never be called")
    }

    override fun beginInParent() {
        for (ttl in instances.shuffled()) {
            ttl.beginInParent()
        }
    }

    override fun afterTaskCreatedInParent() {
        for (ttl in instances.shuffled()) {
            ttl.afterTaskCreatedInParent()
        }
    }

    override fun afterTaskStartedInParent() {
        for (ttl in instances.shuffled()) {
            ttl.afterTaskStartedInParent()
        }
    }

    override fun beginInChild() {
        for (ttl in instances.shuffled()) {
            ttl.beginInChild()
        }
    }

    override fun endInChild() {
        for (ttl in instances.shuffled()) {
            ttl.endInChild()
        }
    }

    override fun afterTaskFinishedInParent() {
        for (ttl in instances.shuffled()) {
            ttl.afterTaskFinishedInParent()
        }
    }
}


////////////////////////////////////////////////////////////////////////////////
// TTL classes for test
////////////////////////////////////////////////////////////////////////////////

private data class Pojo(var name: String?, var age: Int)
