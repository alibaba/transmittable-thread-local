package com.alibaba.ttl3

import com.alibaba.expandThreadPool
import com.alibaba.getForTest
import com.alibaba.shutdownForTest
import com.alibaba.ttlWrapIfNoTtlAgentRun
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldBeTypeOf
import java.util.concurrent.*
import kotlin.concurrent.thread

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class TtlCallableTest : FunSpec({
    lateinit var executorService: ExecutorService

    beforeSpec {
        executorService = Executors.newFixedThreadPool(3).also { expandThreadPool(it) }
    }

    afterSpec {
        executorService.shutdownForTest()
    }


    ttlFlowTest("run in current thread") { checkLogicInTask ->
        val ttlCallable = TtlCallable.get {
            checkLogicInTask()
            42
        }!!

        val taskRunner: TaskRunner = {
            // run in the *current* thread
            ttlCallable.call()

            CompletableFuture.completedFuture(null)
        }
        taskRunner
    }

    ttlFlowTest("async run by new thread") { checkLogicInTask ->
        val ttlCallable = TtlCallable.get {
            checkLogicInTask()
            42
        }!!

        val taskRunner: TaskRunner = {
            val future = CompletableFuture<Void>()
            // run in new thread
            thread {
                ttlCallable.call()
                future.complete(null)
            }
            future
        }
        taskRunner
    }

    ttlFlowTest("async run by executor service", captureTimeByAgentRun) { checkLogicInTask ->
        val callable = Callable {
            checkLogicInTask()
            42
        }.ttlWrapIfNoTtlAgentRun()

        val taskRunner: TaskRunner = {
            executorService.submit(callable)
        }
        taskRunner
    }


    test("release ttl value reference after call") {
        val call = Callable { 42 }
        val ttlCallable = TtlCallable.get(call, true)!!
        ttlCallable.callable shouldBeSameInstanceAs call

        executorService.submit(ttlCallable).getForTest() shouldBe 42

        val exception = shouldThrow<ExecutionException> {
            executorService.submit(ttlCallable).getForTest()
        }
        exception.cause.shouldBeTypeOf<IllegalStateException>()
        exception.message shouldContain "TTL value reference is released after call!"
    }

    test("get same") {
        val call = Callable { 42 }
        val ttlCallable = TtlCallable.get(call)!!
        ttlCallable.callable shouldBeSameInstanceAs call
    }

    test("get idempotent") {
        val call = TtlCallable.get { 42 }

        shouldThrow<IllegalStateException> {
            TtlCallable.get(call)
        }.message shouldContain "Already TtlCallable"
    }

    test("get null input") {
        TtlCallable.get<Any>(null).shouldBeNull()
    }

    test("gets") {
        val call1 = Callable { 1 }
        val call2 = Callable { 2 }
        val call3 = Callable { 3 }

        val callList = TtlCallable.gets(
            listOf(call1, call2, null, call3)
        )

        callList.shouldHaveSize(4)
        callList[0].shouldBeTypeOf<TtlCallable<*>>()
        callList[1].shouldBeTypeOf<TtlCallable<*>>()
        callList[2].shouldBeNull()
        callList[3].shouldBeTypeOf<TtlCallable<*>>()
    }

    test("unwrap") {
        TtlCallable.unwrap<String>(null).shouldBeNull()

        val callable = Callable { "hello" }
        val ttlCallable = TtlCallable.get(callable)


        TtlCallable.unwrap(callable) shouldBe callable
        TtlCallable.unwrap(ttlCallable) shouldBe callable

        TtlWrappers.unwrap(callable) shouldBe callable
        TtlWrappers.unwrap(ttlCallable) shouldBe callable


        TtlCallable.unwraps(listOf(callable)).shouldContainInOrder(callable)
        TtlCallable.unwraps(listOf(ttlCallable)).shouldContainInOrder(callable)

        TtlCallable.unwraps(listOf(ttlCallable, callable)).shouldContainInOrder(callable, callable)
        TtlCallable.unwraps<String>(null).shouldBeEmpty()
    }
})
