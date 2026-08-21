package com.alibaba.ttl.threadpool.agent

import com.alibaba.expandThreadPool
import com.alibaba.hasTtlAgentRun
import com.alibaba.noTtlAgentRun
import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.ttl.threadpool.agent.internal.transformlet.JavassistTransformlet
import com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.TtlCompletionTransformlet
import com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.TtlForkJoinTransformlet
import io.kotest.assertions.withClue
import java.lang.instrument.Instrumentation
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.core.test.config.TestCaseConfig
import io.kotest.matchers.shouldBe
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask
import java.util.concurrent.RecursiveTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.AtomicReferenceArray

private const val CREATOR = "creator"
private const val COMPLETER = "completer"
private const val MUTATED_BY_STAGE = "mutated-by-stage"
private const val COMPLETER_ONLY = "completer-only"
private const val NOT_RUN = "not-run"
private const val TIMEOUT_SECONDS = 5L
private const val CHAIN_DEPTH = 10
private const val CF_PREFIX = "java.util.concurrent.CompletableFuture$"
private const val CF_ASYNC_SUPPLY = CF_PREFIX + "AsyncSupply"

/**
 * Regression tests for the `CompletableFuture` completion-stage context propagation bug fixed by
 * [TtlCompletionTransformlet].
 *
 * `CompletableFuture` completion stages (`thenApply`, `thenAccept`, ...) extend `ForkJoinTask`, and
 * TTL normally captures context when a `ForkJoinTask` is constructed and replays it in `doExec`.
 * But `CompletableFuture.postComplete()` fires most stages by calling `Completion.tryFire(...)`
 * *directly*, bypassing `doExec` entirely -- so, before the fix, a stage ran with whichever
 * thread's context happened to complete the future, instead of the context captured when the
 * stage itself was created.
 *
 * These tests create a stage while a [TransmittableThreadLocal] is set to [CREATOR] on the
 * current (Kotest) thread, then complete the underlying future from a *different* thread whose
 * own value is [COMPLETER]. Without the agent (and without the fix), the stage would observe
 * [COMPLETER]; with the fix it must observe [CREATOR]. This whole spec is meaningless without the
 * agent attached, so it is disabled outside the `-Penable-ttl-agent-for-test` run.
 *
 * @see TtlCompletionTransformlet
 */
class CompletableFutureTtlTest : AnnotationSpec() {
    @Suppress("OVERRIDE_DEPRECATION")
    override fun defaultTestCaseConfig(): TestCaseConfig = TestCaseConfig(enabled = hasTtlAgentRun())

    private lateinit var ttl: TransmittableThreadLocal<String>

    @BeforeEach
    fun setUp() {
        ttl = TransmittableThreadLocal()
    }

    @AfterEach
    fun tearDown() {
        ttl.remove()
    }

