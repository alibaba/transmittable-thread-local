package com.alibaba.user_api_test.ttl3

import com.alibaba.ttl3.TransmittableThreadLocal
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.Test
import java.util.concurrent.FutureTask
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

/**
 * Test the "Ignore-Null-Value Semantics" of [TransmittableThreadLocal] from user code(different package)
 */
class DisableIgnoreNullValueSemanticsTest {
    @Test
    fun test_TTL_not_disableIgnoreNullValueSemantics_defaultTtlBehavior() {
        val ttl = object : TransmittableThreadLocal<String?>() {
            override fun initialValue(): String {
                return "init"
            }

            override fun childValue(parentValue: String?): String {
                return "$parentValue + child"
            }
        }

        ttl.get() shouldBe "init"
        ttl.set(null)
        // DO NOT `ttl.get()` !
        //   `get` operation will re-init the value of ThreadLocal

        val task = FutureTask {
            ttl.get()
        }
        thread { task.run() }.join()

        // `get` operation will re-init the value of ThreadLocal !
        ttl.get() shouldBe "init"
        // "Ignore-Null-Value Semantics" will not transmit ThreadLocal with the null value,
        // so the value in new thread is "init" value
        ttl.get() shouldBe "init"

        //////////////////////////////////////

        val task2 = FutureTask {
            ttl.get()
        }
        thread { task2.run() }.join()

        ttl.get() shouldBe "init"
        task2.get() shouldBe "init + child"
    }

    @Test
    fun test_TTL_not_disableIgnoreNullValueSemantics_defaultTtlBehavior_getSafe_ForNullInit() {
        val count = AtomicInteger()

        val ttl = object : TransmittableThreadLocal<String?>() {
            override fun initialValue(): String? {
                count.getAndIncrement()
                return super.initialValue()
            }

            override fun childValue(parentValue: String?): String? {
                count.getAndSet(1000)
                return super.childValue(parentValue)
            }
        }

        ttl.get().shouldBeNull()
        count.get() shouldBe 1

        ttl.set(null)
        ttl.get().shouldBeNull()
        count.get() shouldBe 2
    }

    @Test
    fun test_TTL_disableIgnoreNullValueSemantics_sameAsThreadLocal() {
        val ttl = object : TransmittableThreadLocal<String?>(true) {
            override fun initialValue(): String {
                return "init"
            }

            override fun childValue(parentValue: String?): String {
                return "$parentValue + child"
            }
        }

        ttl.get() shouldBe "init"
        ttl.set(null)
        ttl.get().shouldBeNull()

        val task = FutureTask {
            ttl.get()
        }
        thread { task.run() }.join()

        ttl.get().shouldBeNull()
        task.get() shouldBe "null + child"

        //////////////////////////////////////

        val task2 = FutureTask {
            ttl.get()
        }
        thread { task2.run() }.join()

        ttl.get().shouldBeNull()
        task.get() shouldBe "null + child"
    }

    @Test
    fun test_InheritableThreadLocal() {
        val ttl = object : InheritableThreadLocal<String?>() {
            override fun initialValue(): String {
                return "init"
            }

            override fun childValue(parentValue: String?): String {
                return "$parentValue + child"
            }
        }

        ttl.get() shouldBe "init"
        ttl.set(null)
        ttl.get().shouldBeNull()

        val task = FutureTask {
            ttl.get()
        }
        thread { task.run() }.join()

        ttl.get().shouldBeNull()
        task.get() shouldBe "null + child"

        //////////////////////////////////////

        val task2 = FutureTask {
            ttl.get()
        }
        thread { task2.run() }.join()

        ttl.get().shouldBeNull()
        task.get() shouldBe "null + child"
    }
}
