package com.alibaba.mtc;

import org.junit.AfterClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;


/**
 * @author ding.lid
 */
public class MtContextCallableTest {
    static ExecutorService executorService = Executors.newFixedThreadPool(3);

    static {
        Utils.expandThreadPool(executorService);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        executorService.shutdown();
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
        MtContextCallable<String> mtContextCallable = MtContextCallable.get(call);
        assertEquals(call, mtContextCallable.getCallable());

        // create after new Task, won't see parent value in in task!
        MtContextThreadLocal<String> after = new MtContextThreadLocal<String>();
        after.set("after");
        mtContexts.put("after", after);

        String ret = mtContextCallable.call();
        assertEquals("ok", ret);

        // Child independent & Inheritable
        assertEquals(4, call.copiedContent.size());
        assertEquals("parent", call.copiedContent.get("parent"));
        assertEquals("p1", call.copiedContent.get("p"));
        assertEquals("after", call.copiedContent.get("after")); // same thread, parent is available from task
        assertEquals("child", call.copiedContent.get("child"));

        // children do not effect parent
        Map<String, Object> copied = Utils.copied(mtContexts);
        assertEquals(4, copied.size());
        assertEquals("parent", copied.get("parent"));
        assertEquals("p", copied.get("p"));
        assertEquals("after", copied.get("after"));
        assertEquals("child", copied.get("child")); // same thread, task set is available from parent 
    }

    @Test
    public void test_MtContextCallable_withExecutorService() throws Exception {
        ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts = new ConcurrentHashMap<String, MtContextThreadLocal<String>>();

        MtContextThreadLocal<String> parent = new MtContextThreadLocal<String>();
        parent.set("parent");
        mtContexts.put("parent", parent);

        MtContextThreadLocal<String> p = new MtContextThreadLocal<String>();
        p.set("p");
        mtContexts.put("p", p);

        Call call = new Call("1", mtContexts);
        MtContextCallable<String> mtContextCallable = MtContextCallable.get(call);
        assertEquals(call, mtContextCallable.getCallable());

        // create after new Task, won't see parent value in in task!
        MtContextThreadLocal<String> after = new MtContextThreadLocal<String>();
        after.set("after");
        mtContexts.put("after", after);

        Future future = executorService.submit(mtContextCallable);
        assertEquals("ok", future.get());

        // Child independent & Inheritable
        assertEquals(3, call.copiedContent.size());
        assertEquals("parent", call.copiedContent.get("parent"));
        assertEquals("p1", call.copiedContent.get("p"));
        assertEquals("child", call.copiedContent.get("child"));

        // children do not effect parent
        Map<String, Object> copied = Utils.copied(mtContexts);
        assertEquals(3, copied.size());
        assertEquals("parent", copied.get("parent"));
        assertEquals("p", copied.get("p"));
        assertEquals("after", copied.get("after"));
    }

    @Test
    public void test_MtContextCallable_copyObject() throws Exception {
    }

    @Test
    public void test_idempotent() throws Exception {
        MtContextCallable<String> call = MtContextCallable.get(new Call("1", null));
        assertSame(call, MtContextCallable.get(call));
    }
}
