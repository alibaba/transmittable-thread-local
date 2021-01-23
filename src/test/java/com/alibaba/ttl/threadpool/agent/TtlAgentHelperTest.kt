package com.alibaba.ttl.threadpool.agent

import com.alibaba.support.junit.conditional.ConditionalIgnoreRule
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule.ConditionalIgnore
import com.alibaba.support.junit.conditional.IsAgentRun
import com.alibaba.ttl.threadpool.agent.TtlAgentHelper.splitCommaColonStringToKV
import com.alibaba.ttl.threadpool.agent.TtlAgentHelper.splitListStringToStringList
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TtlAgentHelperTest {
    @Rule
    @JvmField
    val rule = ConditionalIgnoreRule()

    @Test
    @ConditionalIgnore(condition = IsAgentRun::class)
    fun test_splitCommaColonStringToKV() {
        assertEquals(emptyMap<String, String>(), splitCommaColonStringToKV(null))
        assertEquals(emptyMap<String, String>(), splitCommaColonStringToKV(""))
        assertEquals(emptyMap<String, String>(), splitCommaColonStringToKV("   "))


        assertEquals(mapOf("k1" to "", "k2" to ""), splitCommaColonStringToKV(
                "k1,k2"))
        assertEquals(mapOf("k1" to "", "k2" to ""), splitCommaColonStringToKV(
                "   k1,   k2 "))

        assertEquals(mapOf("ttl.agent.logger" to "STDOUT"), splitCommaColonStringToKV(
                "ttl.agent.logger:STDOUT"))
        assertEquals(mapOf("k1" to "v1", "ttl.agent.logger" to "STDOUT"), splitCommaColonStringToKV(
                "k1:v1,ttl.agent.logger:STDOUT"))

        assertEquals(mapOf("k1" to "v1", "ttl.agent.logger" to "STDOUT"), splitCommaColonStringToKV(
                "     k1     :v1  , ttl.agent.logger    :STDOUT   "))

        assertEquals(mapOf("k1" to "v1", "ttl.agent.logger" to "STDOUT", "k3" to ""), splitCommaColonStringToKV(
                "     k1     :v1  , ttl.agent.logger    :STDOUT   ,k3"))
    }

    @Test
    @ConditionalIgnore(condition = IsAgentRun::class)
    fun test_splitListStringToStringList() {
        assertEquals(emptyList<String>(), splitListStringToStringList(null))
        assertEquals(emptyList<String>(), splitListStringToStringList(""))
        assertEquals(emptyList<String>(), splitListStringToStringList("   "))
        assertEquals(emptyList<String>(), splitListStringToStringList("   |"))
        assertEquals(emptyList<String>(), splitListStringToStringList("   |  "))
        assertEquals(emptyList<String>(), splitListStringToStringList("   | | "))

        assertEquals(listOf("v1", "v2"), splitListStringToStringList("v1|v2"))
        assertEquals(listOf("v1", "v2", "v3"), splitListStringToStringList("  v1|v2  |v3  "))

        assertEquals(listOf("com.alibaba.ttl.TtlExecutorTransformlet"),
            splitListStringToStringList("com.alibaba.ttl.TtlExecutorTransformlet"))
        assertEquals(listOf("com.alibaba.ttl.TtlExecutorTransformlet", "com.alibaba.ttl.TtlForkJoinTransformlet", "v3"),
            splitListStringToStringList("com.alibaba.ttl.TtlExecutorTransformlet|com.alibaba.ttl.TtlForkJoinTransformlet|v3"))
        assertEquals(listOf("com.alibaba.ttl.TtlExecutorTransformlet", "com.alibaba.ttl.TtlForkJoinTransformlet", "v3"),
            splitListStringToStringList("  com.alibaba.ttl.TtlExecutorTransformlet|  com.alibaba.ttl.TtlForkJoinTransformlet   |v3  "))
    }
}
