package com.alibaba.ttl.threadpool.agent

import com.alibaba.support.junit.conditional.ConditionalIgnoreRule
import com.alibaba.support.junit.conditional.IsAgentRun
import com.alibaba.ttl.threadpool.agent.logging.Logger
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
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

    companion object {
        @BeforeClass
        @JvmStatic
        @Suppress("unused")
        fun beforeClass() {
            Logger.setLoggerImplTypeIfNotSetYet("stderr")
        }
    }
}
