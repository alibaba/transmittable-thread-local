package com.alibaba.third_part_lib_test

import com.alibaba.support.junit.conditional.ConditionalIgnoreRule
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule.ConditionalIgnore
import com.alibaba.support.junit.conditional.IsAgentRun
import javassist.ClassPool
import javassist.CtClass
import org.hamcrest.CoreMatchers.containsString
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/**
 * [simplify the try-finally code gen by javassist, do not need copy method #115](https://github.com/alibaba/transmittable-thread-local/issues/115)
 */
class JavassistTest {
    @Rule
    @JvmField
    val rule = ConditionalIgnoreRule()

    @Test
    @ConditionalIgnore(condition = IsAgentRun::class) // skip unit test for Javassist on agent, because Javassist is repackaged
    fun insertAfter_as_finally() {
        val classPool = ClassPool(true)
        val ctClass = classPool.getCtClass("com.alibaba.third_part_lib_test.DemoRunnable")
        // To execute it when an exception is thrown, the second parameter asFinally to insertAfter() must be true.
        ctClass.getDeclaredMethod("run", arrayOf()).insertAfter("value = 42;", true)

        val instance = ctClass.toClass().getDeclaredConstructor().newInstance()

        assertEquals(0, (instance as Supplier).get())

        (instance as Runnable).let {
            try {
                it.run()

                fail()
            } catch (e: RuntimeException) {
                assertEquals("Intended", e.message)
            }
        }

        assertEquals(42, (instance as Supplier).get())
    }

    /**
     * more info see
     * - [Bad Bytecode when trying access localVariable using insertAfter and asFinally = true](https://issues.jboss.org/browse/JASSIST-232?_sscc=t)
     * - Javadoc of [javassist.CtBehavior.addLocalVariable]:
     * If the second parameter asFinally to insertAfter() is true, the declared local variable is not visible from the code inserted by insertAfter().
     */
    @Test
    @ConditionalIgnore(condition = IsAgentRun::class) // skip unit test for Javassist on agent, because Javassist is repackaged
    fun insertAfter_as_finally_fail_with_local_var() {
        val classPool = ClassPool(true)
        val ctClass = classPool.getCtClass("com.alibaba.third_part_lib_test.DemoRunnable2")
        // To execute it when an exception is thrown, the second parameter asFinally to insertAfter() must be true.
        ctClass.getDeclaredMethod("run", arrayOf()).apply {
            addLocalVariable("var", CtClass.intType)
            insertBefore("var = 2;")
            insertAfter("value = 40 + var;", true)
        }

        try {
            (ctClass.toClass().getDeclaredConstructor().newInstance() as Runnable).run()

            fail()
        } catch (e: VerifyError) {
            e.printStackTrace()
            assertThat<String>(e.message, containsString("Bad local variable type"))
        }
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
