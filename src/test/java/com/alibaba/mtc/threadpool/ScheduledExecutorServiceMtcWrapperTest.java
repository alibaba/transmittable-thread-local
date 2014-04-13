package com.alibaba.mtc.threadpool;

import com.alibaba.mtc.testmodel.Call;
import com.alibaba.mtc.MtContextThreadLocal;
import com.alibaba.mtc.testmodel.Task;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import static com.alibaba.mtc.Utils.CHILD;
import static com.alibaba.mtc.Utils.PARENT_AFTER_CREATE_MTC_TASK;
import static com.alibaba.mtc.Utils.PARENT_MODIFIED_IN_CHILD;
import static com.alibaba.mtc.Utils.PARENT_UNMODIFIED_IN_CHILD;
import static com.alibaba.mtc.Utils.copied;
import static com.alibaba.mtc.Utils.createTestMtContexts;
import static com.alibaba.mtc.Utils.expandThreadPool;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author ding.lid
 */
public class ScheduledExecutorServiceMtcWrapperTest {
    static ScheduledExecutorService executorService;

    static {
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(3);
        scheduledThreadPoolExecutor.setKeepAliveTime(1024, TimeUnit.DAYS);
        executorService = MtContextExecutors.getMtcScheduledExecutorService(scheduledThreadPoolExecutor);
        expandThreadPool(executorService);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        executorService.shutdown();
    }

    ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts;

    @Before
    public void setUp() throws Exception {
        mtContexts = createTestMtContexts();
    }

    static void assertTask1(Map<String, Object> copied) {
        // child Inheritable
        assertEquals(4, copied.size());
        assertEquals(PARENT_UNMODIFIED_IN_CHILD, copied.get(PARENT_UNMODIFIED_IN_CHILD));
        assertEquals(PARENT_MODIFIED_IN_CHILD + "1", copied.get(PARENT_MODIFIED_IN_CHILD));
        assertEquals(CHILD + "1", copied.get(CHILD + "1"));
        assertEquals(PARENT_AFTER_CREATE_MTC_TASK, copied.get(PARENT_AFTER_CREATE_MTC_TASK)); // because create MtContextRunnable in method executorService
    }

    static void assertTask2(Map<String, Object> copied) {
        // child Inheritable
        assertEquals(4, copied.size());
        assertEquals(PARENT_UNMODIFIED_IN_CHILD, copied.get(PARENT_UNMODIFIED_IN_CHILD));
        assertEquals(PARENT_MODIFIED_IN_CHILD + "2", copied.get(PARENT_MODIFIED_IN_CHILD));
        assertEquals(CHILD + "2", copied.get(CHILD + "2"));
        assertEquals(PARENT_AFTER_CREATE_MTC_TASK, copied.get(PARENT_AFTER_CREATE_MTC_TASK)); // because create MtContextRunnable in method executorService
    }


    @After
    public void tearDown() throws Exception {
        // child do not effect parent
        Map<String, Object> thisThreadCopied = copied(mtContexts);
        assertEquals(3, thisThreadCopied.size());
        assertEquals(PARENT_UNMODIFIED_IN_CHILD, thisThreadCopied.get(PARENT_UNMODIFIED_IN_CHILD));
        assertEquals(PARENT_MODIFIED_IN_CHILD, thisThreadCopied.get(PARENT_MODIFIED_IN_CHILD));
        assertEquals(PARENT_AFTER_CREATE_MTC_TASK, thisThreadCopied.get(PARENT_AFTER_CREATE_MTC_TASK));
    }

    private void setLocalAfter() {
        // create after new Task, won't see parent value in in task!
        MtContextThreadLocal<String> after = new MtContextThreadLocal<String>();
        after.set(PARENT_AFTER_CREATE_MTC_TASK);
        mtContexts.put(PARENT_AFTER_CREATE_MTC_TASK, after);
    }

    @Test
    public void test_execute() throws Exception {
        Task task = new Task("1", mtContexts);

        setLocalAfter();

        executorService.execute(task);
        Thread.sleep(100);

        assertTask1(task.copied);
    }

