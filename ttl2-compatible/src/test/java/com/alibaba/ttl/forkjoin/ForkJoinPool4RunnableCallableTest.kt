package com.alibaba.ttl.forkjoin

import com.alibaba.*
import com.alibaba.ttl.TtlCallable
import com.alibaba.ttl.TtlRunnable
import com.alibaba.ttl.testmodel.Call
import com.alibaba.ttl.testmodel.Task
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import java.util.concurrent.ForkJoinPool

private val pool = ForkJoinPool()

class ForkJoinPool4RunnableCallableTest : AnnotationSpec() {

    @Test
    fun test_Runnable() {
        val ttlInstances = createParentTtlInstances()

        val task = Task("1", ttlInstances)
        val ttlRunnable = if (noTtlAgentRun()) TtlRunnable.get(task) else task

        if (noTtlAgentRun()) {
            // create after new Task, won't see parent value in in task!
            createParentTtlInstancesAfterCreateChild(ttlInstances)
        }
        val submit = pool.submit(ttlRunnable)
        if (!noTtlAgentRun()) {
            // create after new Task, won't see parent value in in task!
            createParentTtlInstancesAfterCreateChild(ttlInstances)
        }


        submit.get()


        // child Inheritable
        assertChildTtlValues("1", task.copied)

        // child do not effect parent
        assertParentTtlValues(copyTtlValues(ttlInstances))
    }

    @Test
    fun test_Callable() {
        val ttlInstances = createParentTtlInstances()

        val call = Call("1", ttlInstances)
        val ttlCallable = if (noTtlAgentRun()) TtlCallable.get(call) else call

        if (noTtlAgentRun()) {
            // create after new Task, won't see parent value in in task!
            createParentTtlInstancesAfterCreateChild(ttlInstances)
        }
        val future = pool.submit(ttlCallable)
        if (!noTtlAgentRun()) {
            // create after new Task, won't see parent value in in task!
            createParentTtlInstancesAfterCreateChild(ttlInstances)
        }

        future.get() shouldBe "ok"


        // child Inheritable
        assertChildTtlValues("1", call.copied)

        // child do not effect parent
        assertParentTtlValues(copyTtlValues(ttlInstances))
    }
}
