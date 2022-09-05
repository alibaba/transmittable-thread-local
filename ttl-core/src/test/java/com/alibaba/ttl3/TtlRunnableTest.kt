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
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldBeTypeOf
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.thread

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class TtlRunnableTest : FunSpec({
    lateinit var executorService: ExecutorService

    beforeSpec {
        executorService = Executors.newFixedThreadPool(3).also { expandThreadPool(it) }
    }

    afterSpec {
        executorService.shutdownForTest()
    }


    ttlFlowTest("run in current thread") { checkLogicInTask ->
        val ttlRunnable = TtlRunnable.get(checkLogicInTask)!!

        val taskRunner: TaskRunner = {
            // run in the *current* thread
            ttlRunnable.run()

            CompletableFuture.completedFuture(null)
        }
        taskRunner
    }

    ttlFlowTest("async run by new thread") { checkLogicInTask ->
        val ttlRunnable = TtlRunnable.get(checkLogicInTask)!!

        val taskRunner: TaskRunner = {
            val future = CompletableFuture<Void>()
            // run in new thread
            thread {
                ttlRunnable.run()
                future.complete(null)
            }
            future
        }
        taskRunner
    }

    ttlFlowTest("async run by executor service", captureTimeByAgentRun) { checkLogicInTask ->
        val runnable: Runnable = checkLogicInTask.toRunnable().ttlWrapIfNoTtlAgentRun()

        val taskRunner: TaskRunner = {
            executorService.submit(runnable)
        }
        taskRunner
    }

    test("release ttl value reference after run") {
        val ttlRunnable = TtlRunnable.get({ }, true)!!

        executorService.submit(ttlRunnable).getForTest().shouldBeNull()

        val exception = shouldThrow<ExecutionException> {
            executorService.submit(ttlRunnable).get()
        }
        exception.cause.shouldBeTypeOf<IllegalStateException>()
        exception.message shouldContain "TTL value reference is released after run!"
    }

    test("get same") {
        val task = Runnable {}
        val ttlRunnable = TtlRunnable.get(task)!!
        ttlRunnable.runnable shouldBeSameInstanceAs task
    }

    test("get idempotent") {
        val task = TtlRunnable.get {}

        shouldThrow<IllegalStateException> {
            TtlRunnable.get(task)
        }.message shouldContain "Already TtlRunnable"
    }

    test("get null input") {
        TtlRunnable.get(null).shouldBeNull()
    }

    test("gets") {
        val task1 = Runnable {}
        val task2 = Runnable {}
        val task3 = Runnable {}

        val taskList = TtlRunnable.gets(listOf(task1, task2, null, task3))

        taskList.shouldHaveSize(4)

        taskList[0].shouldBeTypeOf<TtlRunnable>()
        taskList[1].shouldBeTypeOf<TtlRunnable>()
        taskList[2].shouldBeNull()
        taskList[3].shouldBeTypeOf<TtlRunnable>()
    }

    test("unwrap") {
        TtlRunnable.unwrap(null).shouldBeNull()

        val runnable = Runnable {}
        val ttlRunnable = TtlRunnable.get(runnable)

        TtlRunnable.unwrap(runnable) shouldBeSameInstanceAs runnable
        TtlRunnable.unwrap(ttlRunnable) shouldBeSameInstanceAs runnable

        TtlWrappers.unwrap(runnable) shouldBeSameInstanceAs runnable
        TtlWrappers.unwrap(ttlRunnable) shouldBeSameInstanceAs runnable


        TtlRunnable.unwraps(listOf(runnable)).shouldContainInOrder(runnable)
        TtlRunnable.unwraps(listOf(ttlRunnable)).shouldContainInOrder(runnable)

        TtlRunnable.unwraps(listOf(ttlRunnable, runnable)).shouldContainInOrder(runnable, runnable)
        TtlRunnable.unwraps(null).shouldBeEmpty()
    }
})
