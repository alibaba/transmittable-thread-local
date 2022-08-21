package com.alibaba.user_api_test.ttl3

import com.alibaba.ttl3.transmitter.Transmittee
import com.alibaba.ttl3.transmitter.Transmitter
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.mockk.*

/**
 * Test [Transmitter] from user code(different package)
 */
class TransmittableThreadLocal_Transmitter_registerTransmittee_UserTest : AnnotationSpec() {
    @Test
    fun test_registerTransmittee_crr() {
        // ========================================
        // 0. mocks creation and stubbing
        // ========================================
        val transmittee = mockk<Transmittee<List<String>, Set<Int>>>()
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
        val transmittee = mockk<Transmittee<List<String>, Set<Int>>>()
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