    @Test
    public void test_submit() throws Exception {
        Call call = new Call("1", mtContexts);

        // create after new Task, won't see parent value in in task!
        setLocalAfter();

        Future future = executorService.submit(call);
        assertEquals("ok", future.get());

        assertTask1(call.copied);
    }

    @Test
    public void test_submit_runnable_result() throws Exception {
        Task task = new Task("1", mtContexts);

        setLocalAfter();

        Future future = executorService.submit(task, "ok");
        assertEquals("ok", future.get());

        assertTask1(task.copied);
    }

    @Test
    public void test_submit_runnable_null() throws Exception {
        Task task = new Task("1", mtContexts);

        setLocalAfter();

        Future future = executorService.submit(task);
        assertNull(future.get());

        // child Inheritable
        assertTask1(task.copied);
    }

    @Test
    public void test_invokeAll() throws Exception {
        Call call1 = new Call("1", mtContexts);
        Call call2 = new Call("2", mtContexts);

        setLocalAfter();

        List<Future<String>> futures = executorService.invokeAll(Arrays.asList(call1, call2));
        for (Future<String> future : futures) {
            assertEquals("ok", future.get());
        }

        assertTask1(call1.copied);
        assertTask2(call2.copied);
    }

    @Test
    public void test_invokeAll_timeout() throws Exception {
        Call call1 = new Call("1", mtContexts);
        Call call2 = new Call("2", mtContexts);

        setLocalAfter();

        List<Future<String>> futures = executorService.invokeAll(Arrays.asList(call1, call2), 10, TimeUnit.SECONDS);
        for (Future<String> future : futures) {
            assertEquals("ok", future.get());
        }

        assertTask1(call1.copied);
        assertTask2(call2.copied);
    }

    @Test
    public void test_invokeAny() throws Exception {
        Call call1 = new Call("1", mtContexts);
        Call call2 = new Call("2", mtContexts);

        setLocalAfter();

        String s = executorService.invokeAny(Arrays.asList(call1, call2));
        assertEquals("ok", s);

        Thread.sleep(1000);
        assertTask1(call1.copied);
        assertTask2(call2.copied);
    }

    @Test
    public void test_invokeAny_timeout() throws Exception {
        Call call1 = new Call("1", mtContexts);
        Call call2 = new Call("2", mtContexts);

        setLocalAfter();

        String s = executorService.invokeAny(Arrays.asList(call1, call2), 10, TimeUnit.SECONDS);
        assertEquals("ok", s);

        Thread.sleep(1000);
        assertTask1(call1.copied);
        assertTask2(call2.copied);
    }

    @Test
    public void test_schedule_runnable() throws Exception {
        Task task = new Task("1", mtContexts);

        setLocalAfter();

        Future future = executorService.schedule(task, 1, TimeUnit.SECONDS);
        assertNull(future.get());

        // child Inheritable
        assertTask1(task.copied);
    }

    @Test
    public void test_schedule_callable() throws Exception {
        Call call = new Call("1", mtContexts);

        // create after new Task, won't see parent value in in task!
        setLocalAfter();

        Future future = executorService.schedule(call, 1, TimeUnit.SECONDS);
        assertEquals("ok", future.get());

        assertTask1(call.copied);
    }

    @Test
    public void test_scheduleAtFixedRate() throws Exception {
        Task task = new Task("1", mtContexts);

        setLocalAfter();

        Future future = executorService.scheduleAtFixedRate(task, 0, 100, TimeUnit.SECONDS);

        Thread.sleep(1000);
        future.cancel(true);

        assertTask1(task.copied);
    }

    @Test
    public void test_scheduleWithFixedDelay() throws Exception {
        Task task = new Task("1", mtContexts);

        setLocalAfter();

        Future future = executorService.scheduleWithFixedDelay(task, 0, 100, TimeUnit.SECONDS);

        Thread.sleep(1000);
        future.cancel(true);

        assertTask1(task.copied);
    }
}
