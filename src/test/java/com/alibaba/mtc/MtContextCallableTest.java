package com.alibaba.mtc;

import org.junit.AfterClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;


/**
 * @author ding.lid
 */
public class MtContextCallableTest {
    static ExecutorService executorService = Executors.newFixedThreadPool(3);

    static {
        MtContext.getContext().set(new HashMap<String, Object>());
        Utils.expandThreadPool(executorService);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        executorService.shutdown();
    }

    @Test
    public void test_MtContextCallable() throws Exception {
        MtContext.getContext().set(new HashMap<String, Object>());
        MtContext.getContext().set("parent", "parent");
        MtContext.getContext().set("p", "p0");

        Call call = new Call("1");
        MtContextCallable<String> mtContextCallable = MtContextCallable.get(call);
        assertEquals(call, mtContextCallable.getCallable());

        MtContext.getContext().set("after", "after");

        String ret = mtContextCallable.call();
        assertEquals("ok", ret);

        // Child independent & Inheritable
        assertEquals(3, call.copiedContent.size());
        assertEquals("1", call.copiedContent.get("key"));
        assertEquals("p01", call.copiedContent.get("p"));
        assertEquals("parent", call.copiedContent.get("parent"));

        // children do not effect parent
        assertEquals(3, MtContext.getContext().get().size());
        assertEquals("parent", MtContext.getContext().get("parent"));
        assertEquals("p0", MtContext.getContext().get("p"));
        assertEquals("after", MtContext.getContext().get("after"));
    }

    @Test
    public void test_MtContextCallable_withExecutorService() throws Exception {
        MtContext.getContext().set(new HashMap<String, Object>());
        MtContext.getContext().set("parent", "parent");
        MtContext.getContext().set("p", "p0");

        Call call = new Call("1");
        MtContextCallable mtContextCallable = MtContextCallable.get(call);
        assertEquals(call, mtContextCallable.getCallable());
        Future future = executorService.submit(mtContextCallable);
        assertEquals("ok", future.get());

        // Child independent & Inheritable
        assertEquals(3, call.copiedContent.size());
        assertEquals("1", call.copiedContent.get("key"));
        assertEquals("p01", call.copiedContent.get("p"));
        assertEquals("parent", call.copiedContent.get("parent"));

        // restored
        assertEquals(0, call.context.get().size());

        // children do not effect parent
        assertEquals(2, MtContext.getContext().get().size());
        assertEquals("parent", MtContext.getContext().get("parent"));
        assertEquals("p0", MtContext.getContext().get("p"));
    }

    @Test
    public void test_MtContextCallable_copyObject() throws Exception {
        MtContext.getContext().set(new HashMap<String, Object>());
        MtContext.getContext().set("parent", "parent");
        MtContext.getContext().set("p", "p0");
        MtContext.getContext().set("foo", new FooContext("parent", 0));

        Call call = new Call("1");
        MtContextCallable mtContextCallable = MtContextCallable.get(call);
        assertEquals(call, mtContextCallable.getCallable());
        executorService.submit(mtContextCallable);

        Future future = executorService.submit(mtContextCallable);
        assertEquals("ok", future.get());

        assertNotSame(call.copiedContent.get("foo"), MtContext.getContext().get("foo"));
        assertEquals(new FooContext("child", 100), call.copiedContent.get("foo"));
        assertEquals(new FooContext("parent", 0), MtContext.getContext().get("foo"));
    }

    @Test
    public void test_idempotent() throws Exception {
        MtContextCallable<String> call = MtContextCallable.get(new Call("1"));
        assertSame(call, MtContextCallable.get(call));
    }
}
