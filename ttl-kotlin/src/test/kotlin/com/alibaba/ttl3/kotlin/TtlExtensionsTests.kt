package com.alibaba.ttl3.kotlin

import com.alibaba.ttl3.TtlCallable
import com.alibaba.ttl3.TtlRunnable
import com.alibaba.ttl3.spi.TtlWrapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forAll
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

class TtlExtensionsTests : FunSpec({

    test("ttl runnable wrap & unwrap") {
        val r = Runnable { }

        val ttlRunnable = r.ttlWrap()
        ttlRunnable.shouldBeTypeOf<TtlRunnable>()

        ttlRunnable.ttlWrap(idempotent = true) shouldBeSameInstanceAs ttlRunnable
        shouldThrow<IllegalStateException> {
            ttlRunnable.ttlWrap()
        }.message shouldContain "Already TtlRunnable"
        r.ttlWrap().let {
            it shouldNotBeSameInstanceAs ttlRunnable
            it shouldBe ttlRunnable
        }

        ttlRunnable.ttlUnwrap() shouldBeSameInstanceAs r
        ttlRunnable.runnable shouldBeSameInstanceAs r
        r.ttlUnwrap() shouldBeSameInstanceAs r
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

        val ttlCallable = c.ttlWrap()
        ttlCallable.shouldBeTypeOf<TtlCallable<*>>()

        ttlCallable.ttlWrap(idempotent = true) shouldBeSameInstanceAs ttlCallable
        shouldThrow<IllegalStateException> {
            ttlCallable.ttlWrap()
        }.message shouldContain "Already TtlCallable"
        c.ttlWrap().let {
            it shouldNotBeSameInstanceAs ttlCallable
            it shouldBe ttlCallable
        }

        ttlCallable.ttlUnwrap() shouldBeSameInstanceAs c
        ttlCallable.callable shouldBeSameInstanceAs c
        c.ttlUnwrap() shouldBeSameInstanceAs c
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

    test("executor wrap & unwrap") {
        val executor: Executor = Executors.newCachedThreadPool()
        executor.ttlWrap().let { wrap ->
            wrap.ttlWrap() shouldBeSameInstanceAs wrap
            wrap.ttlUnwrap() shouldBeSameInstanceAs executor

            wrap.isTtlExecutor().shouldBeTrue()
            wrap.shouldBeInstanceOf<TtlWrapper<*>>()
        }

        val executorService: ExecutorService = Executors.newCachedThreadPool()
        executorService.ttlWrap().let { wrap: ExecutorService ->
            wrap.ttlWrap() shouldBeSameInstanceAs wrap
            wrap.ttlUnwrap() shouldBeSameInstanceAs executorService

            wrap.isTtlExecutor().shouldBeTrue()
            wrap.shouldBeInstanceOf<TtlWrapper<*>>()
        }

        val scheduledExecutorService: ScheduledExecutorService = ScheduledThreadPoolExecutor(1)
        scheduledExecutorService.ttlWrap().let { wrap ->
            wrap.ttlWrap() shouldBeSameInstanceAs wrap
            wrap.ttlUnwrap() shouldBeSameInstanceAs scheduledExecutorService

            wrap.isTtlExecutor().shouldBeTrue()
            wrap.shouldBeInstanceOf<TtlWrapper<*>>()
        }
    }

    test("DisableInheritableThreadFactory") {
        val factory = Executors.defaultThreadFactory()
        factory.ttlWrapAsDisableInheritableThreadFactory().let { wrap ->
            wrap.ttlWrapAsDisableInheritableThreadFactory() shouldBeSameInstanceAs wrap
            wrap.ttlUnwrapDisableInheritableThreadFactory() shouldBeSameInstanceAs factory

            wrap.isDisableInheritableThreadFactory().shouldBeTrue()
            wrap.shouldBeInstanceOf<TtlWrapper<*>>()
        }

        getDefaultDisableInheritableThreadFactory().let { wrap ->
            wrap.ttlWrapAsDisableInheritableThreadFactory() shouldBeSameInstanceAs wrap
            wrap.ttlUnwrapDisableInheritableThreadFactory().javaClass shouldBe factory.javaClass

            wrap.isDisableInheritableThreadFactory().shouldBeTrue()
            wrap.shouldBeInstanceOf<TtlWrapper<*>>()
        }
    }

    test("DisableInheritableForkJoinWorkerThreadFactory") {
        val factory = ForkJoinPool.defaultForkJoinWorkerThreadFactory
        factory.ttlWrapAsDisableInheritableForkJoinWorkerThreadFactory().let { wrap ->
            wrap.ttlWrapAsDisableInheritableForkJoinWorkerThreadFactory() shouldBeSameInstanceAs wrap
            wrap.ttlUnwrapDisableInheritableForkJoinWorkerThreadFactory() shouldBeSameInstanceAs factory

            wrap.isDisableInheritableForkJoinWorkerThreadFactory().shouldBeTrue()
            wrap.shouldBeInstanceOf<TtlWrapper<*>>()
        }

        getDefaultDisableInheritableForkJoinWorkerThreadFactory().let { wrap ->
            wrap.ttlWrapAsDisableInheritableForkJoinWorkerThreadFactory() shouldBeSameInstanceAs wrap
            wrap.ttlUnwrapDisableInheritableForkJoinWorkerThreadFactory().javaClass shouldBe factory.javaClass

            wrap.isDisableInheritableForkJoinWorkerThreadFactory().shouldBeTrue()
            wrap.shouldBeInstanceOf<TtlWrapper<*>>()
        }
    }

    test("TtlRunnableUnwrapComparator") {
        val comparator: Comparator<Runnable> = Comparator.comparing { it.hashCode() }

        comparator.ttlWrapAsTtlRunnableUnwrapComparator().let { wrap ->
            wrap.ttlWrapAsTtlRunnableUnwrapComparator() shouldBeSameInstanceAs wrap

            wrap.ttlUnwrapTtlRunnableUnwrapComparator() shouldBeSameInstanceAs comparator

            wrap.isTtlRunnableUnwrapComparator().shouldBeTrue()
            wrap.shouldBeInstanceOf<TtlWrapper<*>>()
        }

        getTtlRunnableUnwrapComparatorForComparableRunnable().let { wrap ->
            wrap.ttlWrapAsTtlRunnableUnwrapComparator() shouldBeSameInstanceAs wrap

            wrap.ttlUnwrapTtlRunnableUnwrapComparator().javaClass.name shouldBe "com.alibaba.ttl3.executor.ComparableComparator"
            wrap.ttlUnwrapTtlRunnableUnwrapComparator() shouldBeSameInstanceAs wrap.ttlUnwrapTtlRunnableUnwrapComparator()

            wrap.isTtlRunnableUnwrapComparator().shouldBeTrue()
            wrap.shouldBeInstanceOf<TtlWrapper<*>>()
        }
    }
})
