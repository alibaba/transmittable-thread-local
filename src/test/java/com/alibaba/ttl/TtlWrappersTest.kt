package com.alibaba.ttl

import com.alibaba.expandThreadPool
import com.alibaba.ttl.TtlUnwrap.unwrap
import com.alibaba.ttl.TtlWrappers.*
import io.kotest.core.spec.style.AnnotationSpec
import org.junit.Assert.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.function.*
import java.util.function.Function

class TtlWrappersTest : AnnotationSpec() {

    @Test
    fun test_null() {
        val supplier: Supplier<String>? = null
        @Suppress("DEPRECATION")
        assertNull(wrap(supplier))
        assertNull(wrapSupplier(supplier))
        assertNull(unwrap(supplier))

        val consumer: Consumer<String>? = null
        @Suppress("DEPRECATION")
        assertNull(wrap(consumer))
        assertNull(wrapConsumer(consumer))
        assertNull(unwrap(consumer))

        val biConsumer: BiConsumer<String, String>? = null
        @Suppress("DEPRECATION")
        assertNull(wrap(biConsumer))
        assertNull(wrapBiConsumer(biConsumer))
        assertNull(unwrap(biConsumer))

        val function: Function<String, String>? = null
        @Suppress("DEPRECATION")
        assertNull(wrap(function))
        assertNull(wrapFunction(function))
        assertNull(unwrap(function))

        val biFunction: BiFunction<String, String, String>? = null
        @Suppress("DEPRECATION")
        assertNull(wrap(biFunction))
        assertNull(wrapBiFunction(biFunction))
        assertNull(unwrap(biFunction))
    }

    @Test
    fun wrap_ReWrap_Unwrap_same() {
        // Supplier
        val supplier = Supplier { 42 }

        @Suppress("DEPRECATION")
        val ttlSupplier = wrap(supplier)
        @Suppress("DEPRECATION")
        assertSame(ttlSupplier, wrap(ttlSupplier))
        assertSame(ttlSupplier, wrapSupplier(ttlSupplier))
        assertSame(supplier, unwrap(ttlSupplier))

        val ttlSupplier2 = wrapSupplier(supplier)
        assertSame(ttlSupplier2, wrapSupplier(ttlSupplier2))
        @Suppress("DEPRECATION")
        assertSame(ttlSupplier2, wrap(ttlSupplier2))
        assertSame(supplier, unwrap(ttlSupplier2))

        // Consumer
        val consumer = Consumer<String> {}

        @Suppress("DEPRECATION")
        val ttlConsumer = wrap(consumer)
        @Suppress("DEPRECATION")
        assertSame(ttlConsumer, wrap(ttlConsumer))
        assertSame(ttlConsumer, wrapConsumer(ttlConsumer))
        assertSame(consumer, unwrap(ttlConsumer))

        val ttlConsumer2 = wrapConsumer(consumer)
        assertSame(ttlConsumer2, wrapConsumer(ttlConsumer2))
        @Suppress("DEPRECATION")
        assertSame(ttlConsumer2, wrap(ttlConsumer2))
        assertSame(consumer, unwrap(ttlConsumer2))

        // BiConsumer
        val biConsumer = BiConsumer<String, String> { _, _ -> }

        @Suppress("DEPRECATION")
        val ttlBiConsumer = wrap(biConsumer)
        @Suppress("DEPRECATION")
        assertSame(ttlBiConsumer, wrap(ttlBiConsumer))
        assertSame(ttlBiConsumer, wrapBiConsumer(ttlBiConsumer))
        assertSame(biConsumer, unwrap(ttlBiConsumer))

        val ttlBiConsumer2 = wrapBiConsumer(biConsumer)
        assertSame(ttlBiConsumer2, wrapBiConsumer(ttlBiConsumer2))
        @Suppress("DEPRECATION")
        assertSame(ttlBiConsumer2, wrap(ttlBiConsumer2))
        assertSame(biConsumer, unwrap(ttlBiConsumer2))

        // Function
        val function = Function<String, String> { "" }

        @Suppress("DEPRECATION")
        val ttlFunction = wrap(function)
        @Suppress("DEPRECATION")
        assertSame(ttlFunction, wrap(ttlFunction))
        assertSame(ttlFunction, wrapFunction(ttlFunction))
        assertSame(function, unwrap(ttlFunction))

        val ttlFunction2 = wrapFunction(function)
        assertSame(ttlFunction2, wrapFunction(ttlFunction2))
        @Suppress("DEPRECATION")
        assertSame(ttlFunction2, wrap(ttlFunction2))
        assertSame(function, unwrap(ttlFunction2))

        // BiFunction
        val biFunction = BiFunction<String, String, String> { _, _ -> "" }

        @Suppress("DEPRECATION")
        val ttlBiFunction = wrap(biFunction)
        @Suppress("DEPRECATION")
        assertSame(ttlBiFunction, wrap(ttlBiFunction))
        assertSame(ttlBiFunction, wrapBiFunction(ttlBiFunction))
        assertSame(biFunction, unwrap(ttlBiFunction))

        val ttlBiFunction2 = wrapBiFunction(biFunction)
        assertSame(ttlBiFunction2, wrapBiFunction(ttlBiFunction2))
        @Suppress("DEPRECATION")
        assertSame(ttlBiFunction2, wrap(ttlBiFunction2))
        assertSame(biFunction, unwrap(ttlBiFunction2))
    }

    @Test
    fun test_Supplier() {
        val ttl = TransmittableThreadLocal<String>()

        fun Supplier<String>.ttlWrapThenAsRunnable(): Runnable {
            @Suppress("DEPRECATION")
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
    fun test_Consumer() {
        fun Consumer<String>.ttlWrapThenAsRunnable(): Runnable {
            @Suppress("DEPRECATION")
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
    fun test_BiConsumer() {
        fun BiConsumer<String, String>.ttlWrapThenAsRunnable(): Runnable {
            @Suppress("DEPRECATION")
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
    fun test_Function() {
        fun Function<String, String>.ttlWrapThenAsRunnable(): Runnable {
            @Suppress("DEPRECATION")
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
    fun test_BiFunction() {
        fun BiFunction<String, String, String>.ttlWrapThenAsRunnable(): Runnable {
            @Suppress("DEPRECATION")
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

    @AfterAll
    fun afterAll() {
        executorService.shutdown()
        assertTrue("Fail to shutdown thread pool", executorService.awaitTermination(100, TimeUnit.MILLISECONDS))
    }

    companion object {
        private val executorService = Executors.newFixedThreadPool(3).also { expandThreadPool(it) }
    }
}