    /**
     * Runs [completeAction] on a fresh thread that first sets [ttl] to [COMPLETER], then joins
     * that thread (bounded by [TIMEOUT_SECONDS], so a regression hangs the suite for seconds, not
     * forever) and asserts the completer thread's *own* value was [COMPLETER] right up to the end
     * of [completeAction] -- i.e. that replaying the creator's captured context into the stage did
     * not leak into, or fail to be restored on, the thread that actually completed the future.
     */
    private fun completeOnOtherThread(completeAction: () -> Unit) {
        val completerValueAfterComplete = AtomicReference<String?>()
        val completerThread = Thread {
            ttl.set(COMPLETER)
            completeAction()
            completerValueAfterComplete.set(ttl.get())
        }
        completerThread.start()
        completerThread.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS))

        withClue("completer thread must have finished within $TIMEOUT_SECONDS seconds") {
            completerThread.isAlive shouldBe false
        }
        withClue("completer thread's own TTL value must be restored (not leaked into) after completing the future") {
            completerValueAfterComplete.get() shouldBe COMPLETER
        }
    }

    @Test
    fun thenApply_sees_context_captured_at_stage_creation() {
        ttl.set(CREATOR)
        val future = CompletableFuture<String>()
        val stage = future.thenApply { v -> "$v:${ttl.get()}" }

        completeOnOtherThread { future.complete("value") }

        stage.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) shouldBe "value:$CREATOR"
    }

    @Test
    fun thenAccept_sees_context_captured_at_stage_creation() {
        ttl.set(CREATOR)
        val observed = AtomicReference<String?>()
        val future = CompletableFuture<String>()
        val stage = future.thenAccept { observed.set(ttl.get()) }

        completeOnOtherThread { future.complete("value") }

        stage.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        observed.get() shouldBe CREATOR
    }

    @Test
    fun thenRun_sees_context_captured_at_stage_creation() {
        ttl.set(CREATOR)
        val observed = AtomicReference<String?>()
        val future = CompletableFuture<String>()
        val stage = future.thenRun { observed.set(ttl.get()) }

        completeOnOtherThread { future.complete("value") }

        stage.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        observed.get() shouldBe CREATOR
    }

    @Test
    fun whenComplete_sees_context_captured_at_stage_creation() {
        ttl.set(CREATOR)
        val observed = AtomicReference<String?>()
        val future = CompletableFuture<String>()
        val stage = future.whenComplete { _, _ -> observed.set(ttl.get()) }

        completeOnOtherThread { future.complete("value") }

        stage.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        observed.get() shouldBe CREATOR
    }

    @Test
    fun handle_sees_context_captured_at_stage_creation() {
        ttl.set(CREATOR)
        val future = CompletableFuture<String>()
        val stage = future.handle { v, _ -> "$v:${ttl.get()}" }

        completeOnOtherThread { future.complete("value") }

        stage.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) shouldBe "value:$CREATOR"
    }

    @Test
    fun thenCompose_sees_context_captured_at_stage_creation() {
        ttl.set(CREATOR)
        val future = CompletableFuture<String>()
        val stage = future.thenCompose { v -> CompletableFuture.completedFuture("$v:${ttl.get()}") }

        completeOnOtherThread { future.complete("value") }

        stage.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) shouldBe "value:$CREATOR"
    }

    @Test
    fun thenCombine_sees_context_captured_at_stage_creation() {
        ttl.set(CREATOR)
        val future = CompletableFuture<String>()
        val other = CompletableFuture.completedFuture("other")
        val stage = future.thenCombine(other) { v, o -> "$v:$o:${ttl.get()}" }

        completeOnOtherThread { future.complete("value") }

        stage.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) shouldBe "value:other:$CREATOR"
    }

    @Test
    fun exceptionally_sees_context_captured_at_stage_creation() {
        ttl.set(CREATOR)
        val future = CompletableFuture<String>()
        val stage = future.exceptionally { "handled:${ttl.get()}" }

        completeOnOtherThread { future.completeExceptionally(RuntimeException("boom")) }

        stage.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) shouldBe "handled:$CREATOR"
    }

    @Test
    fun thenApplyAsync_with_explicit_executor_still_sees_context_and_is_not_broken_by_the_fix() {
        val executor: ExecutorService = Executors.newFixedThreadPool(4)
        try {
            expandThreadPool(executor)

            ttl.set(CREATOR)
            val future = CompletableFuture<String>()
            val stage = future.thenApplyAsync({ v -> "$v:${ttl.get()}" }, executor)

            completeOnOtherThread { future.complete("value") }

            // this variant fires via ForkJoinTask#doExec (not the direct tryFire path), so this
            // guards against the fix double-replaying (or otherwise breaking) that existing path.
            stage.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) shouldBe "value:$CREATOR"
        } finally {
            executor.shutdown()
        }
    }

    @Test
    fun thenApplyAsync_with_default_ForkJoinPool_still_sees_context_and_is_not_broken_by_the_fix() {
        ttl.set(CREATOR)
        val future = CompletableFuture<String>()
        val stage = future.thenApplyAsync { v -> "$v:${ttl.get()}" }

        completeOnOtherThread { future.complete("value") }

        // this variant fires via ForkJoinTask#doExec on the common pool (not the direct tryFire
        // path), so this guards against the fix double-replaying (or otherwise breaking) it.
        stage.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) shouldBe "value:$CREATOR"
    }

    @Test
    fun mutating_ttl_after_stage_creation_but_before_completion_is_not_visible_to_the_stage() {
        ttl.set(CREATOR)
        val future = CompletableFuture<String>()
        val stage = future.thenApply { v -> "$v:${ttl.get()}" }

        // mutate on the creator thread, strictly after the stage was created: this must not be
        // visible to the stage function, since capture happens at construction time.
        ttl.set("mutated-after-stage-creation")

        completeOnOtherThread { future.complete("value") }

        withClue("stage must see the value captured at stage-creation time, not a later mutation") {
            stage.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) shouldBe "value:$CREATOR"
        }
    }

    @Test
    fun chained_thenApply_stages_all_see_the_creator_context() {
        ttl.set(CREATOR)
        val future = CompletableFuture<String>()
        val stage = future
            .thenApply { v -> "$v:${ttl.get()}" }
            .thenApply { v -> "$v:${ttl.get()}" }

        completeOnOtherThread { future.complete("value") }

        stage.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) shouldBe "value:$CREATOR:$CREATOR"
    }

    /**
     * A [CHAIN_DEPTH]-deep chain in which the creator writes a *different* value before creating
     * each stage, and every stage then overwrites the context from inside its own body.
     *
     * Each stage captures at its own construction, so each must observe its own creator value --
     * not the one the previous stage wrote, and not the one the creator ended up with. The whole
     * chain fires on the completing thread, nested inside a single `postComplete()`, so this also
     * pins down that [CHAIN_DEPTH] replay/restore pairs unwind in order and leave that thread clean.
     */
    @Test
    fun a_long_chain_where_every_stage_mutates_the_context() {
        val seenByStage = AtomicReferenceArray<String?>(CHAIN_DEPTH)

        val future = CompletableFuture<String>()
        var stage: CompletableFuture<String> = future
        for (i in 0 until CHAIN_DEPTH) {
            // the creator context differs for every stage, so "each stage sees its own capture" and
            // "every stage sees the creator's latest value" are distinguishable outcomes
            ttl.set("$CREATOR-$i")
            stage = stage.thenApply { v ->
                seenByStage.set(i, ttl.get())
                // ...and every stage clobbers the context it was given
                ttl.set("$MUTATED_BY_STAGE-$i")
                "$v>$i"
            }
        }
        val creatorValueAfterBuildingTheChain = ttl.get()

        // asserts the completing thread is back to COMPLETER once all CHAIN_DEPTH stages have run
        completeOnOtherThread { future.complete("start") }

        stage.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) shouldBe
            (0 until CHAIN_DEPTH).joinToString(separator = "", prefix = "start") { ">$it" }

        for (i in 0 until CHAIN_DEPTH) {
            withClue("stage #$i must see the context captured when *it* was created") {
                seenByStage.get(i) shouldBe "$CREATOR-$i"
            }
        }
        withClue("the creator thread must be untouched by the whole chain") {
            ttl.get() shouldBe creatorValueAfterBuildingTheChain
        }
    }

    @Test
    fun a_ttl_written_by_the_stage_does_not_leak_into_the_completing_thread() {
        ttl.set(CREATOR)
        val future = CompletableFuture<String>()
        val stage = future.thenApply { v ->
            // the stage body runs under the *replayed* creator context; whatever it writes there
            // must be undone by the restore, rather than left behind on the thread that fired it
            ttl.set(MUTATED_BY_STAGE)
            "$v:${ttl.get()}"
        }

        // completeOnOtherThread asserts the completing thread is back to COMPLETER afterwards --
        // which, unlike the tests above, the restore now has real work to undo
        completeOnOtherThread { future.complete("value") }

        withClue("the stage must observe its own write while it is running") {
            stage.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) shouldBe "value:$MUTATED_BY_STAGE"
        }
        withClue("the creator thread must be unaffected by what the stage wrote") {
            ttl.get() shouldBe CREATOR
        }
    }

    @Test
    fun a_ttl_set_only_on_the_completing_thread_is_not_visible_to_the_stage() {
        // this TTL is never set on the creator thread, so it is absent from the snapshot captured
        // at stage creation. Replaying that snapshot must *clear* it for the duration of the stage
        // -- propagation is "install the captured context", not "add to whatever is already there".
        val completerOnlyTtl = TransmittableThreadLocal<String>()
        try {
            ttl.set(CREATOR)
            val observedInStage = AtomicReference<String?>(NOT_RUN)
            val observedOnCompleterAfterwards = AtomicReference<String?>()

            val future = CompletableFuture<String>()
            val stage = future.thenApply { v ->
                observedInStage.set(completerOnlyTtl.get())
                v
            }

            val completerThread = Thread {
                ttl.set(COMPLETER)
                completerOnlyTtl.set(COMPLETER_ONLY)
                future.complete("value")
                observedOnCompleterAfterwards.set(completerOnlyTtl.get())
            }
            completerThread.start()
            completerThread.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS))
            completerThread.isAlive shouldBe false

            stage.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) shouldBe "value"
            withClue("a TTL absent from the captured snapshot must not be visible inside the stage") {
                observedInStage.get() shouldBe null
            }
            withClue("...but must be restored on the completing thread once the stage is done") {
                observedOnCompleterAfterwards.get() shouldBe COMPLETER_ONLY
            }
        } finally {
            completerOnlyTtl.remove()
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    // The fix makes ForkJoinTask#doExec skip its replay for anything marked
    // TtlCompletionTransformlet.Enhanced. These guard the other side of that branch:
    // everything NOT marked must still get the plain doExec replay it had before.
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * A plain [ForkJoinTask] is never marked [TtlCompletionTransformlet.Enhanced], so `doExec` must
     * still replay for it.
     *
     * Unlike a `CompletableFuture` stage, a task can be *constructed* and *submitted* from different
     * threads, which is what makes this discriminating: the task captures [CREATOR] at construction,
     * is submitted from a thread holding [COMPLETER], and runs on a pool worker holding neither.
     */
    @Test
    fun a_plain_fork_join_task_is_not_enhanced_and_still_replays_via_doExec() {
        withClue("a plain ForkJoinTask must not be marked Enhanced -- it needs the doExec replay") {
            TtlCompletionTransformlet.Enhanced::class.java.isAssignableFrom(ReadTtlTask::class.java) shouldBe false
        }

        val pool = ForkJoinPool(2)
        try {
            ttl.set(CREATOR)
            // constructed here, so ForkJoinTask's captured field holds CREATOR
            val task = ReadTtlTask(ttl)

            val computed = AtomicReference<String?>(NOT_RUN)
            val submitter = Thread {
                ttl.set(COMPLETER)
                computed.set(pool.invoke(task))
            }
            submitter.start()
            submitter.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS))
            submitter.isAlive shouldBe false

            withClue("doExec must replay the context captured when the task was constructed") {
                computed.get() shouldBe CREATOR
            }
        } finally {
            pool.shutdown()
        }
    }

    /**
     * `supplyAsync` runs an `AsyncSupply`, which extends [ForkJoinTask] but is *not* a `Completion`
     * -- so it is not marked [TtlCompletionTransformlet.Enhanced] and keeps the `doExec` replay.
     */
    @Test
    fun asyncSupply_is_not_enhanced_and_still_replays_via_doExec() {
        withClue("AsyncSupply is a ForkJoinTask but not a Completion, so it must keep the doExec replay") {
            TtlCompletionTransformlet.Enhanced::class.java
                .isAssignableFrom(Class.forName(CF_ASYNC_SUPPLY)) shouldBe false
        }

        val pool = ForkJoinPool(1)
        try {
            // create the pool's worker from a thread holding COMPLETER: the worker inherits that
            // (or, with "disable inheritable" enabled, nothing) -- either way not CREATOR, so a
            // missing replay cannot masquerade as a passing test
            val warmUp = Thread {
                ttl.set(COMPLETER)
                pool.submit { }.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            }
            warmUp.start()
            warmUp.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS))
            warmUp.isAlive shouldBe false

            ttl.set(CREATOR)
            val stage = CompletableFuture.supplyAsync({ ttl.get() }, pool)

            withClue("doExec must replay the context captured when the AsyncSupply was constructed") {
                stage.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) shouldBe CREATOR
            }
        } finally {
            pool.shutdown()
        }
    }
}

