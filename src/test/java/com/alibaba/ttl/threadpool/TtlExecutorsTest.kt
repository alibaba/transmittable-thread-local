package com.alibaba.ttl.threadpool

import com.alibaba.noTtlAgentRun
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule
import com.alibaba.support.junit.conditional.IsAgentRun
import com.alibaba.ttl.TtlCallable
import com.alibaba.ttl.TtlRunnable
import com.alibaba.ttl.TtlUnwrap
import com.alibaba.ttl.threadpool.TtlExecutors.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.*
import java.util.concurrent.Executors.newScheduledThreadPool
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class TtlExecutorsTest {
    @Rule
    @JvmField
    val rule = ConditionalIgnoreRule()

    ///////////////////////////////////////////////
    // test getTtl*ExecutorService
    ///////////////////////////////////////////////

    @Test
    fun test_getTtlExecutorService__common() {
        val newScheduledThreadPool = newScheduledThreadPool(3)

        getTtlExecutor(newScheduledThreadPool).let {
            if (noTtlAgentRun()) assertTrue(it is ExecutorTtlWrapper)
            assertEquals(noTtlAgentRun(), isTtlWrapper(it))

            assertSame(newScheduledThreadPool, unwrap(it))
            assertSame(newScheduledThreadPool, TtlUnwrap.unwrap(it))
        }
        getTtlExecutorService(newScheduledThreadPool).let {
            if (noTtlAgentRun()) assertTrue(it is ExecutorServiceTtlWrapper)
            assertEquals(noTtlAgentRun(), isTtlWrapper(it))

            assertSame(newScheduledThreadPool, unwrap(it))
            assertSame(newScheduledThreadPool, TtlUnwrap.unwrap(it))
        }
        getTtlScheduledExecutorService(newScheduledThreadPool).let {
            if (noTtlAgentRun()) assertTrue(it is ScheduledExecutorServiceTtlWrapper)
            assertEquals(noTtlAgentRun(), isTtlWrapper(it))

            assertSame(newScheduledThreadPool, unwrap(it))
            assertSame(newScheduledThreadPool, TtlUnwrap.unwrap(it))
        }

        newScheduledThreadPool.shutdown()
    }

    @Test
    fun test_getTtlExecutorService__null() {
        assertNull(getTtlExecutor(null))
        assertNull(getTtlExecutorService(null))
        assertNull(getTtlScheduledExecutorService(null))

        assertFalse(isTtlWrapper(null))
        assertNull(unwrap<Executor>(null))
    }

    @Test
    fun test_getTtlExecutorService_is__idempotent() {
        val newScheduledThreadPool = newScheduledThreadPool(3)

        getTtlExecutor(newScheduledThreadPool)!!.let {
            assertSame(it, getTtlExecutor(it))

            it.execute(TtlRunnable.get { }!!)
        }

        getTtlExecutorService(newScheduledThreadPool)!!.let {
            assertSame(it, getTtlExecutorService(it))

            it.submit(TtlCallable.get { 42 }!!).get()
            it.submit(TtlRunnable.get { }!!, 42).get()
            it.submit(TtlRunnable.get { }!!).get()

            it.invokeAll(listOf(TtlCallable.get { 42 }!!)).map { f -> f.get() }
            it.invokeAll(listOf(TtlCallable.get { 42 }!!), 1, TimeUnit.SECONDS).map { f -> f.get() }

            it.invokeAny(listOf(TtlCallable.get { 42 }!!))
            it.invokeAny(listOf(TtlCallable.get { 42 }!!), 1, TimeUnit.SECONDS)
        }

        getTtlScheduledExecutorService(newScheduledThreadPool)!!.let {
            assertSame(it, getTtlScheduledExecutorService(it))

            it.schedule(TtlRunnable.get { }!!, 1, TimeUnit.MICROSECONDS).get()
            it.schedule(TtlCallable.get { 42 }!!, 1, TimeUnit.MICROSECONDS).get()

            it.scheduleAtFixedRate(TtlRunnable.get { }!!, 0, 1, TimeUnit.MICROSECONDS).cancel(true)
            it.scheduleWithFixedDelay(TtlRunnable.get { }!!, 0, 1, TimeUnit.MICROSECONDS).cancel(true)
        }

        newScheduledThreadPool.shutdown()
    }

    ///////////////////////////////////////////////
    // test getDisableInheritableThreadFactory
    ///////////////////////////////////////////////

    @Test
    fun test_getDisableInheritableThreadFactory__common() {
        val threadFactory = ThreadFactory { Thread(it) }
        getDisableInheritableThreadFactory(threadFactory).let {
            assertTrue(it is DisableInheritableThreadFactory)
            assertTrue(isDisableInheritableThreadFactory(it))

            assertSame(threadFactory, unwrap(it))
            assertSame(threadFactory, TtlUnwrap.unwrap(it))
        }
    }

    @Test
    @Suppress("CAST_NEVER_SUCCEEDS")
    fun test_getDisableInheritableThreadFactory__null() {
        assertNull(getDisableInheritableThreadFactory(null))
        assertFalse(isDisableInheritableThreadFactory(null))
        assertNull(unwrap(null as? ThreadFactory))
    }

    @Test
    fun test_getDisableInheritableThreadFactory__is_idempotent() {
        val threadFactory = ThreadFactory { Thread(it) }

        val disableInheritableThreadFactory = getDisableInheritableThreadFactory(threadFactory)
        assertSame(disableInheritableThreadFactory, getDisableInheritableThreadFactory(disableInheritableThreadFactory))
    }

    ///////////////////////////////////////////////
    // test getTtlRunnableUnwrapComparator
    ///////////////////////////////////////////////

    @Test
    fun test_getTtlRunnableUnwrapComparator__common() {
        val comparator: Comparator<Runnable> =
            Comparator { _, _ -> throw NotImplementedError("An operation is not implemented") }

        getTtlRunnableUnwrapComparator(comparator).let {
            // use class name check instead of type check by
            //     assertTrue(it is TtlUnwrapComparator)
            //
            // avoid test error under java 11 using TTL Agent:
            //
            // java.lang.IllegalAccessError:
            //   failed to access class com.alibaba.ttl.threadpool.TtlUnwrapComparator
            //     from class com.alibaba.ttl.threadpool.TtlExecutorsTest
            //   (com.alibaba.ttl.threadpool.TtlUnwrapComparator is in unnamed module of loader 'bootstrap';
            //     com.alibaba.ttl.threadpool.TtlExecutorsTest is in unnamed module of loader 'app')
            assertEquals("com.alibaba.ttl.threadpool.TtlUnwrapComparator", it!!.javaClass.name)

            assertTrue(isTtlRunnableUnwrapComparator(it))

            assertSame(comparator, unwrap(it))
            assertSame(comparator, TtlUnwrap.unwrap(it))
        }
    }

    @Test
    @Suppress("CAST_NEVER_SUCCEEDS")
    fun test_getTtlRunnableUnwrapComparator__null() {
        assertNull(getTtlRunnableUnwrapComparator(null))
        assertFalse(isTtlRunnableUnwrapComparator(null))
        assertNull(unwrap(null as? java.util.Comparator<Runnable>))
    }

    @Test
    fun test_getTtlRunnableUnwrapComparator__is_idempotent() {
        val comparator: Comparator<Runnable> =
            Comparator { _, _ -> throw NotImplementedError("An operation is not implemented") }

        val ttlRunnableUnwrapComparator = getTtlRunnableUnwrapComparator(comparator)
        assertSame(ttlRunnableUnwrapComparator, getTtlRunnableUnwrapComparator(ttlRunnableUnwrapComparator))
    }

    /**
     * https://github.com/alibaba/transmittable-thread-local/issues/330
     */
    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = IsAgentRun::class)
    fun test_reproduce_ClassCastException_of_issue_330() {
        val priorityBlockingQueue = PriorityBlockingQueue<Runnable>()

        Pair(
            BizComparableTask().also { priorityBlockingQueue.put(it) },
            BizComparableTask().also { priorityBlockingQueue.put(it) },
        ).let { (task0, task1) ->
            assertEquals(task0, priorityBlockingQueue.poll())
            assertEquals(task1, priorityBlockingQueue.poll())
        }

        BizComparableTask().also { priorityBlockingQueue.put(it) }

        try {
            BizComparableTask().also { priorityBlockingQueue.put(TtlRunnable.get(it)!!) }
            fail()
        } catch (e: ClassCastException) {
            assertClassCastException(e, TtlRunnable::class.java, Comparable::class.java)
        }
    }

    @Test
    fun test_fixed_ClassCastException_of_issue_330() {
        val priorityBlockingQueue: BlockingQueue<Runnable> = if (noTtlAgentRun()) {
            // explicit PriorityBlockingQueue arguments
            PriorityBlockingQueue(11, getTtlRunnableUnwrapComparatorForComparableRunnable())
        } else {
            // No PriorityBlockingQueue arguments
            PriorityBlockingQueue()
        }

        Pair(
            BizComparableTask().also { priorityBlockingQueue.put(it) },
            BizComparableTask().let { TtlRunnable.get(it) }!!.also { priorityBlockingQueue.put(it) },
        ).let { (task0, task1) ->
            assertEquals(task0, priorityBlockingQueue.poll())
            assertEquals(task1, priorityBlockingQueue.poll())
        }
    }

    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = IsAgentRun::class)
    fun test_reproduce_ClassCastException_explicit_comparator() {
        val priorityBlockingQueue = PriorityBlockingQueue(11, compareBy<Runnable> { (it as BizOrderTask).order })

        Pair(
            BizOrderTask(1).also { priorityBlockingQueue.put(it) },
            BizOrderTask(2).also { priorityBlockingQueue.put(it) },
        ).let { (task0, task1) ->
            assertEquals(task0, priorityBlockingQueue.poll())
            assertEquals(task1, priorityBlockingQueue.poll())
        }

        BizOrderTask(3).also { priorityBlockingQueue.put(it) }

        try {
            BizOrderTask(4).also { priorityBlockingQueue.put(TtlRunnable.get(it)!!) }
            fail()
        } catch (e: ClassCastException) {
            assertClassCastException(e, TtlRunnable::class.java, BizOrderTask::class.java)
        }
    }

    @Test
    fun test_fixed_ClassCastException_explicit_comparator() {
        val priorityBlockingQueue = PriorityBlockingQueue(11,
            compareBy<Runnable> { (it as BizOrderTask).order }.let {
                if (noTtlAgentRun()) getTtlRunnableUnwrapComparator(it)
                else it
            }
        )

        Pair(
            BizOrderTask(1).also { priorityBlockingQueue.put(it) },
            BizOrderTask(2).let { TtlRunnable.get(it) }.also { priorityBlockingQueue.put(it) },
        ).let { (task0, task1) ->
            assertEquals(task0, priorityBlockingQueue.poll())
            assertEquals(task1, priorityBlockingQueue.poll())
        }
    }

    private fun assertClassCastException(e: ClassCastException, actualClass: Class<*>, targetClass: Class<*>) {
        // java 8
        val msg1 = "${actualClass.name} cannot be cast to ${targetClass.name}"
        // java 11
        val msg2 = "class ${actualClass.name} cannot be cast to class ${targetClass.name}"

        assertTrue(
            "${e.message}, actual: ${actualClass.name}, target: ${targetClass.name}",
            msg1 == e.message || e.message!!.startsWith(msg2)
        )
    }

    /**
     * https://github.com/alibaba/transmittable-thread-local/issues/361
     */
    @Test
    fun test_fixed_ClassCastException_of_issue_361() {
        val queue = PriorityBlockingQueue<Int>()
        queue.put(1)
        queue.put(100)
        queue.put(2)

        assertEquals(1, queue.poll())
        assertEquals(2, queue.poll())
        assertEquals(100, queue.poll())
    }
}

private class BizComparableTask : Runnable, Comparable<Runnable> {
    companion object {
        val counter = AtomicInteger()
    }

    private val num = counter.getAndIncrement()

    override fun run() {
        println("BizComparableTask#run")
    }

    override fun compareTo(other: Runnable): Int = num - (other as BizComparableTask).num
}

private data class BizOrderTask(val order: Int) : Runnable {
    override fun run() {
        println("BizOrderTask#run")
    }
}
