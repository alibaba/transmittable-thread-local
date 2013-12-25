package com.alibaba.mtc.threadpool;

import com.alibaba.mtc.Call;
import com.alibaba.mtc.MtContextThreadLocal;
import com.alibaba.mtc.Task;
import com.alibaba.mtc.Utils;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * @author ding.lid
 */
public class ScheduledExecutorServiceMtcWrapperTest {
    static ExecutorService executorService;

    static {
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(3);
        scheduledThreadPoolExecutor.setKeepAliveTime(1024, TimeUnit.DAYS);
        executorService = MtContextExecutors.getMtcScheduledExecutorService(scheduledThreadPoolExecutor);
        Utils.expandThreadPool(executorService);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        executorService.shutdown();
    }

    @Test
    public void test_execute() throws Exception {
        ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts = new ConcurrentHashMap<String, MtContextThreadLocal<String>>();

        MtContextThreadLocal<String> parent = new MtContextThreadLocal<String>();
        parent.set("parent");
        mtContexts.put("parent", parent);

        MtContextThreadLocal<String> p = new MtContextThreadLocal<String>();
        p.set("p");
        mtContexts.put("p", p);

        Task task = new Task("1", mtContexts);

        // create after new Task
        MtContextThreadLocal<String> after = new MtContextThreadLocal<String>();
        after.set("after");
        mtContexts.put("after", after);

        executorService.execute(task);
        Thread.sleep(100);

        // child Inheritable
        assertEquals(4, task.copied.size());
        assertEquals("parent", task.copied.get("parent"));
        assertEquals("p1", task.copied.get("p"));
        assertEquals("child", task.copied.get("child"));
        assertEquals("after", task.copied.get("after")); // because create MtContextRunnable in method executorService.execute

        // child do not effect parent
        Map<String, Object> copied = Utils.copied(mtContexts);
        assertEquals(3, copied.size());
        assertEquals("parent", copied.get("parent"));
        assertEquals("p", copied.get("p"));
        assertEquals("after", copied.get("after"));
    }

    @Test
    public void test_submit() throws Exception {
        ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts = new ConcurrentHashMap<String, MtContextThreadLocal<String>>();

        MtContextThreadLocal<String> parent = new MtContextThreadLocal<String>();
        parent.set("parent");
        mtContexts.put("parent", parent);

        MtContextThreadLocal<String> p = new MtContextThreadLocal<String>();
        p.set("p");
        mtContexts.put("p", p);

        Call call = new Call("1", mtContexts);

        // create after new Task, won't see parent value in in task!
        MtContextThreadLocal<String> after = new MtContextThreadLocal<String>();
        after.set("after");
        mtContexts.put("after", after);

        Future future = executorService.submit(call);
        assertEquals("ok", future.get());

        // child Inheritable
        assertEquals(4, call.copied.size());
        assertEquals("parent", call.copied.get("parent"));
        assertEquals("p1", call.copied.get("p"));
        assertEquals("child", call.copied.get("child"));
        assertEquals("after", call.copied.get("after")); // because create MtContextCallable in method executorService.execute

        // child do not effect parent
        Map<String, Object> copied = Utils.copied(mtContexts);
        assertEquals(3, copied.size());
        assertEquals("parent", copied.get("parent"));
        assertEquals("p", copied.get("p"));
        assertEquals("after", copied.get("after"));
    }
}
