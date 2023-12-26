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
import org.apache.commons.lang3.JavaVersion
import org.apache.commons.lang3.SystemUtils

/**
 * [simplify the try-finally code gen by javassist, do not need copy method #115](https://github.com/alibaba/transmittable-thread-local/issues/115)
 */
class JavassistTest : AnnotationSpec() {
    /**
     * ## skip test case when run unit test under TTL agent
     *
     * Because javassist is repackaged and excluded, javassist-classes is not found.
     *
     * ## skip test case when run unit test under java 16+
     *
     * Because with the release of Java 16 the access control of the new Jigsaw module system is starting to be enforced by the JVM,
     * encounter `InaccessibleObjectException`:
     *
     * ```
     * Caused by: java.lang.reflect.InaccessibleObjectException: Unable to make protected final java.lang.Class java.lang.ClassLoader.defineClass(java.lang.String,byte[],int,int,java.security.ProtectionDomain) throws java.lang.ClassFormatError accessible: module java.base does not "opens java.lang" to unnamed module @604cb8dc
     * at java.base/java.lang.reflect.AccessibleObject.checkCanSetAccessible(AccessibleObject.java:354)
     * at java.base/java.lang.reflect.AccessibleObject.checkCanSetAccessible(AccessibleObject.java:297)
     * at java.base/java.lang.reflect.Method.checkCanSetAccessible(Method.java:199)
     * at java.base/java.lang.reflect.Method.setAccessible(Method.java:193)
     * at javassist.util.proxy.SecurityActions.setAccessible(SecurityActions.java:159)
     * at javassist.util.proxy.DefineClassHelper$JavaOther.defineClass(DefineClassHelper.java:213)
     * at javassist.util.proxy.DefineClassHelper$Java11.defineClass(DefineClassHelper.java:52)
     * at javassist.util.proxy.DefineClassHelper.toClass(DefineClassHelper.java:260)
     * at javassist.ClassPool.toClass(ClassPool.java:1240)
     * at javassist.ClassPool.toClass(ClassPool.java:1098)
     * at javassist.ClassPool.toClass(ClassPool.java:1056)
     * at javassist.CtClass.toClass(CtClass.java:1298)
     * at com.alibaba.third_part_lib_test.JavassistTest.insertAfter_as_finally()
     * ...
     * ```
     */
    @Suppress("OVERRIDE_DEPRECATION")
    override fun defaultTestCaseConfig(): TestCaseConfig =
        TestCaseConfig(enabled = noTtlAgentRun() && SystemUtils.isJavaVersionAtMost(JavaVersion.JAVA_15))

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
