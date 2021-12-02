package com.alibaba.ttl.threadpool

import com.alibaba.noTtlAgentRun
import com.alibaba.ttl.TtlCallable
import com.alibaba.ttl.TtlRunnable
import com.alibaba.ttl.TtlUnwrap
import com.alibaba.ttl.threadpool.TtlExecutors.*
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.Executor
import java.util.concurrent.Executors.newScheduledThreadPool
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class TtlExecutorsTest {

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
            //     assertTrue(it is TtlRunnableUnwrapComparator)
            //
            // avoid test error under java 11 using TTL Agent:
            //
            // java.lang.IllegalAccessError:
            //   failed to access class com.alibaba.ttl.threadpool.TtlRunnableUnwrapComparator
            //     from class com.alibaba.ttl.threadpool.TtlExecutorsTest
            //   (com.alibaba.ttl.threadpool.TtlRunnableUnwrapComparator is in unnamed module of loader 'bootstrap';
            //     com.alibaba.ttl.threadpool.TtlExecutorsTest is in unnamed module of loader 'app')
            assertEquals("com.alibaba.ttl.threadpool.TtlRunnableUnwrapComparator", it!!.javaClass.name)

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
}
