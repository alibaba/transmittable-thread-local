package com.alibaba.ttl.threadpool.agent.transformlet.helper

import com.alibaba.support.junit.conditional.ConditionalIgnoreRule
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule.ConditionalIgnore
import com.alibaba.support.junit.conditional.IsAgentRun
import com.alibaba.ttl.threadpool.agent.logging.Logger
import com.alibaba.ttl.threadpool.agent.transformlet.helper.TtlTransformletHelper.*
import javassist.ClassPool
import org.apache.commons.lang3.StringUtils
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

class TtlTransformletHelperTest {
    @Rule
    @JvmField
    val rule = ConditionalIgnoreRule()

    @Test
    fun test_getFileLocationOfClass_javaClass() {

        assertNull(getLocationFileOfClass(String::class.java))

        MatcherAssert.assertThat(
            getLocationFileOfClass(StringUtils::class.java),
            CoreMatchers.endsWith("/commons-lang3-3.5.jar")
        )
    }

    @Test
    @ConditionalIgnore(condition = IsAgentRun::class) // skip unit test for Javassist on agent, because Javassist is repackaged
    fun test_getFileLocationOfClass_ctClass() {
        val classPool = ClassPool(true)

        // Java 8: file:/path/to/jdk_8/jre/lib/rt.jar!/java/lang/String.class
        // Java 11: /java.base/java/lang/String.class
        MatcherAssert.assertThat(
            getLocationFileOfClass(classPool.getCtClass("java.lang.String")),
            CoreMatchers.endsWith("/java/lang/String.class")
        )

        // Java 8: file:/path/to/commons-lang3-3.5.jar!/org/apache/commons/lang3/StringUtils.class
        MatcherAssert.assertThat(
            getLocationFileOfClass(classPool.getCtClass("org.apache.commons.lang3.StringUtils")),
            CoreMatchers.endsWith("/commons-lang3-3.5.jar!/org/apache/commons/lang3/StringUtils.class")
        )
    }

    @Test
    fun test_className_package() {
        assertEquals("", getPackageName("Hello"))
        assertEquals("com.foo", getPackageName("com.foo.Hello"))

        assertTrue(isClassAtPackage("java.util.TimerTask", "java.util"))
        assertFalse(isClassAtPackage("java.util.TimerTask", "java.utils"))
        assertFalse(isClassAtPackage("java.util.TimerTask", "java"))
        assertFalse(isClassAtPackage("java.util.TimerTask", "java.util.zip"))

        assertTrue(isClassUnderPackage("java.util.TimerTask", "java.util"))
        assertFalse(isClassUnderPackage("java.util.TimerTask", "java.utils"))
        assertTrue(isClassUnderPackage("java.util.TimerTask", "java"))
        assertFalse(isClassUnderPackage("java.util.TimerTask", "javax"))

        assertTrue(isClassAtPackageJavaUtil("java.util.PriorityQueue"))
        assertFalse(isClassAtPackageJavaUtil("java.util.zip.ZipInputStream"))
    }

    companion object {
        @BeforeClass
        @JvmStatic
        @Suppress("unused")
        fun beforeClass() {
            Logger.setLoggerImplTypeIfNotSetYet("stderr")
        }
    }
}
