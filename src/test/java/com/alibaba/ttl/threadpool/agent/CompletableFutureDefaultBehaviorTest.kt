package com.alibaba.ttl.threadpool.agent

import com.alibaba.noTtlAgentRun
import com.alibaba.ttl.TransmittableThreadLocal
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.core.test.config.TestCaseConfig
import io.kotest.matchers.shouldBe
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

private const val CREATOR = "creator"
private const val COMPLETER = "completer"
private const val TIMEOUT_SECONDS = 5L

/**
 * Asserts the **default** (broken) behavior when the completion transformlet is not active:
 * completion stages triggered via the `tryFire` path see the completing thread's context instead
 * of the context captured when the stage was created.
 *
 * This is the counterpart to [CompletableFutureTtlTest], which asserts the fixed behavior.
 * Together they document both sides of the contract:
 * - without the fix (this class): stages see [COMPLETER]
 * - with the fix ([CompletableFutureTtlTest]): stages see [CREATOR]
 *
 * These tests run only in the plain (no-agent) surefire execution, which is equivalent to having
 * the agent loaded with `ttl.agent.enable.completion.transformlet:false` for the tryFire path:
 * neither configuration wraps the `fn` field, so `postComplete` fires the raw closure on the
 * completing thread's context.
 */
class CompletableFutureDefaultBehaviorTest : AnnotationSpec() {
    @Suppress("OVERRIDE_DEPRECATION")
    override fun defaultTestCaseConfig(): TestCaseConfig = TestCaseConfig(enabled = noTtlAgentRun())

    private lateinit var ttl: TransmittableThreadLocal<String>

    @BeforeEach
    fun setUp() {
        ttl = TransmittableThreadLocal()
    }

    @AfterEach
    fun tearDown() {
        ttl.remove()
    }

    private fun completeOnOtherThread(completeAction: () -> Unit) {
        val thread = Thread {
            ttl.set(COMPLETER)
            completeAction()
        }
        thread.start()
        thread.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS))
        withClue("completer thread must have finished") {
            thread.isAlive shouldBe false
        }
    }

    @Test
    fun thenApply_sees_completer_context_without_completion_transformlet() {
        ttl.set(CREATOR)
        val future = CompletableFuture<String>()
        val stage = future.thenApply { ttl.get() }

        completeOnOtherThread { future.complete("x") }

        withClue("without the completion transformlet, tryFire runs under the completer's context") {
            stage.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) shouldBe COMPLETER
        }
    }

    @Test
    fun thenAccept_sees_completer_context_without_completion_transformlet() {
        ttl.set(CREATOR)
        val observed = AtomicReference<String?>()
        val future = CompletableFuture<String>()
        val stage = future.thenAccept { observed.set(ttl.get()) }

        completeOnOtherThread { future.complete("x") }

        stage.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        withClue("without the completion transformlet, tryFire runs under the completer's context") {
            observed.get() shouldBe COMPLETER
        }
    }

    @Test
    fun thenRun_sees_completer_context_without_completion_transformlet() {
        ttl.set(CREATOR)
        val observed = AtomicReference<String?>()
        val future = CompletableFuture<String>()
        val stage = future.thenRun { observed.set(ttl.get()) }

        completeOnOtherThread { future.complete("x") }

        stage.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        withClue("without the completion transformlet, tryFire runs under the completer's context") {
            observed.get() shouldBe COMPLETER
        }
    }

    @Test
    fun whenComplete_sees_completer_context_without_completion_transformlet() {
        ttl.set(CREATOR)
        val observed = AtomicReference<String?>()
        val future = CompletableFuture<String>()
        val stage = future.whenComplete { _, _ -> observed.set(ttl.get()) }

        completeOnOtherThread { future.complete("x") }

        stage.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        withClue("without the completion transformlet, tryFire runs under the completer's context") {
            observed.get() shouldBe COMPLETER
        }
    }

    @Test
    fun handle_sees_completer_context_without_completion_transformlet() {
        ttl.set(CREATOR)
        val future = CompletableFuture<String>()
        val stage = future.handle { _, _ -> ttl.get() }

        completeOnOtherThread { future.complete("x") }

        withClue("without the completion transformlet, tryFire runs under the completer's context") {
            stage.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) shouldBe COMPLETER
        }
    }

    @Test
    fun thenCompose_sees_completer_context_without_completion_transformlet() {
        ttl.set(CREATOR)
        val future = CompletableFuture<String>()
        val stage = future.thenCompose { CompletableFuture.completedFuture(ttl.get()) }

        completeOnOtherThread { future.complete("x") }

        withClue("without the completion transformlet, tryFire runs under the completer's context") {
            stage.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) shouldBe COMPLETER
        }
    }

    @Test
    fun exceptionally_sees_completer_context_without_completion_transformlet() {
        ttl.set(CREATOR)
        val future = CompletableFuture<String>()
        val stage = future.exceptionally { ttl.get() }

        completeOnOtherThread { future.completeExceptionally(RuntimeException("boom")) }

        withClue("without the completion transformlet, tryFire runs under the completer's context") {
            stage.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) shouldBe COMPLETER
        }
    }
}
