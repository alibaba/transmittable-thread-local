package com.alibaba.ttl

import com.alibaba.ttl.testmodel.Call
import org.junit.AfterClass
import org.junit.Test

import java.util.Arrays
import java.util.concurrent.*

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
class TtlCallableTest {

    @Test
    fun test_TtlCallable_inSameThread() {
        val ttlInstances = createTestTtlValue()

        val call = Call("1", ttlInstances)
        val ttlCallable = TtlCallable.get(call)

        // create after new Task, won't see parent value in in task!
        val after = TransmittableThreadLocal<String>()
        after.set(PARENT_AFTER_CREATE_TTL_TASK)
        ttlInstances[PARENT_AFTER_CREATE_TTL_TASK] = after

        val ret = ttlCallable.call()
        assertEquals("ok", ret)

        // child Inheritable
        assertTtlInstances(call.captured,
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD + 1, PARENT_MODIFIED_IN_CHILD,
                CHILD + 1, CHILD + 1
        )

        // child do not effect parent
        assertTtlInstances(captured(ttlInstances),
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD, // restored after call!
                PARENT_AFTER_CREATE_TTL_TASK, PARENT_AFTER_CREATE_TTL_TASK
        )
    }

    @Test
    fun test_TtlCallable_asyncWithExecutorService() {
        val ttlInstances = createTestTtlValue()

        val call = Call("1", ttlInstances)
        val ttlCallable = TtlCallable.get(call)

        // create after new Task, won't see parent value in in task!
        val after = TransmittableThreadLocal<String>()
        after.set(PARENT_AFTER_CREATE_TTL_TASK)
        ttlInstances[PARENT_AFTER_CREATE_TTL_TASK] = after

        val future = executorService.submit(ttlCallable)
        assertEquals("ok", future.get())

        // child Inheritable
        assertTtlInstances(call.captured,
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD + 1, PARENT_MODIFIED_IN_CHILD,
                CHILD + 1, CHILD + 1
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
        ttlInstances[PARENT_UNMODIFIED_IN_CHILD]!!.remove()

        val call = Call("1", ttlInstances)
        val ttlCallable = TtlCallable.get(call)

        // create after new Task, won't see parent value in in task!
        val after = TransmittableThreadLocal<String>()
        after.set(PARENT_AFTER_CREATE_TTL_TASK)
        ttlInstances[PARENT_AFTER_CREATE_TTL_TASK] = after

        val future = executorService.submit(ttlCallable)
        assertEquals("ok", future.get())

        // child Inheritable
        assertTtlInstances(call.captured,
                PARENT_MODIFIED_IN_CHILD + 1, PARENT_MODIFIED_IN_CHILD,
                CHILD + 1, CHILD + 1
        )

        // child do not effect parent
        assertTtlInstances(captured(ttlInstances),
                PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD,
                PARENT_AFTER_CREATE_TTL_TASK, PARENT_AFTER_CREATE_TTL_TASK
        )
    }

    @Test
    fun test_releaseTtlValueReferenceAfterCall() {
        val ttlInstances = createTestTtlValue()

        val call = Call("1", ttlInstances)
        val ttlCallable = TtlCallable.get(call, true)
        assertSame(call, ttlCallable.callable)

        var future = executorService.submit(ttlCallable)
        assertEquals("ok", future.get())

        future = executorService.submit(ttlCallable)
        try {
            future.get()
            fail()
        } catch (expected: ExecutionException) {
            assertThat<Throwable>(expected.cause, instanceOf(IllegalStateException::class.java))
            assertThat<String>(expected.message, containsString("TTL value reference is released after call!"))
        }

    }

    @Test
    fun test_get_same() {
        val call = Call("1", null)
        val ttlCallable = TtlCallable.get(call)
        assertSame(call, ttlCallable.callable)
    }

    @Test
    fun test_get_idempotent() {
        val call = TtlCallable.get(Call("1", null))
        try {
            TtlCallable.get(call)
            fail()
        } catch (e: IllegalStateException) {
            assertThat<String>(e.message, containsString("Already TtlCallable"))
        }

    }

    @Test
    @Throws(Exception::class)
    fun test_get_nullInput() {
        assertNull(TtlCallable.get<Any>(null))
    }

    @Test
    fun test_gets() {
        val call1 = Call("1", null)
        val call2 = Call("1", null)
        val call3 = Call("1", null)

        val callList = TtlCallable.gets(
                Arrays.asList<Callable<String>>(call1, call2, null, call3))

        assertEquals(4, callList.size.toLong())
        assertThat(callList[0], instanceOf(TtlCallable::class.java))
        assertThat(callList[1], instanceOf(TtlCallable::class.java))
        assertNull(callList[2])
        assertThat(callList[3], instanceOf(TtlCallable::class.java))
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
