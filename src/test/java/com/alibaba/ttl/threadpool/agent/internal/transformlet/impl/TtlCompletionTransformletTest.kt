package com.alibaba.ttl.threadpool.agent.internal.transformlet.impl

import com.alibaba.noTtlAgentRun
import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.ttl.threadpool.agent.internal.logging.Logger
import com.alibaba.ttl.threadpool.agent.internal.transformlet.ClassInfo
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.core.test.config.TestCaseConfig
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import javassist.bytecode.ClassFile
import javassist.bytecode.ConstPool
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier

/**
 * Tests for [TtlCompletionTransformlet], which instruments the `CompletableFuture$Completion`
 * subclasses that hold user code (`UniApply`, `BiApply`, ...) so they replay the captured TTL
 * context no matter whether they are triggered via `doExec` or `tryFire`.
 */
class TtlCompletionTransformletTest : AnnotationSpec() {
    /**
     * when run unit test under TTL agent,
     * javassist is repackaged and excluded.
     *
     * skip this test case.
     */
    @Suppress("OVERRIDE_DEPRECATION")
    override fun defaultTestCaseConfig(): TestCaseConfig = TestCaseConfig(enabled = noTtlAgentRun())

    /**
     * the transformlets hold a `static final Logger`, and [Logger.getLogger] throws unless the
     * implementation type has been chosen (normally done by `TtlAgent.premain`).
     */
    @BeforeAll
    fun beforeAll() {
        Logger.setLoggerImplTypeIfNotSetYet("stderr")
    }

    @Test
    fun doTransform_instruments_functional_completion_subclasses() {
        // UniComposeExceptionally only exists on JDK 12+; filter to whatever this JDK actually has.
        val simpleNames = listOf(
            "UniApply", "UniAccept", "UniRun", "UniWhenComplete", "UniHandle", "UniExceptionally",
            "UniComposeExceptionally", "UniCompose", "BiApply", "BiAccept", "BiRun",
            "OrApply", "OrAccept", "OrRun"
        )
        val classNames = simpleNames.map { CF + it }.filter { isClassReadable(it) }

        classNames.shouldNotBeEmpty()
        listOf("UniApply", "UniAccept", "UniRun", "BiApply", "UniWhenComplete", "UniHandle").forEach {
            classNames shouldContain (CF + it)
        }

        for (className in classNames) {
            withClue(className) {
                val classInfo = ClassInfo(className, classBytes(className), null)

                newTransformlet().doTransform(classInfo)

                classInfo.isModified shouldBe true
                classInfo.ctClass.interfaces.map { it.name } shouldContain
                    TtlCompletionTransformlet.Enhanced::class.java.name
                classInfo.ctClass.toBytecode().isEmpty() shouldBe false
            }
        }
    }

    @Test
    fun doTransform_leaves_functional_field_less_completion_subclasses_unmodified() {
        // these run no user code, so TTL's plain doExec instrumentation must remain in effect
        val simpleNames = listOf("Completion", "UniCompletion", "UniRelay", "BiCompletion", "CoCompletion", "Signaller", "AnyOf")
        val classNames = simpleNames.map { CF + it }.filter { isClassReadable(it) }

        classNames.shouldNotBeEmpty()

        for (className in classNames) {
            withClue(className) {
                val classInfo = ClassInfo(className, classBytes(className), null)

                newTransformlet().doTransform(classInfo)

                classInfo.isModified shouldBe false
            }
        }
    }

    @Test
    fun doTransform_ignores_non_completion_classes() {
        val classNames = listOf(
            CF + "AsyncSupply",
            CF + "AsyncRun",
            "java.util.concurrent.CompletableFuture",
            "java.util.concurrent.ForkJoinTask",
            "java.util.TimerTask"
        )

        for (className in classNames) {
            withClue(className) {
                val classInfo = ClassInfo(className, classBytes(className), null)

                newTransformlet().doTransform(classInfo)

                classInfo.isModified shouldBe false
            }
        }
    }

    @Test
    fun doTransform_injects_the_type_specific_wrap_overload() {
        data class Case(val simpleName: String, val expectedDescriptor: String)

        val cases = listOf(
            Case("UniApply", "(Ljava/util/function/Function;Ljava/lang/Object;)Ljava/util/function/Function;"),
            Case("UniRun", "(Ljava/lang/Runnable;Ljava/lang/Object;)Ljava/lang/Runnable;"),
            Case("UniWhenComplete", "(Ljava/util/function/BiConsumer;Ljava/lang/Object;)Ljava/util/function/BiConsumer;")
        )

        for ((simpleName, expectedDescriptor) in cases) {
            withClue(simpleName) {
                val className = CF + simpleName
                val classInfo = ClassInfo(className, classBytes(className), null)

                newTransformlet().doTransform(classInfo)

                val bytecode = classInfo.ctClass.toBytecode()
                val descriptors = wrapMethodDescriptors(bytecode)

                // this is a regression guard: an (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
                // descriptor would mean the generic/Object wrap overload got called instead, which
                // silently does not replay the captured context
                descriptors shouldContain expectedDescriptor
            }
        }
    }

