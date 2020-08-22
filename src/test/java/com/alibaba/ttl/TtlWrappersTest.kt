package com.alibaba.ttl

import com.alibaba.expandThreadPool
import com.alibaba.support.junit.conditional.BelowJava8
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule
import com.alibaba.ttl.TtlUnwrap.unwrap
import com.alibaba.ttl.TtlWrappers.wrap
import org.junit.AfterClass
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.function.*
import java.util.function.Function

class TtlWrappersTest {
    @Rule
    @JvmField
    val rule = ConditionalIgnoreRule()

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = BelowJava8::class)
    fun test_null() {
        val supplier: Supplier<String>? = null
        assertNull(wrap(supplier))
        assertNull(unwrap(supplier))

        val consumer: Consumer<String>? = null
        assertNull(unwrap(consumer))
        assertNull(wrap(consumer))

        val biConsumer: BiConsumer<String, String>? = null
        assertNull(wrap(biConsumer))
        assertNull(unwrap(biConsumer))

        val function: Function<String, String>? = null
        assertNull(wrap(function))
        assertNull(unwrap(function))

        val biFunction: BiFunction<String, String, String>? = null
        assertNull(wrap(biFunction))
        assertNull(unwrap(biFunction))
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = BelowJava8::class)
    fun wrap_ReWrap_Unwrap_same() {
        val supplier = Supplier { 42 }
        val ttlSupplier = wrap(supplier)
        assertSame(ttlSupplier, wrap(ttlSupplier))
        assertSame(supplier, unwrap(ttlSupplier))

        val consumer = Consumer<String> {}
        val ttlConsumer = wrap(consumer)
        assertSame(ttlConsumer, wrap(ttlConsumer))
        assertSame(consumer, unwrap(ttlConsumer))

        val biConsumer = BiConsumer<String, String> { _, _ -> }
        val ttlBiConsumer = wrap(biConsumer)
        assertSame(ttlBiConsumer, wrap(ttlBiConsumer))
        assertSame(biConsumer, unwrap(ttlBiConsumer))

        val function = Function<String, String> { "" }
        val ttlFunction = wrap(function)
        assertSame(ttlFunction, wrap(ttlFunction))
        assertSame(function, unwrap(ttlFunction))

        val biFunction = BiFunction<String, String, String> { _, _ -> "" }
        val ttlBiFunction = wrap(biFunction)
        assertSame(ttlBiFunction, wrap(ttlBiFunction))
        assertSame(biFunction, unwrap(ttlBiFunction))
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = BelowJava8::class)
    fun test_Supplier() {
        val ttl = TransmittableThreadLocal<String>()

        fun Supplier<String>.ttlWrapThenAsRunnable(): Runnable {
            val wrap = wrap(this)!!
            return Runnable { wrap.get() }
        }

        ttl.set("1")
        Supplier<String> {
            assertEquals("1", ttl.get())
            "world"
        }.ttlWrapThenAsRunnable().let {
            ttl.set("main")

            executorService.submit(it).get()
            assertEquals("main", ttl.get())
        }

        ttl.set("2")
        Supplier<String> {
            assertEquals("2", ttl.get())
            "world"
        }.ttlWrapThenAsRunnable().let {
            ttl.set("main")

            executorService.submit(it).get()
            assertEquals("main", ttl.get())
        }
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = BelowJava8::class)
    fun test_Consumer() {
        fun Consumer<String>.ttlWrapThenAsRunnable(): Runnable {
            val wrap = wrap(this)!!
            return Runnable { wrap.accept("hello ${System.nanoTime()}") }
        }

        val ttl = TransmittableThreadLocal<String>()

        ttl.set("1")
        Consumer<String> {
            assertEquals("1", ttl.get())
        }.ttlWrapThenAsRunnable().let {
            ttl.set("main")

            executorService.submit(it).get()
            assertEquals("main", ttl.get())
        }

        ttl.set("2")
        Consumer<String> {
            assertEquals("2", ttl.get())
        }.ttlWrapThenAsRunnable().let {
            ttl.set("main")

            executorService.submit(it).get()
            assertEquals("main", ttl.get())
        }
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = BelowJava8::class)
    fun test_BiConsumer() {
        fun BiConsumer<String, String>.ttlWrapThenAsRunnable(): Runnable {
            val wrap = wrap(this)!!
            return Runnable { wrap.accept("hello ${System.nanoTime()}", "world ${System.nanoTime()}") }
        }

        val ttl = TransmittableThreadLocal<String>()

        ttl.set("1")
        BiConsumer<String, String> { _, _ ->
            assertEquals("1", ttl.get())
        }.ttlWrapThenAsRunnable().let {
            ttl.set("main")

            executorService.submit(it).get()
            assertEquals("main", ttl.get())
        }

        ttl.set("2")
        BiConsumer<String, String> { _, _ ->
            assertEquals("2", ttl.get())
        }.ttlWrapThenAsRunnable().let {
            ttl.set("main")

            executorService.submit(it).get()
            assertEquals("main", ttl.get())
        }
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = BelowJava8::class)
    fun test_Function() {
        fun Function<String, String>.ttlWrapThenAsRunnable(): Runnable {
            val wrap = wrap(this)!!
            return Runnable { wrap.apply("hello ${System.nanoTime()}") }
        }

        val ttl = TransmittableThreadLocal<String>()

        ttl.set("1")
        Function<String, String> {
            assertEquals("1", ttl.get())
            "world"
        }.ttlWrapThenAsRunnable().let {
            ttl.set("main")

            executorService.submit(it).get()
            assertEquals("main", ttl.get())
        }

        ttl.set("2")
        Function<String, String> {
            assertEquals("2", ttl.get())
            "world"
        }.ttlWrapThenAsRunnable().let {
            ttl.set("main")

            executorService.submit(it).get()
            assertEquals("main", ttl.get())
        }
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = BelowJava8::class)
    fun test_BiFunction() {
        fun BiFunction<String, String, String>.ttlWrapThenAsRunnable(): Runnable {
            val wrap = wrap(this)!!
            return Runnable { wrap.apply("hello ${System.nanoTime()}", "world") }
        }

        val ttl = TransmittableThreadLocal<String>()

        ttl.set("1")
        BiFunction<String, String, String> { _, _ ->
            assertEquals("1", ttl.get())
            "world"
        }.ttlWrapThenAsRunnable().let {
            ttl.set("main")

            executorService.submit(it).get()
            assertEquals("main", ttl.get())
        }

        ttl.set("2")
        BiFunction<String, String, String> { _, _ ->
            assertEquals("2", ttl.get())
            "world"
        }.ttlWrapThenAsRunnable().let {
            ttl.set("main")

            executorService.submit(it).get()
            assertEquals("main", ttl.get())
        }
    }

    companion object {
        private val executorService = Executors.newFixedThreadPool(3).also { expandThreadPool(it) }

        @AfterClass
        @JvmStatic
        @Suppress("unused")
        fun afterClass() {
            executorService.shutdown()
            executorService.awaitTermination(100, TimeUnit.MILLISECONDS)
            if (!executorService.isTerminated) fail("Fail to shutdown thread pool")
        }
    }
}
