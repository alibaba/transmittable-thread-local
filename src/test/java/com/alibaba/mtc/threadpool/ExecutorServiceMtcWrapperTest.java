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
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author ding.lid
 */
public class ExecutorServiceMtcWrapperTest {
    static ExecutorService executorService = new ExecutorServiceMtcWrapper(Executors.newFixedThreadPool(3));

    static {
        Utils.expandThreadPool(executorService);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        executorService.shutdown();
    }

    @Test
    public void test_MtContextRunnable() throws Exception {
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

        // Child independent & Inheritable
        assertEquals(4, task.copiedContent.size());
        assertEquals("parent", task.copiedContent.get("parent"));
        assertEquals("p1", task.copiedContent.get("p"));
        assertEquals("child", task.copiedContent.get("child"));
        assertEquals("after", task.copiedContent.get("after")); // because create MtContextRunnable in method executorService.execute

        // children do not effect parent
        Map<String, Object> copied = Utils.copied(mtContexts);
        assertEquals(3, copied.size());
        assertEquals("parent", copied.get("parent"));
        assertEquals("p", copied.get("p"));
        assertEquals("after", copied.get("after"));
    }

    @Test
    public void test_MtContextCallable() throws Exception {
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

        // Child independent & Inheritable
        assertEquals(4, call.copiedContent.size());
        assertEquals("parent", call.copiedContent.get("parent"));
        assertEquals("p1", call.copiedContent.get("p"));
        assertEquals("child", call.copiedContent.get("child"));
        assertEquals("after", call.copiedContent.get("after")); // because create MtContextCallable in method executorService.execute

        // children do not effect parent
        Map<String, Object> copied = Utils.copied(mtContexts);
        assertEquals(3, copied.size());
        assertEquals("parent", copied.get("parent"));
        assertEquals("p", copied.get("p"));
        assertEquals("after", copied.get("after"));
    }
}
