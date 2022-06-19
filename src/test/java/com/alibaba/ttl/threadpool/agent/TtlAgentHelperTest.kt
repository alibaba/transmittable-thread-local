package com.alibaba.ttl.threadpool.agent

import com.alibaba.ttl.threadpool.agent.TtlAgentHelper.*
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.core.test.config.TestCaseConfig
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.Assert.*

class TtlAgentHelperTest : AnnotationSpec() {
    override fun defaultTestCaseConfig(): TestCaseConfig =
        TestCaseConfig(enabled = !TtlAgent.isTtlAgentLoaded())

    @Test
    fun test_isBooleanOptionSet() {

        // === test with KV config only  ===

        val kvs = mapOf(
            "ttl.test.bool_k1" to "true",
            "ttl.test.bool_k2" to "false",
            "ttl.test.bool_k3" to "" // value absent
            // ttl.test.notExisted : key absent
        )

        kvs.keys.forEach {
            System.clearProperty(it)
        }
        System.clearProperty("ttl.test.notExisted")

        assertTrue(isBooleanOptionSet(kvs, "ttl.test.bool_k1", false))
        assertTrue(isBooleanOptionSet(kvs, "ttl.test.bool_k1", true))

        assertFalse(isBooleanOptionSet(kvs, "ttl.test.bool_k2", true))
        assertFalse(isBooleanOptionSet(kvs, "ttl.test.bool_k2", false))

        // bool_k3 is *value absent*, IS true
        assertTrue(isBooleanOptionSet(kvs, "ttl.test.bool_k3", true))
        assertTrue(isBooleanOptionSet(kvs, "ttl.test.bool_k3", false))

        // notExisted is *key absent*, use defaultValueIfKeyAbsent
        assertTrue(isBooleanOptionSet(kvs, "ttl.test.notExisted", true))
        assertFalse(isBooleanOptionSet(kvs, "ttl.test.notExisted", false))


        // === test with -D properties override ===

        // override with -D properties value
        System.setProperty("ttl.test.bool_k1", "false")

        assertFalse(isBooleanOptionSet(kvs, "ttl.test.bool_k1", false))
        assertFalse(isBooleanOptionSet(kvs, "ttl.test.bool_k1", true))

        // override with -D properties empty value, IS true
        System.setProperty("ttl.test.bool_k2", "")

        assertTrue(isBooleanOptionSet(kvs, "ttl.test.bool_k2", true))
        assertTrue(isBooleanOptionSet(kvs, "ttl.test.bool_k2", false))


        // === test with -D properties config only  ===

        mapOf(
            "ttl.test.property_only.bool_k1" to "true",
            "ttl.test.property_only.bool_k2" to "false",
            "ttl.test.property_only.bool_k3" to "" // value absent
            // ttl.test.property_only.notExisted : key absent
        ).forEach { (k, v) -> System.setProperty(k, v) }
        System.clearProperty("ttl.test.property_only.notExisted")

        assertTrue(isBooleanOptionSet(kvs, "ttl.test.property_only.bool_k1", false))
        assertTrue(isBooleanOptionSet(kvs, "ttl.test.property_only.bool_k1", true))

        assertFalse(isBooleanOptionSet(kvs, "ttl.test.property_only.bool_k2", true))
        assertFalse(isBooleanOptionSet(kvs, "ttl.test.property_only.bool_k2", false))

        // bool_k3 is *value absent*, IS true
        assertTrue(isBooleanOptionSet(kvs, "ttl.test.property_only.bool_k3", true))
        assertTrue(isBooleanOptionSet(kvs, "ttl.test.property_only.bool_k3", false))

        // notExisted is *key absent*, use defaultValueIfKeyAbsent
        assertTrue(isBooleanOptionSet(kvs, "ttl.test.property_only.notExisted", true))
        assertFalse(isBooleanOptionSet(kvs, "ttl.test.property_only.notExisted", false))
    }

    @Test
    fun test_getOptionStringListValues() {

        // === test with KV config only  ===

        val kvs = mapOf(
            "ttl.test.str_k1" to "value1",
            "ttl.test.str_k2" to "" // value absent
            // ttl.test.notExisted : key absent
        )

        kvs.keys.forEach {
            System.clearProperty(it)
        }
        System.clearProperty("ttl.test.notExisted")

        assertEquals("value1", getStringOptionValue(kvs, "ttl.test.str_k1", "default_value"))
        // str_k2 is *value absent*, use default value
        assertEquals("default_value", getStringOptionValue(kvs, "ttl.test.str_k2", "default_value"))
        // notExisted is *key absent*, use default value
        assertEquals("default_value", getStringOptionValue(kvs, "ttl.test.notExisted", "default_value"))


        // === test with -D properties override ===

        // override with -D properties value
        System.setProperty("ttl.test.str_k1", "value2")
        assertEquals("value2", getStringOptionValue(kvs, "ttl.test.str_k1", "default_value"))

        // override with -D properties empty value, use default value
        System.setProperty("ttl.test.str_k1", "")
        assertEquals("default_value", getStringOptionValue(kvs, "ttl.test.str_k1", "default_value"))


        // === test with -D properties config only ===

        mapOf(
            "ttl.test.property_only.str_k1" to "value1",
            "ttl.test.property_only.str_k2" to "" // value absent
            // ttl.test.property_only.notExisted : key absent
        ).forEach { (k, v) -> System.setProperty(k, v) }
        System.clearProperty("ttl.test.property_only.notExisted")


        assertEquals("value1", getStringOptionValue(kvs, "ttl.test.property_only.str_k1", "default_value"))
        // str_k2 is *value absent*, use default value
        assertEquals("default_value", getStringOptionValue(kvs, "ttl.test.property_only.str_k2", "default_value"))
        // str_k2 is *key absent*, use default value
        assertEquals("default_value", getStringOptionValue(kvs, "ttl.test.property_only.notExisted", "default_value"))
    }

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

    @Test
    fun test_splitListStringToStringList() {
        splitListStringToStringList(null).shouldBeEmpty()
        splitListStringToStringList("").shouldBeEmpty()
        splitListStringToStringList("   ").shouldBeEmpty()
        splitListStringToStringList("   |").shouldBeEmpty()
        splitListStringToStringList("   |  ").shouldBeEmpty()
        splitListStringToStringList("   | | ").shouldBeEmpty()

        splitListStringToStringList("v1|v2")
            .shouldContainExactly("v1", "v2")
        splitListStringToStringList("  v1|v2  |v3  ")
            .shouldContainExactly("v1", "v2", "v3")

        splitListStringToStringList("com.alibaba.ttl.TtlExecutorTransformlet")
            .shouldContainExactly("com.alibaba.ttl.TtlExecutorTransformlet")

        splitListStringToStringList("com.alibaba.ttl.TtlExecutorTransformlet|com.alibaba.ttl.TtlForkJoinTransformlet|v3")
            .shouldContainExactly(
                "com.alibaba.ttl.TtlExecutorTransformlet",
                "com.alibaba.ttl.TtlForkJoinTransformlet",
                "v3"
            )

        splitListStringToStringList("  com.alibaba.ttl.TtlExecutorTransformlet|  com.alibaba.ttl.TtlForkJoinTransformlet   |v3  ")
            .shouldContainExactly(
                "com.alibaba.ttl.TtlExecutorTransformlet",
                "com.alibaba.ttl.TtlForkJoinTransformlet",
                "v3"
            )
    }
}
