package com.alibaba.third_part_lib_test

import com.alibaba.noTtlAgentRun
import io.kotest.assertions.fail
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.core.test.config.TestCaseConfig
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import javassist.ClassPool
import javassist.CtClass

/**
 * [simplify the try-finally code gen by javassist, do not need copy method #115](https://github.com/alibaba/transmittable-thread-local/issues/115)
 */
class JavassistTest : AnnotationSpec() {
    /**
     * when run unit test under TTL agent,
     * javassist is repackaged and excluded.
     *
     * skip this test case.
     */
    override fun defaultTestCaseConfig(): TestCaseConfig = TestCaseConfig(enabled = noTtlAgentRun())

    @Test
    fun insertAfter_as_finally() {
        val classPool = ClassPool(true)
        val ctClass = classPool.getCtClass("com.alibaba.third_part_lib_test.DemoRunnable")
        // To execute it when an exception is thrown, the second parameter asFinally to insertAfter() must be true.
        ctClass.getDeclaredMethod("run", arrayOf()).insertAfter("value = 42;", true)

        val instance = ctClass.toClass().getDeclaredConstructor().newInstance()

        (instance as Supplier).get() shouldBe 0

        (instance as Runnable).let {
            try {
                it.run()
                fail("must not run to here")
            } catch (e: RuntimeException) {
                e.message shouldBe "Intended"
            }
        }

        (instance as Supplier).get() shouldBe 42
    }

    /**
     * more info see
     * - [Bad Bytecode when trying access localVariable using insertAfter and asFinally = true](https://issues.jboss.org/browse/JASSIST-232?_sscc=t)
     * - Javadoc of [javassist.CtBehavior.addLocalVariable]:
     * If the second parameter asFinally to insertAfter() is true, the declared local variable is not visible from the code inserted by insertAfter().
     */
    @Test
    fun insertAfter_as_finally_fail_with_local_var() {
        val classPool = ClassPool(true)
        val ctClass = classPool.getCtClass("com.alibaba.third_part_lib_test.DemoRunnable2")
        // To execute it when an exception is thrown, the second parameter asFinally to insertAfter() must be true.
        ctClass.getDeclaredMethod("run", arrayOf()).apply {
            addLocalVariable("var", CtClass.intType)
            insertBefore("var = 2;")
            insertAfter("value = 40 + var;", true)
        }


        shouldThrow<VerifyError> {
            (ctClass.toClass().getDeclaredConstructor().newInstance() as Runnable).run()
        }.message shouldContain "Bad local variable type"
    }
}

private interface Supplier {
    fun get(): Int
}

@Suppress("unused")
private class DemoRunnable : Runnable, Supplier {
    @Volatile
    private var value = 0

    override fun get(): Int = value

    override fun run() {
        throw RuntimeException("Intended")
    }
}

@Suppress("unused")
private class DemoRunnable2 : Runnable, Supplier {
    @Volatile
    private var value = 0

    override fun get(): Int = value

    override fun run() {}
}
