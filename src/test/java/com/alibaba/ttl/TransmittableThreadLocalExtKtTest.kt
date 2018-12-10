package com.alibaba.ttl

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Test
import kotlin.concurrent.thread

class TransmittableThreadLocalExtKtTest {

    @Test
    fun `ttl delegate`() {
        var parent by TtlDelegate<String>()
        parent = "value-set-in-parent"

        thread {
            val valueFromParent = parent
            assertEquals("value-set-in-parent", valueFromParent)
        }.join()

    }

    @Test
    fun `ttl nullable delegate`() {
        val parent by TtlDelegate<Int>()

        thread {
            assertNotNull("value-set-in-parent", parent)
        }.join()

    }
}
