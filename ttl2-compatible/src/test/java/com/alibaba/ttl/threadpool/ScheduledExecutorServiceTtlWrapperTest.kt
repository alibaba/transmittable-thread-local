package com.alibaba.ttl.threadpool

import com.alibaba.*
import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.ttl.testmodel.Call
import com.alibaba.ttl.testmodel.Task
import io.kotest.core.spec.style.AnnotationSpec
import org.junit.Assert.*
import java.util.concurrent.*

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
class ScheduledExecutorServiceTtlWrapperTest : AnnotationSpec() {

    private lateinit var ttlInstances: ConcurrentMap<String, TransmittableThreadLocal<String>>

    @Before
    fun setUp() {
        ttlInstances = createParentTtlInstances(ConcurrentHashMap())
    }

    @After
    fun tearDown() {
        // child do not affect parent
        assertParentTtlValues(copyTtlValues(ttlInstances))
    }

    @Test
    fun test_execute() {
        val task = Task("1", ttlInstances)

        // create after new Task, won't see parent value in in task!
        createParentTtlInstancesAfterCreateChild(ttlInstances)


        executorService.execute(task)

        // child Inheritable
        assertChildTtlValuesWithParentCreateAfterCreateChild("1", task.copied)
    }

    @Test
    fun test_submit() {
        val call = Call("1", ttlInstances)

        // create after new Task, won't see parent value in in task!
        createParentTtlInstancesAfterCreateChild(ttlInstances)


        val future = executorService.submit(call)
        assertEquals("ok", future.get())

        // child Inheritable
        assertChildTtlValuesWithParentCreateAfterCreateChild("1", call.copied)
    }

    @Test
    fun test_submit_runnable_result() {
        val task = Task("1", ttlInstances)

        // create after new Task, won't see parent value in in task!
        createParentTtlInstancesAfterCreateChild(ttlInstances)


        val future = executorService.submit(task, "ok")
        assertEquals("ok", future.get())

        // child Inheritable
        assertChildTtlValuesWithParentCreateAfterCreateChild("1", task.copied)
    }

    @Test
    fun test_submit_runnable_null() {
        val task = Task("1", ttlInstances)

        // create after new Task, won't see parent value in in task!
        createParentTtlInstancesAfterCreateChild(ttlInstances)


        val future = executorService.submit(task)
        assertNull(future.get())

        // child Inheritable
        assertChildTtlValuesWithParentCreateAfterCreateChild("1", task.copied)
    }

    @Test
    fun test_invokeAll() {
        val call1 = Call("1", ttlInstances)
        val call2 = Call("2", ttlInstances)

        // create after new Task, won't see parent value in in task!
        createParentTtlInstancesAfterCreateChild(ttlInstances)


        val futures = executorService.invokeAll(listOf(call1, call2))
        for (future in futures) {
            assertEquals("ok", future.get())
        }

        // child Inheritable
        assertChildTtlValuesWithParentCreateAfterCreateChild("1", call1.copied)
        assertChildTtlValuesWithParentCreateAfterCreateChild("2", call2.copied)
    }

    @Test
    fun test_invokeAll_timeout() {
        val call1 = Call("1", ttlInstances)
        val call2 = Call("2", ttlInstances)

        // create after new Task, won't see parent value in in task!
        createParentTtlInstancesAfterCreateChild(ttlInstances)


        val futures = executorService.invokeAll(listOf(call1, call2), 1, TimeUnit.SECONDS)
        for (future in futures) {
            assertEquals("ok", future.get())
        }

        // child Inheritable
        assertChildTtlValuesWithParentCreateAfterCreateChild("1", call1.copied)
        assertChildTtlValuesWithParentCreateAfterCreateChild("2", call2.copied)
    }

    @Test
    fun test_invokeAny() {
        val call1 = Call("1", ttlInstances)
        val call2 = Call("2", ttlInstances)

        // create after new Task, won't see parent value in in task!
        createParentTtlInstancesAfterCreateChild(ttlInstances)


        val s = executorService.invokeAny(listOf(call1, call2))
        assertEquals("ok", s)

        assertTrue(call1.isCopied || call2.isCopied)
        if (call1.isCopied)
        // child Inheritable
            assertChildTtlValuesWithParentCreateAfterCreateChild("1", call1.copied)
        if (call2.isCopied)
            assertChildTtlValuesWithParentCreateAfterCreateChild("2", call2.copied)
    }

    @Test
    fun test_invokeAny_timeout() {
        val call1 = Call("1", ttlInstances)
        val call2 = Call("2", ttlInstances)

        // create after new Task, won't see parent value in in task!
        createParentTtlInstancesAfterCreateChild(ttlInstances)


        val s = executorService.invokeAny(listOf(call1, call2), 1, TimeUnit.SECONDS)
        assertEquals("ok", s)

        assertTrue(call1.isCopied || call2.isCopied)

        if (call1.isCopied)
        // child Inheritable
            assertChildTtlValuesWithParentCreateAfterCreateChild("1", call1.copied)
        if (call2.isCopied)
            assertChildTtlValuesWithParentCreateAfterCreateChild("2", call2.copied)
    }

    @Test
    fun test_schedule_runnable() {
        val task = Task("1", ttlInstances)

        // create after new Task, won't see parent value in in task!
        createParentTtlInstancesAfterCreateChild(ttlInstances)


        val future = executorService.schedule(task, 100, TimeUnit.MILLISECONDS)
        assertNull(future.get())

        // child Inheritable
        assertChildTtlValuesWithParentCreateAfterCreateChild("1", task.copied)
    }

    @Test
    fun test_schedule_callable() {
        val call = Call("1", ttlInstances)

        // create after new Task, won't see parent value in in task!
        createParentTtlInstancesAfterCreateChild(ttlInstances)

        val future = executorService.schedule(call, 100, TimeUnit.MILLISECONDS)
        assertEquals("ok", future.get())

        // child Inheritable
        assertChildTtlValuesWithParentCreateAfterCreateChild("1", call.copied)
    }

    @Test
    fun test_scheduleAtFixedRate() {
        val task = Task("1", ttlInstances)

        // create after new Task, won't see parent value in in task!
        createParentTtlInstancesAfterCreateChild(ttlInstances)

        val future = executorService.scheduleAtFixedRate(task, 0, 10, TimeUnit.SECONDS)
        Thread.sleep(10)
        future.cancel(true)

        // child Inheritable
        assertChildTtlValuesWithParentCreateAfterCreateChild("1", task.copied)
    }

    @Test
    fun test_scheduleWithFixedDelay() {
        val task = Task("1", ttlInstances)

        // create after new Task, won't see parent value in in task!
        createParentTtlInstancesAfterCreateChild(ttlInstances)


        val future = executorService.scheduleWithFixedDelay(task, 0, 10, TimeUnit.SECONDS)

        Thread.sleep(10)
        future.cancel(true)

        // child Inheritable
        assertChildTtlValuesWithParentCreateAfterCreateChild("1", task.copied)
    }

    @AfterAll
    fun afterAll() {
        executorService.shutdown()
        assertTrue("Fail to shutdown thread pool", executorService.awaitTermination(1, TimeUnit.SECONDS))
    }

    companion object {
        private val executorService: ScheduledExecutorService = ScheduledThreadPoolExecutor(3).let {
            it.setKeepAliveTime(10, TimeUnit.SECONDS)
            expandThreadPool(it)
            TtlExecutors.getTtlScheduledExecutorService(it)
        }!!
    }
}