    @Test
    fun findFunctionalField_returns_the_fn_field_or_null() {
        val uniApplyName = CF + "UniApply"
        val uniApply = ClassInfo(uniApplyName, classBytes(uniApplyName), null).ctClass
        val fnField = TtlCompletionTransformlet.findFunctionalField(uniApply)

        fnField.shouldNotBeNull()
        fnField.name shouldBe "fn"
        fnField.type.name shouldBe "java.util.function.Function"

        val uniRelayName = CF + "UniRelay"
        val uniRelay = ClassInfo(uniRelayName, classBytes(uniRelayName), null).ctClass
        TtlCompletionTransformlet.findFunctionalField(uniRelay).shouldBeNull()
    }

    @Test
    fun isCompletionSubclass_true_for_completion_subclass_false_otherwise() {
        val uniApplyName = CF + "UniApply"
        val uniApply = ClassInfo(uniApplyName, classBytes(uniApplyName), null).ctClass
        TtlCompletionTransformlet.isCompletionSubclass(uniApply) shouldBe true

        val asyncSupplyName = CF + "AsyncSupply"
        val asyncSupply = ClassInfo(asyncSupplyName, classBytes(asyncSupplyName), null).ctClass
        TtlCompletionTransformlet.isCompletionSubclass(asyncSupply) shouldBe false
    }

    @Test
    fun wrap_Runnable_replays_captured_context_and_restores_after_run() {
        val ttl = TransmittableThreadLocal<String>()
        ttl.set("captured")
        val captured = TransmittableThreadLocal.Transmitter.capture()
        ttl.set("current")

        var observed: String? = null
        val wrapped = TtlCompletionTransformlet.wrap(Runnable { observed = ttl.get() }, captured)

        wrapped.run()

        observed shouldBe "captured"
        ttl.get() shouldBe "current"
        ttl.remove()
    }

    @Test
    fun wrap_Runnable_restores_even_when_the_wrapped_fn_throws() {
        val ttl = TransmittableThreadLocal<String>()
        ttl.set("captured")
        val captured = TransmittableThreadLocal.Transmitter.capture()
        ttl.set("current")

        val wrapped = TtlCompletionTransformlet.wrap(Runnable { throw RuntimeException("boom") }, captured)

        shouldThrow<RuntimeException> { wrapped.run() }

        ttl.get() shouldBe "current"
        ttl.remove()
    }

    @Test
    fun wrap_Function_replays_restores_and_passes_argument_and_return_value_through() {
        val ttl = TransmittableThreadLocal<String>()
        ttl.set("captured")
        val captured = TransmittableThreadLocal.Transmitter.capture()
        ttl.set("current")

        var observed: String? = null
        val wrapped = TtlCompletionTransformlet.wrap(Function<String, String> { arg -> observed = ttl.get(); "$arg!" }, captured)

        wrapped.apply("hi") shouldBe "hi!"
        observed shouldBe "captured"
        ttl.get() shouldBe "current"
        ttl.remove()
    }

    @Test
    fun wrap_Consumer_replays_restores_and_passes_argument_through() {
        val ttl = TransmittableThreadLocal<String>()
        ttl.set("captured")
        val captured = TransmittableThreadLocal.Transmitter.capture()
        ttl.set("current")

        var observedArg: String? = null
        var observedTtl: String? = null
        val wrapped = TtlCompletionTransformlet.wrap(
            Consumer<String> { arg -> observedArg = arg; observedTtl = ttl.get() },
            captured
        )

        wrapped.accept("hi")

        observedArg shouldBe "hi"
        observedTtl shouldBe "captured"
        ttl.get() shouldBe "current"
        ttl.remove()
    }

    @Test
    fun wrap_BiFunction_replays_restores_and_passes_arguments_and_return_value_through() {
        val ttl = TransmittableThreadLocal<String>()
        ttl.set("captured")
        val captured = TransmittableThreadLocal.Transmitter.capture()
        ttl.set("current")

        var observed: String? = null
        val wrapped = TtlCompletionTransformlet.wrap(
            BiFunction<String, String, String> { a, b -> observed = ttl.get(); "$a-$b" },
            captured
        )

        wrapped.apply("x", "y") shouldBe "x-y"
        observed shouldBe "captured"
        ttl.get() shouldBe "current"
        ttl.remove()
    }

