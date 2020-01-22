package com.alibaba.ttl

import com.alibaba.*
import com.alibaba.ttl.testmodel.DeepCopyFooTransmittableThreadLocal
import com.alibaba.ttl.testmodel.FooPojo
import com.alibaba.ttl.testmodel.FooTask
import com.alibaba.ttl.testmodel.Task
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.AfterClass
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger


/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class TtlRunnableTest {
    @Test
    fun test_ttlRunnable_runInCurrentThread() {
        val ttlInstances = createParentTtlInstances()

        val task = Task("1", ttlInstances)
        val ttlRunnable = TtlRunnable.get(task)!!

        // create after new Task, won't see parent value in in task!
        createParentTtlInstancesAfterCreateChild(ttlInstances)


        // run in the *current* thread
        ttlRunnable.run()


        // child Inheritable
        assertChildTtlValues("1", task.copied)

        // child do not effect parent
        assertParentTtlValues(copyTtlValues(ttlInstances))
    }

    @Test
    fun test_ttlRunnable_asyncRunByNewThread() {
        val ttlInstances = createParentTtlInstances()

        val task = Task("1", ttlInstances)
        val thread1 = Thread(task)

        // create after new Task, won't see parent value in in task!
        createParentTtlInstancesAfterCreateChild(ttlInstances)


        thread1.start()
        thread1.join()


        // child Inheritable
        assertChildTtlValues("1", task.copied)

        // child do not effect parent
        assertParentTtlValues(copyTtlValues(ttlInstances))
    }

    @Test
    fun test_TtlRunnable_asyncRunByExecutorService() {
        val ttlInstances = createParentTtlInstances()

        val task = Task("1", ttlInstances)
        val ttlRunnable = if (noTtlAgentRun()) TtlRunnable.get(task) else task

        if (noTtlAgentRun()) {
            // create after new Task, won't see parent value in in task!
            createParentTtlInstancesAfterCreateChild(ttlInstances)
        }
        val submit = executorService.submit(ttlRunnable)
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
    fun test_remove_sameAsNotSet() {
        val ttlInstances = createParentTtlInstances()

        // add and remove !!
        newTtlInstanceAndPut("add and removed!", ttlInstances).remove()


        val task = Task("1", ttlInstances)
        val ttlRunnable = if (noTtlAgentRun()) TtlRunnable.get(task) else task

        if (noTtlAgentRun()) {
            // create after new Task, won't see parent value in in task!
            createParentTtlInstancesAfterCreateChild(ttlInstances)
        }
        val submit = executorService.submit(ttlRunnable)
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
    fun test_callback_copy_beforeExecute_afterExecute() {
        class CounterTransmittableThreadLocal : TransmittableThreadLocal<String?>() {
            val copyCounter = AtomicInteger()
            val beforeExecuteCounter = AtomicInteger()
            val afterExecuteCounter = AtomicInteger()

            override fun copy(parentValue: String?): String? {
                copyCounter.incrementAndGet()
                return super.copy(parentValue)
            }

            override fun beforeExecute() {
                beforeExecuteCounter.incrementAndGet()
                super.beforeExecute()
            }

            override fun afterExecute() {
                afterExecuteCounter.incrementAndGet()
                super.afterExecute()
            }
        }

        val counterTtl = CounterTransmittableThreadLocal()
        counterTtl.set("Foo")

        // do copy when decorate runnable
        val ttlRunnable1 = if (noTtlAgentRun()) TtlRunnable.get { /* do nothing Runnable */ } else Runnable { /* do nothing Runnable */ }
        assertEquals(if (noTtlAgentRun()) 1 else 0, counterTtl.copyCounter.get())
        assertEquals(0, counterTtl.beforeExecuteCounter.get())
        assertEquals(0, counterTtl.afterExecuteCounter.get())

        // do before/after when run
        executorService.submit(ttlRunnable1).get()
        assertEquals(1, counterTtl.copyCounter.get())
        assertEquals(1, counterTtl.beforeExecuteCounter.get())
        Thread.sleep(1)
        assertEquals(1, counterTtl.afterExecuteCounter.get())

        // do before/after when run
        executorService.submit(ttlRunnable1).get()
        assertEquals(if (noTtlAgentRun()) 1 else 2, counterTtl.copyCounter.get())
        assertEquals(2, counterTtl.beforeExecuteCounter.get())
        Thread.sleep(1)
        assertEquals(2, counterTtl.afterExecuteCounter.get())

        // do copy when decorate runnable
        val ttlRunnable2 = if (noTtlAgentRun()) TtlRunnable.get { /* do nothing Runnable */ } else Runnable { /* do nothing Runnable */ }
        assertEquals(if (noTtlAgentRun()) 2 else 2, counterTtl.copyCounter.get())
        assertEquals(2, counterTtl.beforeExecuteCounter.get())
        Thread.sleep(1)
        assertEquals(2, counterTtl.afterExecuteCounter.get())

        // do before/after when run
        executorService.submit(ttlRunnable2).get()
        assertEquals(if (noTtlAgentRun()) 2 else 3, counterTtl.copyCounter.get())
        assertEquals(3, counterTtl.beforeExecuteCounter.get())
        Thread.sleep(1)
        assertEquals(3, counterTtl.afterExecuteCounter.get())
    }

    @Test
    fun test_TtlRunnable_copyObject() {
        val ttlInstances = ConcurrentHashMap<String, TransmittableThreadLocal<FooPojo>>()

        val parent = DeepCopyFooTransmittableThreadLocal()
        parent.set(FooPojo(PARENT_CREATE_UNMODIFIED_IN_CHILD, 1))
        ttlInstances[PARENT_CREATE_UNMODIFIED_IN_CHILD] = parent

        val p = DeepCopyFooTransmittableThreadLocal()
        p.set(FooPojo(PARENT_CREATE_MODIFIED_IN_CHILD, 2))
        ttlInstances[PARENT_CREATE_MODIFIED_IN_CHILD] = p

        val task = FooTask("1", ttlInstances)
        val ttlRunnable = if (noTtlAgentRun()) TtlRunnable.get(task) else task

        if (noTtlAgentRun()) {
            // create after new Task, won't see parent value in in task!
            val after = DeepCopyFooTransmittableThreadLocal()
            after.set(FooPojo(PARENT_CREATE_AFTER_CREATE_CHILD, 4))
            ttlInstances[PARENT_CREATE_AFTER_CREATE_CHILD] = after
        }
        val submit = executorService.submit(ttlRunnable)
        if (!noTtlAgentRun()) {
            // create after new Task, won't see parent value in in task!
            val after = DeepCopyFooTransmittableThreadLocal()
            after.set(FooPojo(PARENT_CREATE_AFTER_CREATE_CHILD, 4))
            ttlInstances[PARENT_CREATE_AFTER_CREATE_CHILD] = after
        }

        submit.get()

        // child Inheritable
        assertEquals(3, task.copied.size.toLong())
        assertEquals(FooPojo(PARENT_CREATE_UNMODIFIED_IN_CHILD, 1), task.copied[PARENT_CREATE_UNMODIFIED_IN_CHILD])
        assertEquals(FooPojo(PARENT_CREATE_MODIFIED_IN_CHILD + "1", 2), task.copied[PARENT_CREATE_MODIFIED_IN_CHILD])
        assertEquals(FooPojo(CHILD_CREATE + 1, 3), task.copied[CHILD_CREATE + 1])

        // child do not effect parent
        val copied = copyTtlValues(ttlInstances)
        assertEquals(3, copied.size.toLong())
        assertEquals(FooPojo(PARENT_CREATE_UNMODIFIED_IN_CHILD, 1), copied[PARENT_CREATE_UNMODIFIED_IN_CHILD])
        assertEquals(FooPojo(PARENT_CREATE_MODIFIED_IN_CHILD, 2), copied[PARENT_CREATE_MODIFIED_IN_CHILD])
        assertEquals(FooPojo(PARENT_CREATE_AFTER_CREATE_CHILD, 4), copied[PARENT_CREATE_AFTER_CREATE_CHILD])
    }

    @Test
    fun test_releaseTtlValueReferenceAfterRun() {
        val ttlInstances = createParentTtlInstances()

        val task = Task("1", ttlInstances)
        val ttlRunnable = TtlRunnable.get(task, true)

        assertNull(executorService.submit(ttlRunnable).get())

        try {
            executorService.submit(ttlRunnable).get()
            fail()
        } catch (expected: ExecutionException) {
            assertThat<Throwable>(expected.cause, instanceOf(IllegalStateException::class.java))
            assertThat<String>(expected.message, containsString("TTL value reference is released after run!"))
        }

    }

    @Test
    fun test_get_same() {
        val task = Task("1")
        val ttlRunnable = TtlRunnable.get(task)!!
        assertSame(task, ttlRunnable.runnable)
    }

    @Test
    fun test_get_idempotent() {
        val task = TtlRunnable.get(Task("1"))
        try {
            TtlRunnable.get(task)
            fail()
        } catch (e: IllegalStateException) {
            assertThat<String>(e.message, containsString("Already TtlRunnable"))
        }

    }

    @Test
    fun test_get_nullInput() {
        assertNull(TtlRunnable.get(null))
    }

    @Test
    fun test_gets() {
        val task1 = Task("1")
        val task2 = Task("2")
        val task3 = Task("3")

        val taskList = TtlRunnable.gets(listOf<Runnable?>(task1, task2, null, task3))

        assertEquals(4, taskList.size.toLong())
        assertThat(taskList[0], instanceOf(TtlRunnable::class.java))
        assertThat(taskList[1], instanceOf(TtlRunnable::class.java))
        assertNull(taskList[2])
        assertThat(taskList[3], instanceOf(TtlRunnable::class.java))
    }

    @Test
    fun test_unwrap() {
        assertNull(TtlRunnable.unwrap(null))

        val runnable = Runnable {}
        val ttlRunnable = TtlRunnable.get(runnable)


        assertSame(runnable, TtlRunnable.unwrap(runnable))
        assertSame(runnable, TtlRunnable.unwrap(ttlRunnable))

        assertSame(runnable, TtlUnwrap.unwrap(runnable))
        assertSame(runnable, TtlUnwrap.unwrap(ttlRunnable))


        assertEquals(listOf(runnable), TtlRunnable.unwraps(listOf(runnable)))
        assertEquals(listOf(runnable), TtlRunnable.unwraps(listOf(ttlRunnable)))
        assertEquals(listOf(runnable, runnable), TtlRunnable.unwraps(listOf(ttlRunnable, runnable)))
        assertEquals(listOf<Runnable>(), TtlRunnable.unwraps(null))
    }

    companion object {
        private val executorService = Executors.newFixedThreadPool(3).also { expandThreadPool(it) }

        @AfterClass
        @JvmStatic
        @Suppress("unused")
        fun afterClass() {
            executorService.shutdown()
            executorService.awaitTermination(100, TimeUnit.MILLISECONDS)
            if (!executorService.isTerminated) fail("Fail to shutdown thread pool")
        }
    }
}