private class ReadTtlTask(private val ttl: TransmittableThreadLocal<String>) : RecursiveTask<String>() {
    override fun compute(): String? = ttl.get()
}

/**
 * Verifies the structural invariant: if [TtlCompletionTransformlet] is in the transformlet list
 * built by [TtlAgent.premain], then [TtlForkJoinTransformlet] must also be present.
 *
 * Calls [TtlAgent.premain] with a mock [Instrumentation], captures the [TtlTransformer] passed
 * to [Instrumentation.addTransformer], and reflectively reads its transformlet list.
 */
class TtlCompletionTransformletRequiresForkJoinTransformletTest : AnnotationSpec() {
    @Suppress("OVERRIDE_DEPRECATION")
    override fun defaultTestCaseConfig(): TestCaseConfig = TestCaseConfig(enabled = noTtlAgentRun())

    @Test
    fun premain_registers_ForkJoinTransformlet_when_CompletionTransformlet_is_enabled() {
        val transformlets = callPremainAndGetTransformletList(
            "ttl.agent.logger:STDERR,ttl.agent.enable.completion.transformlet:true"
        )

        val hasCompletion = transformlets.any { it is TtlCompletionTransformlet }
        val hasForkJoin = transformlets.any { it is TtlForkJoinTransformlet }

        withClue("completion transformlet should be registered when explicitly enabled") {
            hasCompletion shouldBe true
        }
        withClue("ForkJoinTransformlet must be present whenever CompletionTransformlet is") {
            hasForkJoin shouldBe true
        }
    }

