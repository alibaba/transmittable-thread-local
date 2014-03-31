package com.alibaba.mtc;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.AfterClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


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
        assertSame(call, mtContextCallable.getCallable());

        // create after new Task, won't see parent value in in task!
        MtContextThreadLocal<String> after = new MtContextThreadLocal<String>();
        after.set("after");
        mtContexts.put("after", after);

        String ret = mtContextCallable.call();
        assertEquals("ok", ret);

        // child Inheritable
        assertEquals(4, call.copied.size());
        assertEquals("parent", call.copied.get("parent"));
        assertEquals("p1", call.copied.get("p"));
        assertEquals("after", call.copied.get("after")); // same thread, parent is available from task
        assertEquals("child", call.copied.get("child"));

        // child do not effect parent
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
        assertSame(call, mtContextCallable.getCallable());

        // create after new Task, won't see parent value in in task!
        MtContextThreadLocal<String> after = new MtContextThreadLocal<String>();
        after.set("after");
        mtContexts.put("after", after);

        Future future = executorService.submit(mtContextCallable);
        assertEquals("ok", future.get());

        // child Inheritable
        assertEquals(3, call.copied.size());
        assertEquals("parent", call.copied.get("parent"));
        assertEquals("p1", call.copied.get("p"));
        assertEquals("child", call.copied.get("child"));

        // child do not effect parent
        Map<String, Object> copied = Utils.copied(mtContexts);
        assertEquals(3, copied.size());
        assertEquals("parent", copied.get("parent"));
        assertEquals("p", copied.get("p"));
        assertEquals("after", copied.get("after"));
    }

    @Test
    public void test_releaseMtContextAfterCall() throws Exception {
        ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts = new ConcurrentHashMap<String, MtContextThreadLocal<String>>();

        MtContextThreadLocal<String> parent = new MtContextThreadLocal<String>();
        parent.set("parent");
        mtContexts.put("parent", parent);

        MtContextThreadLocal<String> p = new MtContextThreadLocal<String>();
        p.set("p");
        mtContexts.put("p", p);

        Call call = new Call("1", mtContexts);
        MtContextCallable<String> mtContextCallable = MtContextCallable.get(call, true);
        assertSame(call, mtContextCallable.getCallable());

        // create after new Task, won't see parent value in in task!
        MtContextThreadLocal<String> after = new MtContextThreadLocal<String>();
        after.set("after");
        mtContexts.put("after", after);

        Future future = executorService.submit(mtContextCallable);
        assertEquals("ok", future.get());

        future = executorService.submit(mtContextCallable);
        try {
            future.get();
            fail();
        } catch (ExecutionException expected) {
            assertThat(expected.getCause(), instanceOf(IllegalStateException.class));
            assertThat(expected.getMessage(), containsString("MtContext is released!"));
        }
    }

    @Test
    public void test_testRemove() throws Exception {
        ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts = new ConcurrentHashMap<String, MtContextThreadLocal<String>>();

        MtContextThreadLocal<String> parent = new MtContextThreadLocal<String>();
        parent.set("parent");
        mtContexts.put("parent", parent);

        MtContextThreadLocal<String> p = new MtContextThreadLocal<String>();
        p.set("p");
        mtContexts.put("p", p);

        parent.remove();

        Call call = new Call("1", mtContexts);
        MtContextCallable<String> mtContextCallable = MtContextCallable.get(call);
        assertSame(call, mtContextCallable.getCallable());

        // create after new Task, won't see parent value in in task!
        MtContextThreadLocal<String> after = new MtContextThreadLocal<String>();
        after.set("after");
        mtContexts.put("after", after);

        Future future = executorService.submit(mtContextCallable);
        assertEquals("ok", future.get());

        // child Inheritable
        assertEquals(2, call.copied.size());
        assertEquals("p1", call.copied.get("p"));
        assertEquals("child", call.copied.get("child"));

        // child do not effect parent
        Map<String, Object> copied = Utils.copied(mtContexts);
        assertEquals(2, copied.size());
        assertEquals("p", copied.get("p"));
        assertEquals("after", copied.get("after"));
    }

    @Test
    public void test_get_idempotent() throws Exception {
        MtContextCallable<String> call = MtContextCallable.get(new Call("1", null));
        try {
            MtContextCallable.get(call);
            fail();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString("Already MtContextCallable"));
        }
    }

    @Test
    public void test_get_nullInput() throws Exception {
        assertNull(MtContextCallable.get(null));
    }

    @Test
    public void test_gets() throws Exception {
        Callable<String> call1 = new Call("1", null);
        Callable<String> call2 = new Call("1", null);
        Callable<String> call3 = new Call("1", null);

        List<MtContextCallable<String>> callList = MtContextCallable.gets(
                Arrays.asList(call1, call2, null, call3));

        assertEquals(4, callList.size());
        assertThat(callList.get(0), instanceOf(MtContextCallable.class));
        assertThat(callList.get(1), instanceOf(MtContextCallable.class));
        assertNull(callList.get(2));
        assertThat(callList.get(3), instanceOf(MtContextCallable.class));
    }
}
