package com.alibaba.ttl.threadpool.agent

import com.alibaba.noTtlAgentRun
import com.alibaba.ttl.threadpool.agent.logging.Logger
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.core.test.config.TestCaseConfig
import org.junit.Assert.assertEquals

class TtlExtensionTransformletManagerTest : AnnotationSpec() {
    override fun defaultTestCaseConfig(): TestCaseConfig =
        TestCaseConfig(enabled = noTtlAgentRun())

    @Test
    fun test_readLines() {
        val classLoader = TtlExtensionTransformletManagerTest::class.java.classLoader
        val pair = TtlExtensionTransformletManager.readLinesFromExtensionFiles(
            classLoader.getResources("test_extension/foo.txt"), mutableMapOf()
        )
        val lines: LinkedHashSet<String> = pair.first

        assertEquals(
            linkedSetOf("hello.World", "hello.tabBefore", "hello.tabAfter", "hello.spaceBefore", "hello.spaceAfter"),
            lines
        )
    }

    @BeforeAll
    fun beforeAll() {
        Logger.setLoggerImplTypeIfNotSetYet("stderr")
    }
}