    @Test
    fun premain_still_registers_ForkJoinTransformlet_when_CompletionTransformlet_is_disabled() {
        val transformlets = callPremainAndGetTransformletList(
            "ttl.agent.logger:STDERR,ttl.agent.enable.completion.transformlet:false"
        )

        val hasCompletion = transformlets.any { it is TtlCompletionTransformlet }
        val hasForkJoin = transformlets.any { it is TtlForkJoinTransformlet }

        withClue("completion transformlet should not be registered when disabled") {
            hasCompletion shouldBe false
        }
        withClue("ForkJoinTransformlet must always be present regardless of completion flag") {
            hasForkJoin shouldBe true
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun callPremainAndGetTransformletList(agentArgs: String): List<JavassistTransformlet> {
        // Reset Logger's impl type so premain can set it again
        val loggerImplTypeField = com.alibaba.ttl.threadpool.agent.internal.logging.Logger::class.java
            .getDeclaredField("loggerImplType")
        loggerImplTypeField.isAccessible = true
        loggerImplTypeField.setInt(null, -1)

        // Reset ttlAgentLoaded so premain doesn't short-circuit
        val loadedField = TtlAgent::class.java.getDeclaredField("ttlAgentLoaded")
        loadedField.isAccessible = true
        loadedField.setBoolean(null, false)

        var capturedTransformer: java.lang.instrument.ClassFileTransformer? = null
        val mockInst = java.lang.reflect.Proxy.newProxyInstance(
            Instrumentation::class.java.classLoader,
            arrayOf(Instrumentation::class.java)
        ) { _, method, args ->
            if (method.name == "addTransformer") {
                capturedTransformer = args[0] as java.lang.instrument.ClassFileTransformer
            }
            null
        } as Instrumentation

        TtlAgent.premain(agentArgs, mockInst)

        val transformer = capturedTransformer!!
        val field = TtlTransformer::class.java.getDeclaredField("transformletList")
        field.isAccessible = true
        return field.get(transformer) as List<JavassistTransformlet>
    }
}
