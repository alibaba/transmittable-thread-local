package com.alibaba.ttl3.kotlin

import com.alibaba.expandThreadPool
import com.alibaba.getForTest
import com.alibaba.shutdownForTest
import com.alibaba.ttl3.TransmittableThreadLocal
import com.alibaba.ttl3.TtlCallable
import com.alibaba.ttl3.TtlRunnable
import com.alibaba.ttl3.executor.TtlExecutors
import com.alibaba.ttl3.spi.TtlWrapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldBeTypeOf
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import java.util.concurrent.*
import java.util.function.*
import kotlin.random.Random

class TtlExtensionsTests : FunSpec({

    lateinit var executorService: ExecutorService

    beforeSpec {
        executorService = Executors.newFixedThreadPool(3).let {
            expandThreadPool(it)
            TtlExecutors.getTtlExecutorService(it)!!
        }
    }

    afterSpec {
        executorService.shutdownForTest()
    }


    test("ttl runnable wrap & unwrap") {
        val r = Runnable { }
        r.ttlUnwrap() shouldBeSameInstanceAs r
        r.isTtlWrapper().shouldBeFalse()


        r.ttlWrap().let { wrap ->
            wrap.ttlWrap(idempotent = true) shouldBeSameInstanceAs wrap

            wrap.ttlUnwrap() shouldBeSameInstanceAs r
            wrap.unwrap() shouldBeSameInstanceAs r
            wrap.runnable shouldBeSameInstanceAs r
            (wrap as Any).ttlUnwrap() shouldBeSameInstanceAs r

            wrap shouldNotBeSameInstanceAs r.ttlWrap()
            wrap shouldBe r.ttlWrap()

            wrap.isTtlWrapper().shouldBeTrue()

            wrap.shouldBeTypeOf<TtlRunnable>()
        }

        shouldThrow<IllegalStateException> {
            r.ttlWrap().ttlWrap()
        }.message shouldContain "Already TtlRunnable"
    }

    test("ttl runnable wraps") {
        val r1 = Runnable {}
        val r2 = Runnable {}
        val r3 = Runnable {}

        val list = listOf(r1, r2, r3).ttlWrap()

        list.shouldHaveSize(3)
        list.forAll { it.shouldBeTypeOf<TtlRunnable>() }

        val nullableRunnableList = listOf(r1, r2, null, r3).ttlWrap()
        nullableRunnableList.shouldHaveSize(4)

        nullableRunnableList[0].shouldBeTypeOf<TtlRunnable>()
        nullableRunnableList[1].shouldBeTypeOf<TtlRunnable>()
        nullableRunnableList[2].shouldBeNull()
        nullableRunnableList[3].shouldBeTypeOf<TtlRunnable>()
    }

    test("ttl runnable unwraps") {
        val r1 = Runnable {}
        val r2 = Runnable {}
        val r3 = Runnable {}

        listOf(null as Runnable?).ttlUnwrap().shouldContainInOrder(null)

        listOf(r1).ttlUnwrap().shouldContainInOrder(r1)
        listOf(r1.ttlWrap()).ttlUnwrap().shouldContainInOrder(r1)

        listOf(r1, r2, r3).ttlUnwrap().shouldContainInOrder(r1, r2, r3)
        listOf(r1, r2, r3).ttlWrap().ttlUnwrap().shouldContainInOrder(r1, r2, r3)

        listOf(r1, r2.ttlWrap(), r3).ttlUnwrap()
            .shouldContainInOrder(r1, r2, r3)
        listOf(r1, r2.ttlWrap(), null, r3).ttlUnwrap()
            .shouldContainInOrder(r1, r2, null, r3)

        (null as Collection<Runnable>?).ttlUnwrap().shouldBeEmpty()
        (null as Collection<Runnable?>?).ttlUnwrap().shouldBeEmpty()
    }

    test("ttl callable wrap & unwrap") {
        val c = Callable { 42 }
        c.ttlUnwrap() shouldBeSameInstanceAs c
        c.isTtlWrapper().shouldBeFalse()


        c.ttlWrap().let { wrap ->
            wrap.ttlWrap(idempotent = true) shouldBeSameInstanceAs wrap

            wrap.ttlUnwrap() shouldBeSameInstanceAs c
            wrap.unwrap() shouldBeSameInstanceAs c
            wrap.callable shouldBeSameInstanceAs c
            (wrap as Any).ttlUnwrap() shouldBeSameInstanceAs c

            wrap shouldNotBeSameInstanceAs c.ttlWrap()
            wrap shouldBe c.ttlWrap()

            wrap.isTtlWrapper().shouldBeTrue()

            wrap.shouldBeTypeOf<TtlCallable<*>>()
        }

        shouldThrow<IllegalStateException> {
            c.ttlWrap().ttlWrap()
        }.message shouldContain "Already TtlCallable"
    }

    test("ttl callable wraps") {
        val c1 = Callable { 1 }
        val c2 = Callable { 2 }
        val c3 = Callable { 3 }

        val list = listOf(c1, c2, c3).ttlWrap()

        list.shouldHaveSize(3)
        list.forAll { it.shouldBeTypeOf<TtlCallable<*>>() }

        val nullableCallableList = listOf(c1, c2, null, c3).ttlWrap()
        nullableCallableList.shouldHaveSize(4)

        nullableCallableList[0].shouldBeTypeOf<TtlCallable<*>>()
        nullableCallableList[1].shouldBeTypeOf<TtlCallable<*>>()
        nullableCallableList[2].shouldBeNull()
        nullableCallableList[3].shouldBeTypeOf<TtlCallable<*>>()
    }

    test("ttl callable unwraps") {
        val c1 = Callable { 1 }
        val c2 = Callable { 2 }
        val c3 = Callable { 3 }

        listOf(null as Callable<Int>?).shouldContainInOrder(null)

        listOf(c1).ttlUnwrap().shouldContainInOrder(c1)
        listOf(c1.ttlWrap()).ttlUnwrap().shouldContainInOrder(c1)

        listOf(c1, c2, c3).ttlUnwrap().shouldContainInOrder(c1, c2, c3)
        listOf(c1, c2, c3).ttlWrap().ttlUnwrap().shouldContainInOrder(c1, c2, c3)

        listOf(c1, c2.ttlWrap(), c3).ttlUnwrap()
            .shouldContainInOrder(c1, c2, c3)
        listOf(c1, c2.ttlWrap(), null, c3).ttlUnwrap()
            .shouldContainInOrder(c1, c2, null, c3)

        (null as Collection<Callable<Int>>?).ttlUnwrap().shouldBeEmpty()
        (null as Collection<Callable<Int>?>?).ttlUnwrap().shouldBeEmpty()
    }


    val ttl = TransmittableThreadLocal<String>()
    val parentValue = "parent start ${Random.nextLong()}"

    fun checkLogicInBody() {
        ttl.get() shouldBe parentValue
        ttl.set("child ${Random.nextLong()}")
    }

    fun checkLogicAfterRun(task: Runnable) {
        executorService.submit(task).getForTest()
        ttl.get() shouldBe parentValue
    }

    test("java common functional interface") {
        ttl.set(parentValue)


        val supplier = Supplier {
            checkLogicInBody()
            "Hello"
        }
        supplier.let {
            val w = it.ttlWrap()
            w.isTtlWrapper().shouldBeTrue()
            (w as Any).ttlUnwrap() shouldBeSameInstanceAs it
        }
        checkLogicAfterRun {
            supplier.get()
        }

        val consumer = Consumer<String> {
            checkLogicInBody()
        }
        consumer.let {
            val w = it.ttlWrap()
            w.isTtlWrapper().shouldBeTrue()
            (w as Any).ttlUnwrap() shouldBeSameInstanceAs it
        }
        checkLogicAfterRun {
            consumer.accept("")
        }

        val biConsumer = BiConsumer<String, String> { _, _ ->
            checkLogicInBody()
        }
        biConsumer.let {
            val w = it.ttlWrap()
            w.isTtlWrapper().shouldBeTrue()
            (w as Any).ttlUnwrap() shouldBeSameInstanceAs it
        }
        checkLogicAfterRun {
            biConsumer.accept("", "")
        }

        val function = Function<String, Unit> {
            checkLogicInBody()
        }
        function.let {
            val w = it.ttlWrap()
            w.isTtlWrapper().shouldBeTrue()
            (w as Any).ttlUnwrap() shouldBeSameInstanceAs it
        }
        checkLogicAfterRun {
            function.apply("")
        }

        val biFunction = BiFunction<String, String, Unit> { _, _ ->
            checkLogicInBody()
        }
        biFunction.let {
            val w = it.ttlWrap()
            w.isTtlWrapper().shouldBeTrue()
            (w as Any).ttlUnwrap() shouldBeSameInstanceAs it
        }
        checkLogicAfterRun {
            biFunction.apply("", "")
        }
    }


    test("kotlin function types") {
        ttl.set(parentValue)


        val f0: () -> Unit = { checkLogicInBody() }
        f0.let {
            val w = it.ttlWrap()
            w.isTtlWrapper().shouldBeTrue()
            (w as Any).ttlUnwrap() shouldBeSameInstanceAs it
        }
        checkLogicAfterRun(f0)

        val f1: (String) -> Unit = { checkLogicInBody() }
        f1.let {
            val w = it.ttlWrap()
            w.isTtlWrapper().shouldBeTrue()
            (w as Any).ttlUnwrap() shouldBeSameInstanceAs it
        }
        checkLogicAfterRun { f1("") }

        val f2: (String, Int) -> Unit = { _, _ -> checkLogicInBody() }
        f2.let {
            val w = it.ttlWrap()
            w.isTtlWrapper().shouldBeTrue()
            (w as Any).ttlUnwrap() shouldBeSameInstanceAs it
        }
        checkLogicAfterRun { f2("", 1) }

        val f3: (String, Int, Double) -> Unit = { _, _, _ -> checkLogicInBody() }
        f3.let {
            val w = it.ttlWrap()
            w.isTtlWrapper().shouldBeTrue()
            (w as Any).ttlUnwrap() shouldBeSameInstanceAs it
        }
        checkLogicAfterRun { f3("", 1, 1.0) }

        val f4: (String, Int, Double, Regex) -> Unit = { _, _, _, _ -> checkLogicInBody() }
        f4.let {
            val w = it.ttlWrap()
            w.isTtlWrapper().shouldBeTrue()
            (w as Any).ttlUnwrap() shouldBeSameInstanceAs it
        }
        checkLogicAfterRun { f4("", 1, 1.0, Regex(".")) }
    }

    test("executor wrap & unwrap") {
        val executor: Executor = Executors.newCachedThreadPool()
        executor.ttlUnwrap() shouldBeSameInstanceAs executor
        executor.isTtlExecutor().shouldBeFalse()
        executor.isTtlWrapper().shouldBeFalse()

        executor.ttlWrap().let { wrap ->
            wrap.ttlWrap() shouldBeSameInstanceAs wrap

            wrap.ttlUnwrap() shouldBeSameInstanceAs executor
            (wrap as Any).ttlUnwrap() shouldBeSameInstanceAs executor

            wrap.isTtlExecutor().shouldBeTrue()
            wrap.isTtlWrapper().shouldBeTrue()

            wrap.shouldBeInstanceOf<TtlWrapper<*>>()
        }


        val es: ExecutorService = Executors.newCachedThreadPool()
        es.ttlUnwrap() shouldBeSameInstanceAs es
        es.isTtlExecutor().shouldBeFalse()
        es.isTtlWrapper().shouldBeFalse()

        es.ttlWrap().let { wrap: ExecutorService ->
            wrap.ttlWrap() shouldBeSameInstanceAs wrap

            wrap.ttlUnwrap() shouldBeSameInstanceAs es
            (wrap as Any).ttlUnwrap() shouldBeSameInstanceAs es

            wrap.isTtlExecutor().shouldBeTrue()
            wrap.isTtlWrapper().shouldBeTrue()

            wrap.shouldBeInstanceOf<TtlWrapper<*>>()
        }


        val scheduledExecutorService: ScheduledExecutorService = ScheduledThreadPoolExecutor(1)
        scheduledExecutorService.ttlUnwrap() shouldBeSameInstanceAs scheduledExecutorService
        scheduledExecutorService.isTtlExecutor().shouldBeFalse()
        scheduledExecutorService.isTtlWrapper().shouldBeFalse()

        scheduledExecutorService.ttlWrap().let { wrap ->
            wrap.ttlWrap() shouldBeSameInstanceAs wrap

            wrap.ttlUnwrap() shouldBeSameInstanceAs scheduledExecutorService
            (wrap as Any).ttlUnwrap() shouldBeSameInstanceAs scheduledExecutorService

            wrap.isTtlExecutor().shouldBeTrue()
            wrap.isTtlWrapper().shouldBeTrue()

            wrap.shouldBeInstanceOf<TtlWrapper<*>>()
        }
    }

    test("DisableInheritableThreadFactory") {
        val factory = Executors.defaultThreadFactory()
        factory.ttlUnwrap() shouldBeSameInstanceAs factory
        factory.ttlUnwrapDisableInheritableThreadFactory() shouldBeSameInstanceAs factory

        factory.isDisableInheritableThreadFactory().shouldBeFalse()
        factory.isTtlWrapper().shouldBeFalse()

        factory.ttlWrapToDisableInheritableThreadFactory().let { wrap ->
            wrap.ttlWrapToDisableInheritableThreadFactory() shouldBeSameInstanceAs wrap

            wrap.ttlUnwrapDisableInheritableThreadFactory() shouldBeSameInstanceAs factory
            wrap.ttlUnwrap() shouldBeSameInstanceAs factory
            (wrap as Any).ttlUnwrap() shouldBeSameInstanceAs factory

            wrap.isDisableInheritableThreadFactory().shouldBeTrue()
            wrap.isTtlWrapper().shouldBeTrue()

            wrap.shouldBeInstanceOf<TtlWrapper<*>>()
        }


        getDefaultDisableInheritableThreadFactory().let { wrap ->
            wrap.ttlWrapToDisableInheritableThreadFactory() shouldBeSameInstanceAs wrap

            wrap.ttlUnwrapDisableInheritableThreadFactory().javaClass shouldBe factory.javaClass
            wrap.ttlUnwrap().javaClass shouldBe factory.javaClass
            (wrap as Any).ttlUnwrap().javaClass shouldBe factory.javaClass

            wrap.isDisableInheritableThreadFactory().shouldBeTrue()
            wrap.isTtlWrapper().shouldBeTrue()

            wrap.shouldBeInstanceOf<TtlWrapper<*>>()
        }
    }

    test("DisableInheritableForkJoinWorkerThreadFactory") {
        val factory = ForkJoinPool.defaultForkJoinWorkerThreadFactory
        factory.ttlUnwrap() shouldBeSameInstanceAs factory
        factory.ttlUnwrapDisableInheritableForkJoinWorkerThreadFactory() shouldBeSameInstanceAs factory

        factory.isDisableInheritableForkJoinWorkerThreadFactory().shouldBeFalse()
        factory.isTtlWrapper().shouldBeFalse()

        factory.ttlWrapToDisableInheritableForkJoinWorkerThreadFactory().let { wrap ->
            wrap.ttlWrapToDisableInheritableForkJoinWorkerThreadFactory() shouldBeSameInstanceAs wrap

            wrap.ttlUnwrapDisableInheritableForkJoinWorkerThreadFactory() shouldBeSameInstanceAs factory
            wrap.ttlUnwrap() shouldBeSameInstanceAs factory
            (wrap as Any).ttlUnwrap() shouldBeSameInstanceAs factory

            wrap.isDisableInheritableForkJoinWorkerThreadFactory().shouldBeTrue()
            wrap.isTtlWrapper().shouldBeTrue()

            wrap.shouldBeInstanceOf<TtlWrapper<*>>()
        }

        getDefaultDisableInheritableForkJoinWorkerThreadFactory().let { wrap ->
            wrap.ttlWrapToDisableInheritableForkJoinWorkerThreadFactory() shouldBeSameInstanceAs wrap

            wrap.ttlUnwrapDisableInheritableForkJoinWorkerThreadFactory().javaClass shouldBe factory.javaClass
            wrap.ttlUnwrap().javaClass shouldBe factory.javaClass
            (wrap as Any).ttlUnwrap().javaClass shouldBe factory.javaClass

            wrap.isDisableInheritableForkJoinWorkerThreadFactory().shouldBeTrue()
            wrap.isTtlWrapper().shouldBeTrue()

            wrap.shouldBeInstanceOf<TtlWrapper<*>>()
        }
    }

    test("TtlRunnableUnwrapComparator") {
        val comparator: Comparator<Runnable> = Comparator.comparing { it.hashCode() }
        comparator.ttlUnwrap() shouldBeSameInstanceAs comparator
        comparator.ttlUnwrapTtlRunnableUnwrapComparator() shouldBeSameInstanceAs comparator

        comparator.isTtlRunnableUnwrapComparator().shouldBeFalse()
        comparator.isTtlWrapper().shouldBeFalse()

        comparator.ttlWrapToTtlRunnableUnwrapComparator().let { wrap ->
            wrap.ttlWrapToTtlRunnableUnwrapComparator() shouldBeSameInstanceAs wrap

            wrap.ttlUnwrapTtlRunnableUnwrapComparator() shouldBeSameInstanceAs comparator
            wrap.ttlUnwrap() shouldBeSameInstanceAs comparator
            (wrap as Any).ttlUnwrap() shouldBeSameInstanceAs comparator

            wrap.isTtlRunnableUnwrapComparator().shouldBeTrue()
            wrap.isTtlWrapper().shouldBeTrue()

            wrap.shouldBeInstanceOf<TtlWrapper<*>>()
        }


        getTtlRunnableUnwrapComparatorForComparableRunnable().let { wrap ->
            wrap.ttlWrapToTtlRunnableUnwrapComparator() shouldBeSameInstanceAs wrap

            wrap.ttlUnwrapTtlRunnableUnwrapComparator().javaClass.name shouldBe "com.alibaba.ttl3.executor.ComparableComparator"
            wrap.ttlUnwrapTtlRunnableUnwrapComparator() shouldBeSameInstanceAs wrap.ttlUnwrapTtlRunnableUnwrapComparator()
            wrap.ttlUnwrap() shouldBeSameInstanceAs wrap.ttlUnwrapTtlRunnableUnwrapComparator()
            (wrap as Any).ttlUnwrap() shouldBeSameInstanceAs wrap.ttlUnwrapTtlRunnableUnwrapComparator()

            wrap.isTtlRunnableUnwrapComparator().shouldBeTrue()
            wrap.isTtlWrapper().shouldBeTrue()

            wrap.shouldBeInstanceOf<TtlWrapper<*>>()
        }
    }
})
