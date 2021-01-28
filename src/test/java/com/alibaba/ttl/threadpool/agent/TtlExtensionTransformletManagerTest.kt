package com.alibaba.ttl.threadpool.agent

import com.alibaba.support.junit.conditional.ConditionalIgnoreRule
import com.alibaba.support.junit.conditional.IsAgentRun
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.*

class TtlExtensionTransformletManagerTest {
    @Rule
    @JvmField
    val rule = ConditionalIgnoreRule()

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = IsAgentRun::class)
    fun test_readLines() {
        TtlAgentLoggerInitializer

        val classLoader = TtlExtensionTransformletManagerTest::class.java.classLoader
        val lines: LinkedHashSet<String> =
            TtlExtensionTransformletManager.readLines(classLoader.getResources("test_extension/foo.txt"), mutableMapOf(), linkedSetOf())

        assertEquals(
            linkedSetOf("hello.World", "hello.tabBefore", "hello.tabAfter", "hello.spaceBefore", "hello.spaceAfter"),
            lines
        )
    }
}
