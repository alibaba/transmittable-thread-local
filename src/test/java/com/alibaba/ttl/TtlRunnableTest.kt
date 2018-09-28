package com.alibaba.ttl

import com.alibaba.*
import com.alibaba.ttl.testmodel.*
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.AfterClass
import org.junit.Assert.*
import org.junit.Test
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class TtlRunnableTest {

    @Test
    fun test_ttlRunnable_inSameThread() {
        val ttlInstances = createParentTtlInstances()

        val task = Task("1", ttlInstances)
        val ttlRunnable = TtlRunnable.get(task)!!

        // create after new Task, won't see parent value in in task!
        createParentTtlInstancesAfterCreateChild(ttlInstances)


        ttlRunnable.run()


        // child Inheritable
        assertChildTtlValues("1", task.copied)

        // child do not effect parent
        assertParentTtlValues(copyTtlValues(ttlInstances))
    }

    @Test
    fun test_ttlRunnable_asyncWithNewThread() {
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
    fun test_TtlRunnable_asyncWithExecutorService() {
        val ttlInstances = createParentTtlInstances()

        val task = Task("1", ttlInstances)
        val ttlRunnable = TtlRunnable.get(task)

        // create after new Task, won't see parent value in in task!
        createParentTtlInstancesAfterCreateChild(ttlInstances)


        val submit = executorService.submit(ttlRunnable)
        submit.get()


        // child Inheritable
        assertChildTtlValues("1", task.copied)

        // child do not effect parent
        assertParentTtlValues(copyTtlValues(ttlInstances))
    }

    @Test
    fun test_removeSameAsNotSet() {
        val ttlInstances = createParentTtlInstances()

        // add and remove !!
        newTtlInstanceAndPut("add and removed!", ttlInstances).remove()


        val task = Task("1", ttlInstances)
        val ttlRunnable = TtlRunnable.get(task)

        // create after new Task, won't see parent value in in task!
        createParentTtlInstancesAfterCreateChild(ttlInstances)

        val submit = executorService.submit(ttlRunnable)
        submit.get()


        // child Inheritable
        assertChildTtlValues("1", task.copied)

        // child do not effect parent
        assertParentTtlValues(copyTtlValues(ttlInstances))
    }

    @Test
    fun test_callback_copy_beforeExecute_afterExecute() {
        val callbackTestTransmittableThreadLocal = CallbackTestTransmittableThreadLocal()

        callbackTestTransmittableThreadLocal.set(FooPojo("jerry", 42))

        val task1 = { }
        // do copy when decorate runnable
        val ttlRunnable1 = TtlRunnable.get(task1)

        executorService.submit(ttlRunnable1).get()

        assertEquals(1, callbackTestTransmittableThreadLocal.copyCounter.get().toLong())
        assertEquals(1, callbackTestTransmittableThreadLocal.beforeExecuteCounter.get().toLong())
        assertEquals(1, callbackTestTransmittableThreadLocal.afterExecuteCounter.get().toLong())


        executorService.submit(ttlRunnable1).get()

        assertEquals(1, callbackTestTransmittableThreadLocal.copyCounter.get().toLong())
        assertEquals(2, callbackTestTransmittableThreadLocal.beforeExecuteCounter.get().toLong())
        assertEquals(2, callbackTestTransmittableThreadLocal.afterExecuteCounter.get().toLong())


        val task2 = { }
        // do copy when decorate runnable
        val ttlRunnable2 = TtlRunnable.get(task2)


        executorService.submit(ttlRunnable2).get()

        assertEquals(2, callbackTestTransmittableThreadLocal.copyCounter.get().toLong())
        assertEquals(3, callbackTestTransmittableThreadLocal.beforeExecuteCounter.get().toLong())
        assertEquals(3, callbackTestTransmittableThreadLocal.afterExecuteCounter.get().toLong())
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
        val ttlRunnable = TtlRunnable.get(task)

        // create after new Task, won't see parent value in in task!
        val after = DeepCopyFooTransmittableThreadLocal()
        after.set(FooPojo(PARENT_CREATE_AFTER_CREATE_CHILD, 4))
        ttlInstances[PARENT_CREATE_AFTER_CREATE_CHILD] = after

        val submit = executorService.submit(ttlRunnable)
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

        val taskList = TtlRunnable.gets(Arrays.asList<Runnable>(task1, task2, null, task3))

        assertEquals(4, taskList.size.toLong())
        assertThat(taskList[0], instanceOf(TtlRunnable::class.java))
        assertThat(taskList[1], instanceOf(TtlRunnable::class.java))
        assertNull(taskList[2])
        assertThat(taskList[3], instanceOf(TtlRunnable::class.java))
    }

    companion object {
        private val executorService = Executors.newFixedThreadPool(3).also { expandThreadPool(it) }

        @AfterClass
        @Suppress("unused")
        fun afterClass() {
            executorService.shutdown()
            executorService.awaitTermination(100, TimeUnit.MILLISECONDS)
            if (!executorService.isTerminated) fail("Fail to shutdown thread pool")
        }
    }
}
