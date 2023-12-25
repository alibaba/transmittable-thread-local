package com.alibaba.ttl.reported_bugs

import com.alibaba.noTtlAgentRun
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.core.test.config.TestCaseConfig
import javassist.ClassPool
import org.apache.commons.lang3.SystemUtils

/**
 * Bug URL:https://github.com/alibaba/transmittable-thread-local/issues/555
 * Reporter: @brucelwl
 */
class Issue555Test : AnnotationSpec() {
    @Suppress("OVERRIDE_DEPRECATION")
    override fun defaultTestCaseConfig(): TestCaseConfig {
        // only run test when not run by TTL agent,
        // since the javassist is repackaged(will throw java.lang.NoClassDefFoundError: javassist/ClassPool)
        return TestCaseConfig(enabled = SystemUtils.IS_JAVA_21 && noTtlAgentRun())
    }

    @Test
    fun test() {
        val classPool = ClassPool(true)
        // below reproducible code from issue by @wuwen5:
        // https://github.com/jboss-javassist/javassist/issues/462#issue-1931629164
        val ctClass = classPool.get("sun.net.httpserver.ServerImpl\$ReqRspTimeoutTask")
        ctClass.classFile.compact()
    }
}
