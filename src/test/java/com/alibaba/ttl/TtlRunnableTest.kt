package com.alibaba.ttl

import com.alibaba.ttl.testmodel.*

import java.util.Arrays
import java.util.concurrent.*

import org.junit.AfterClass
import org.junit.Test

import com.alibaba.utils.Utils.CHILD
import com.alibaba.utils.Utils.PARENT_AFTER_CREATE_TTL_TASK
import com.alibaba.utils.Utils.PARENT_MODIFIED_IN_CHILD
import com.alibaba.utils.Utils.PARENT_UNMODIFIED_IN_CHILD
import com.alibaba.utils.Utils.assertTtlInstances
import com.alibaba.utils.Utils.captured
import com.alibaba.utils.Utils.createTestTtlValue
import com.alibaba.utils.Utils.expandThreadPool
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThat
import org.junit.Assert.fail


/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class TtlRunnableTest {

    @Test
    fun test_ttlRunnable_inSameThread() {
        val ttlInstances = createTestTtlValue()

        val task = Task("1", ttlInstances)
        val ttlRunnable = TtlRunnable.get(task)

        // create after new Task
        val after = TransmittableThreadLocal<String>()
        after.set(PARENT_AFTER_CREATE_TTL_TASK)
        ttlInstances[PARENT_AFTER_CREATE_TTL_TASK] = after

        ttlRunnable.run()

        // child Inheritable
        assertTtlInstances(task.captured,
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD + "1", PARENT_MODIFIED_IN_CHILD,
                CHILD + "1", CHILD + "1"
        )

        // child do not effect parent
        assertTtlInstances(captured(ttlInstances),
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD, // restored after call!
                PARENT_AFTER_CREATE_TTL_TASK, PARENT_AFTER_CREATE_TTL_TASK
        )
    }

    @Test
    fun test_ttlRunnable_asyncWithNewThread() {
        val ttlInstances = createTestTtlValue()

        val task = Task("1", ttlInstances)
        val thread1 = Thread(task)

        // create after new Task, won't see parent value in in task!
        val after = TransmittableThreadLocal<String>()
        after.set(PARENT_AFTER_CREATE_TTL_TASK)
        ttlInstances[PARENT_AFTER_CREATE_TTL_TASK] = after

        thread1.start()
        thread1.join()

        // child Inheritable
        assertTtlInstances(task.captured,
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD + "1", PARENT_MODIFIED_IN_CHILD,
                CHILD + "1", CHILD + "1"
        )

        // child do not effect parent
        assertTtlInstances(captured(ttlInstances),
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD,
                PARENT_AFTER_CREATE_TTL_TASK, PARENT_AFTER_CREATE_TTL_TASK
        )
    }

    @Test
    fun test_TtlRunnable_asyncWithExecutorService() {
        val ttlInstances = createTestTtlValue()

        val task = Task("1", ttlInstances)
        val ttlRunnable = TtlRunnable.get(task)

        // create after new Task, won't see parent value in in task!
        val after = TransmittableThreadLocal<String>()
        after.set(PARENT_AFTER_CREATE_TTL_TASK)
        ttlInstances[PARENT_AFTER_CREATE_TTL_TASK] = after

        val submit = executorService.submit(ttlRunnable)
        submit.get()

        // child Inheritable
        assertTtlInstances(task.captured,
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD + "1", PARENT_MODIFIED_IN_CHILD,
                CHILD + "1", CHILD + "1"
        )

        // child do not effect parent
        assertTtlInstances(captured(ttlInstances),
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD,
                PARENT_AFTER_CREATE_TTL_TASK, PARENT_AFTER_CREATE_TTL_TASK
        )
    }

    @Test
    fun test_removeSameAsNotSet() {
        val ttlInstances = createTestTtlValue()

        // remove TransmittableThreadLocal
        ttlInstances[PARENT_UNMODIFIED_IN_CHILD]!!.remove()

        val task = Task("1", ttlInstances)
        val ttlRunnable = TtlRunnable.get(task)

        // create after new Task, won't see parent value in in task!
        val after = TransmittableThreadLocal<String>()
        after.set(PARENT_AFTER_CREATE_TTL_TASK)
        ttlInstances[PARENT_AFTER_CREATE_TTL_TASK] = after

        val submit = executorService.submit(ttlRunnable)
        submit.get()

        // child Inheritable
        assertTtlInstances(task.captured,
                PARENT_MODIFIED_IN_CHILD + "1", PARENT_MODIFIED_IN_CHILD,
                CHILD + 1, CHILD + 1
        )

        // child do not effect parent
        assertTtlInstances(captured(ttlInstances),
                PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD,
                PARENT_AFTER_CREATE_TTL_TASK, PARENT_AFTER_CREATE_TTL_TASK
        )
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
        parent.set(FooPojo(PARENT_UNMODIFIED_IN_CHILD, 1))
        ttlInstances[PARENT_UNMODIFIED_IN_CHILD] = parent

        val p = DeepCopyFooTransmittableThreadLocal()
        p.set(FooPojo(PARENT_MODIFIED_IN_CHILD, 2))
        ttlInstances[PARENT_MODIFIED_IN_CHILD] = p

        val task = FooTask("1", ttlInstances)
        val ttlRunnable = TtlRunnable.get(task)

        // create after new Task, won't see parent value in in task!
        val after = DeepCopyFooTransmittableThreadLocal()
        after.set(FooPojo(PARENT_AFTER_CREATE_TTL_TASK, 4))
        ttlInstances[PARENT_AFTER_CREATE_TTL_TASK] = after

        val submit = executorService.submit(ttlRunnable)
        submit.get()

        // child Inheritable
        assertEquals(3, task.captured.size.toLong())
        assertEquals(FooPojo(PARENT_UNMODIFIED_IN_CHILD, 1), task.captured[PARENT_UNMODIFIED_IN_CHILD])
        assertEquals(FooPojo(PARENT_MODIFIED_IN_CHILD + "1", 2), task.captured[PARENT_MODIFIED_IN_CHILD])
        assertEquals(FooPojo(CHILD + 1, 3), task.captured[CHILD + 1])

        // child do not effect parent
        val captured = captured(ttlInstances)
        assertEquals(3, captured.size.toLong())
        assertEquals(FooPojo(PARENT_UNMODIFIED_IN_CHILD, 1), captured[PARENT_UNMODIFIED_IN_CHILD])
        assertEquals(FooPojo(PARENT_MODIFIED_IN_CHILD, 2), captured[PARENT_MODIFIED_IN_CHILD])
        assertEquals(FooPojo(PARENT_AFTER_CREATE_TTL_TASK, 4), captured[PARENT_AFTER_CREATE_TTL_TASK])
    }

    @Test
    fun test_releaseTtlValueReferenceAfterRun() {
        val ttlInstances = createTestTtlValue()

        val task = Task("1", ttlInstances)
        val ttlRunnable = TtlRunnable.get(task, true)

        var future = executorService.submit(ttlRunnable)
        assertNull(future.get())

        future = executorService.submit(ttlRunnable)
        try {
            future.get()
            fail()
        } catch (expected: ExecutionException) {
            assertThat<Throwable>(expected.cause, instanceOf(IllegalStateException::class.java))
            assertThat<String>(expected.message, containsString("TTL value reference is released after run!"))
        }

    }

    @Test
    fun test_get_same() {
        val task = Task("1", null)
        val ttlRunnable = TtlRunnable.get(task)
        assertSame(task, ttlRunnable.runnable)
    }

    @Test
    fun test_get_idempotent() {
        val task = TtlRunnable.get(Task("1", null))
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
        val task1 = Task("1", null)
        val task2 = Task("1", null)
        val task3 = Task("1", null)

        val taskList = TtlRunnable.gets(Arrays.asList<Runnable>(task1, task2, null, task3))

        assertEquals(4, taskList.size.toLong())
        assertThat(taskList[0], instanceOf(TtlRunnable::class.java))
        assertThat(taskList[1], instanceOf(TtlRunnable::class.java))
        assertNull(taskList[2])
        assertThat(taskList[3], instanceOf(TtlRunnable::class.java))
    }

    companion object {
        private val executorService = Executors.newFixedThreadPool(3)

        init {
            expandThreadPool(executorService)
        }

        @AfterClass
        @Suppress("unused")
        fun afterClass() {
            executorService.shutdown()
            executorService.awaitTermination(100, TimeUnit.MILLISECONDS)
            if (!executorService.isTerminated) fail("Fail to shutdown thread pool")
        }
    }
}
