package com.alibaba.ttl.threadpool.agent.transformlet.helper

import com.alibaba.ttl.threadpool.agent.TtlAgent
import com.alibaba.ttl.threadpool.agent.logging.Logger
import com.alibaba.ttl.threadpool.agent.transformlet.helper.TtlTransformletHelper.*
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldEndWith
import javassist.ClassPool
import org.apache.commons.lang3.StringUtils
import org.junit.Assert.*

class TtlTransformletHelperTest : AnnotationSpec() {

    @Test
    fun test_getFileLocationOfClass_javaClass() {

        getLocationFileOfClass(String::class.java).shouldBeNull()

        val locationFileOfClass = getLocationFileOfClass(StringUtils::class.java)
        locationFileOfClass shouldEndWith ".jar"
        locationFileOfClass shouldContain "/commons-lang3-"
    }

    @Test
    fun test_getFileLocationOfClass_ctClass() {
        if (TtlAgent.isTtlAgentLoaded()) return

        val classPool = ClassPool(true)

        // Java 8: file:/path/to/jdk_8/jre/lib/rt.jar!/java/lang/String.class
        // Java 11: /java.base/java/lang/String.class
        getLocationFileOfClass(classPool.getCtClass("java.lang.String"))
            .shouldEndWith("/java/lang/String.class")

        // Java 8: file:/path/to/commons-lang3-3.5.jar!/org/apache/commons/lang3/StringUtils.class
        val locationFileOfClass = getLocationFileOfClass(classPool.getCtClass("org.apache.commons.lang3.StringUtils"))
        locationFileOfClass shouldEndWith ".jar!/org/apache/commons/lang3/StringUtils.class"
        locationFileOfClass shouldContain "/commons-lang3-"
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

    @BeforeAll
    fun beforeClass() {
        Logger.setLoggerImplTypeIfNotSetYet("stderr")
    }
}
