package com.alibaba.ttl

import com.alibaba.assertChildTtlValues
import com.alibaba.assertParentTtlValues
import com.alibaba.copyTtlValues
import com.alibaba.createParentTtlInstances
import com.alibaba.createParentTtlInstancesAfterCreateChild
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule
import com.alibaba.ttl.testmodel.Call
import org.hamcrest.CoreMatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test

class TtlCallableExtKtTest {

    @Rule
    @JvmField
    val rule = ConditionalIgnoreRule()

    @Test
    fun `callable wrapTtl extension function `() {
        val call = Call("1")
        val ttlCallable = call.wrapTtl()
        assertSame(call, ttlCallable.callable)
    }

    @Test
    fun `callable wrapTtl extension function multiple times`() {
        val call = Call("1").wrapTtl()
        try {
            call.wrapTtl()
            fail()
        } catch (e: IllegalStateException) {
            assertThat<String>(e.message, CoreMatchers.containsString("Already TtlCallable"))
        }

    }

    @Test
    fun `list of callable wrapTtl extension function`() {
        val callList = listOf(Call("1"), Call("2"), Call("3")).wrapTtl()

        assertEquals(3, callList.size)
        callList.forEach {
            assertThat(it, CoreMatchers.instanceOf(TtlCallable::class.java))
        }
    }

    @Test
    fun `TtlCallable invoke operator`() {
        val ttlInstances = createParentTtlInstances()

        val call = Call("1", ttlInstances)


        val ttlCallable = call.wrapTtl()

        // create after new Task, won't see parent value in in task!
        createParentTtlInstancesAfterCreateChild(ttlInstances)

        // run in the *current* thread
        assertEquals("ok", ttlCallable())


        // child Inheritable
        assertChildTtlValues("1", call.copied)

        // child do not effect parent
        assertParentTtlValues(copyTtlValues(ttlInstances))
    }

}
