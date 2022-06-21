package com.alibaba.ttl.threadpool.agent

import com.alibaba.noTtlAgentRun
import com.alibaba.ttl.threadpool.agent.TtlAgent.splitCommaColonStringToKV
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.core.test.config.TestCaseConfig
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe

class TtlAgentTest : AnnotationSpec() {
    override fun defaultTestCaseConfig(): TestCaseConfig =
        TestCaseConfig(enabled = noTtlAgentRun())

    @Test
    fun test_splitCommaColonStringToKV() {
        splitCommaColonStringToKV(null).shouldBeEmpty()
        splitCommaColonStringToKV("").shouldBeEmpty()
        splitCommaColonStringToKV("   ").shouldBeEmpty()

        splitCommaColonStringToKV("k1,k2") shouldBe mapOf("k1" to "", "k2" to "")

        splitCommaColonStringToKV("   k1,   k2 ") shouldBe mapOf("k1" to "", "k2" to "")

        splitCommaColonStringToKV("ttl.agent.logger:STDOUT") shouldBe mapOf("ttl.agent.logger" to "STDOUT")
        splitCommaColonStringToKV("k1:v1,ttl.agent.logger:STDOUT") shouldBe
                mapOf("k1" to "v1", "ttl.agent.logger" to "STDOUT")


        splitCommaColonStringToKV("     k1     :v1  , ttl.agent.logger    :STDOUT   ") shouldBe
                mapOf("k1" to "v1", "ttl.agent.logger" to "STDOUT")

        splitCommaColonStringToKV("     k1     :v1  , ttl.agent.logger    :STDOUT   ,k3") shouldBe
                mapOf("k1" to "v1", "ttl.agent.logger" to "STDOUT", "k3" to "")
    }
}
