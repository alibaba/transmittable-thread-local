package com.alibaba.user_api_test.ttl

import com.alibaba.noTtlAgentRun
import com.alibaba.ttl.TransmittableThreadLocal.Transmitter
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.core.test.config.TestCaseConfig
import io.kotest.matchers.booleans.shouldBeTrue
import io.mockk.*
import org.apache.commons.lang3.JavaVersion
import org.apache.commons.lang3.SystemUtils

/**
 * Test [Transmitter] from user code(different package)
 */
class TransmittableThreadLocal_Transmitter_registerTransmittee_UserTest : AnnotationSpec() {
    @Suppress("OVERRIDE_DEPRECATION")
    override fun defaultTestCaseConfig(): TestCaseConfig {
        // If run under Agent and under java 11+, fail to find proxy classes;
        // so just skipped.
        //
        // error info:
        //   java.lang.NoClassDefFoundError: io/mockk/proxy/jvm/advice/jvm/JvmMockKProxyInterceptor
        // more info error info see:
        //   https://github.com/alibaba/transmittable-thread-local/runs/7826806473?check_suite_focus=true
        if (SystemUtils.isJavaVersionAtMost(JavaVersion.JAVA_1_8)) {
            return TestCaseConfig(enabled = true)
        }

        return TestCaseConfig(enabled = noTtlAgentRun())
    }

    @Test
    fun test_registerTransmittee_crr() {
        // ========================================
        // 0. mocks creation and stubbing
        // ========================================
        val transmittee = mockk<Transmitter.Transmittee<List<String>, Set<Int>>>()
        @Suppress("UnusedEquals", "ReplaceCallWithBinaryOperator")
        excludeRecords {
            transmittee.equals(any())
            transmittee.hashCode()
        }

        every { transmittee.capture() } returns listOf("42", "43")
        every { transmittee.replay(listOf("42", "43")) } returns setOf(42, 43)
        every { transmittee.restore(setOf(42, 43)) } just Runs

        try {
            // ========================================
            // 1. mock record(aka. invocation)
            // ========================================
            Transmitter.registerTransmittee(transmittee).shouldBeTrue()

            val captured = Transmitter.capture()
            val backup = Transmitter.replay(captured)
            Transmitter.restore(backup)

            // ========================================
            // 2. mock verification
            // ========================================
            verifySequence {
                transmittee.capture()
                transmittee.replay(any())
                transmittee.restore(any())
            }
            confirmVerified(transmittee)
        } finally {
            Transmitter.unregisterTransmittee(transmittee).shouldBeTrue()
        }
    }

    @Test
    fun test_registerTransmittee_clear_restore() {
        // ========================================
        // 0. mocks creation and stubbing
        // ========================================
        val transmittee = mockk<Transmitter.Transmittee<List<String>, Set<Int>>>()
        @Suppress("UnusedEquals", "ReplaceCallWithBinaryOperator")
        excludeRecords {
            transmittee.equals(any())
            transmittee.hashCode()
        }

        every { transmittee.clear() } returns setOf(42, 43)
        every { transmittee.restore(setOf(42, 43)) } just Runs

        try {
            // ========================================
            // 1. mock record(aka. invocation)
            // ========================================
            Transmitter.registerTransmittee(transmittee).shouldBeTrue()

            val backup = Transmitter.clear()
            Transmitter.restore(backup)

            // ========================================
            // 2. mock verification
            // ========================================
            verifySequence {
                transmittee.clear()
                transmittee.restore(any())
            }
            confirmVerified(transmittee)
        } finally {
            Transmitter.unregisterTransmittee(transmittee).shouldBeTrue()
        }
    }
}
