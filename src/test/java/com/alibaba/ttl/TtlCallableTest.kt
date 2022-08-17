package com.alibaba.ttl

import com.alibaba.*
import com.alibaba.ttl.testmodel.Call
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.Assert.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class TtlCallableTest : AnnotationSpec() {
    @Test
    fun test_TtlCallable_runInCurrentThread() {
        val ttlInstances = createParentTtlInstances()

        val call = Call("1", ttlInstances)


        val ttlCallable = TtlCallable.get(call)!!

        // create after new Task, won't see parent value in in task!
        createParentTtlInstancesAfterCreateChild(ttlInstances)


        // run in the *current* thread
        assertEquals("ok", ttlCallable.call())


        // child Inheritable
        assertChildTtlValues("1", call.copied)

        // child do not effect parent
        assertParentTtlValues(copyTtlValues(ttlInstances))
    }

    @Test
    fun test_TtlCallable_asyncRunByExecutorService() {
        val ttlInstances = createParentTtlInstances()

        val call = Call("1", ttlInstances)
        val ttlCallable = if (noTtlAgentRun()) TtlCallable.get(call) else call

        if (noTtlAgentRun()) {
            // create after new Task, won't see parent value in in task!
            createParentTtlInstancesAfterCreateChild(ttlInstances)
        }
        val future = executorService.submit(ttlCallable)
        if (!noTtlAgentRun()) {
            // create after new Task, won't see parent value in in task!
            createParentTtlInstancesAfterCreateChild(ttlInstances)
        }

        assertEquals("ok", future.get())


        // child Inheritable
        assertChildTtlValues("1", call.copied)

        // child do not effect parent
        assertParentTtlValues(copyTtlValues(ttlInstances))
    }

    @Test
    fun test_remove_sameAsNotSet() {
        val ttlInstances = createParentTtlInstances()


        // add and remove !!
        newTtlInstanceAndPut("add and removed!", ttlInstances).remove()

        val call = Call("1", ttlInstances)
        val ttlCallable = if (noTtlAgentRun()) TtlCallable.get(call) else call


        if (noTtlAgentRun()) {
            // create after new Task, won't see parent value in in task!
            createParentTtlInstancesAfterCreateChild(ttlInstances)
        }
        val future = executorService.submit(ttlCallable)
        if (!noTtlAgentRun()) {
            // create after new Task, won't see parent value in in task!
            createParentTtlInstancesAfterCreateChild(ttlInstances)
        }

        assertEquals("ok", future.get())


        // child Inheritable
        assertChildTtlValues("1", call.copied)

        // child do not affect parent
        assertParentTtlValues(copyTtlValues(ttlInstances))
    }

    @Test
    fun test_releaseTtlValueReferenceAfterCall() {
        val ttlInstances = createParentTtlInstances()

        val call = Call("1", ttlInstances)
        val ttlCallable = TtlCallable.get(call, true)!!
        assertSame(call, ttlCallable.callable)

        assertEquals("ok", executorService.submit(ttlCallable).get())


        val exception = shouldThrow<ExecutionException> {
            executorService.submit(ttlCallable).get()
        }
        exception.cause.shouldBeInstanceOf<IllegalStateException>()
        exception.message shouldContain "TTL value reference is released after call!"
    }

    @Test
    fun test_get_same() {
        val call = Call("1")
        val ttlCallable = TtlCallable.get(call)!!
        assertSame(call, ttlCallable.callable)
    }

    @Test
    fun test_get_idempotent() {
        val call = TtlCallable.get(Call("1"))

        shouldThrow<java.lang.IllegalStateException> {
            TtlCallable.get(call)
        }.message shouldContain "Already TtlCallable"
    }

    @Test
    fun test_get_nullInput() {
        TtlCallable.get<Any>(null).shouldBeNull()
    }

    @Test
    fun test_gets() {
        val call1 = Call("1")
        val call2 = Call("2")
        val call3 = Call("3")

        val callList = TtlCallable.gets(
            listOf<Callable<String>?>(call1, call2, null, call3)
        )

        callList.shouldHaveSize(4)
        callList[0].shouldBeInstanceOf<TtlCallable<*>>()
        callList[1].shouldBeInstanceOf<TtlCallable<*>>()
        callList[2].shouldBeNull()
        callList[3].shouldBeInstanceOf<TtlCallable<*>>()
    }

    @Test
    fun test_unwrap() {
        assertNull(TtlCallable.unwrap<String>(null))

        val callable = Callable { "hello" }
        val ttlCallable = TtlCallable.get(callable)


        assertSame(callable, TtlCallable.unwrap(callable))
        assertSame(callable, TtlCallable.unwrap(ttlCallable))

        assertSame(callable, TtlUnwrap.unwrap(callable))
        assertSame(callable, TtlUnwrap.unwrap(ttlCallable))


        assertEquals(listOf(callable), TtlCallable.unwraps(listOf(callable)))
        assertEquals(listOf(callable), TtlCallable.unwraps(listOf(ttlCallable)))
        assertEquals(listOf(callable, callable), TtlCallable.unwraps(listOf(ttlCallable, callable)))
        assertEquals(listOf<Callable<String>>(), TtlCallable.unwraps<String>(null))
    }

    @AfterAll
    fun afterAll() {
        executorService.shutdown()
        assertTrue("Fail to shutdown thread pool", executorService.awaitTermination(1, TimeUnit.SECONDS))
    }

    companion object {
        private val executorService = Executors.newFixedThreadPool(3).also { expandThreadPool(it) }
    }
}