    @Test
    fun wrap_BiConsumer_replays_restores_and_passes_arguments_through() {
        val ttl = TransmittableThreadLocal<String>()
        ttl.set("captured")
        val captured = TransmittableThreadLocal.Transmitter.capture()
        ttl.set("current")

        var observedArgs: Pair<String, String>? = null
        var observedTtl: String? = null
        val wrapped = TtlCompletionTransformlet.wrap(
            BiConsumer<String, String> { a, b -> observedArgs = a to b; observedTtl = ttl.get() },
            captured
        )

        wrapped.accept("x", "y")

        observedArgs shouldBe ("x" to "y")
        observedTtl shouldBe "captured"
        ttl.get() shouldBe "current"
        ttl.remove()
    }

    @Test
    fun every_supported_functional_interface_has_a_dedicated_wrap_overload() {
        // The catch-all wrap(T, Object) hands fn back unwrapped, so a type listed as supported but
        // missing a real overload would silently stop replaying context for that completion stage.
        // findFunctionalField matches on this list, so the two must stay in lockstep.
        val overloadFirstParameterTypes = TtlCompletionTransformlet::class.java.methods
            .filter { it.name == "wrap" }
            .map { it.parameterTypes[0].name }

        TtlCompletionTransformlet.supportedFunctionalInterfaces.shouldNotBeEmpty()
        for (functionalInterface in TtlCompletionTransformlet.supportedFunctionalInterfaces) {
            withClue(functionalInterface) {
                overloadFirstParameterTypes shouldContain functionalInterface
            }
        }
    }

    @Test
    fun findFunctionalField_ignores_functional_types_without_a_wrap_overload() {
        // AsyncSupply holds `Supplier<? extends T> fn`: a @FunctionalInterface, but one with no wrap
        // overload. Matching it would route through the catch-all and silently not replay, so the
        // whitelist must reject it. (AsyncSupply is not a Completion either, but findFunctionalField
        // is the layer that has to be type-safe here.)
        val className = CF + "AsyncSupply"
        val asyncSupply = ClassInfo(className, classBytes(className), null).ctClass

        withClue("precondition: AsyncSupply.fn is still a Supplier on this JDK") {
            asyncSupply.getDeclaredField("fn").type.name shouldBe "java.util.function.Supplier"
        }
        TtlCompletionTransformlet.findFunctionalField(asyncSupply).shouldBeNull()
    }

    @Test
    fun catch_all_wrap_returns_the_function_unwrapped() {
        val ttl = TransmittableThreadLocal<String>()
        ttl.set("captured")
        val captured = TransmittableThreadLocal.Transmitter.capture()
        ttl.set("current")

        // Supplier has no dedicated overload, so this binds to the catch-all wrap(T, Object)
        val fn = Supplier { ttl.get() }
        val wrapped: Supplier<String> = TtlCompletionTransformlet.wrap(fn, captured)

        withClue("the catch-all must hand back the original instance, not a replaying wrapper") {
            wrapped shouldBeSameInstanceAs fn
        }
        withClue("and so must NOT replay the captured context -- it logs at SEVERE instead") {
            wrapped.get() shouldBe "current"
        }
        ttl.remove()
    }
}

private const val CF = "java.util.concurrent.CompletableFuture$"

private fun newTransformlet(): TtlCompletionTransformlet {
    return TtlCompletionTransformlet()
}

private fun classBytes(binaryName: String): ByteArray =
    ClassLoader.getSystemResourceAsStream(binaryName.replace('.', '/') + ".class")!!.use { it.readBytes() }

private fun isClassReadable(binaryName: String): Boolean =
    ClassLoader.getSystemResourceAsStream(binaryName.replace('.', '/') + ".class")?.also { it.close() } != null

/**
 * The methodref descriptors of every constant-pool `wrap` method reference in [classBytecode],
 * i.e. which overload of [TtlCompletionTransformlet.wrap] the injected code actually resolved to.
 */
private fun wrapMethodDescriptors(classBytecode: ByteArray): List<String> {
    val classFile = ClassFile(DataInputStream(ByteArrayInputStream(classBytecode)))
    val cp = classFile.constPool

    val descriptors = mutableListOf<String>()
    for (i in 1 until cp.size) {
        try {
            if (cp.getTag(i) == ConstPool.CONST_Methodref && cp.getMethodrefName(i) == "wrap") {
                descriptors += cp.getMethodrefType(i)
            }
        } catch (expected: Exception) {
            // some constant-pool slots (e.g. the second half of a Long/Double entry) are not
            // readable via these accessors; skip them
        }
    }
    return descriptors
}
