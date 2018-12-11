package com.alibaba.ttl

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import org.junit.Test
import kotlin.concurrent.thread

class TransmittableThreadLocalExtKtTest {

    @Test
    fun `simple ttl delegate`() {
        var parent by TtlDelegate<String>()
        parent = "value-set-in-parent"
        var valueFromParent: String? = null

        thread {
            valueFromParent = parent
        }.join()

        assertEquals("value-set-in-parent", valueFromParent)

    }

    @Test
    fun `ttl nullable delegate`() {
        val parent by TtlDelegate<Int>()
        var result: Int? = null

        thread {
            result = parent
        }.join()

        assertNull(result)
    }

    @Test
    fun `ttl inherited delegate`() {
        val inheritedTtl = object : InheritableThreadLocal<Int?>() { }
        var parent by TtlDelegate(inheritedTtl)
        parent = 42
        var result: Int? = null

        thread {
            result = parent
        }.join()

        assertNotNull(result)
    }
}
