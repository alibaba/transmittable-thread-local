package com.alibaba.mtc;

import org.junit.AfterClass;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;


/**
 * @author ding.lid
 */
public class MtContextRunnableTest {
    static ExecutorService executorService = Executors.newFixedThreadPool(3);

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
        MtContextRunnable mtContextRunnable = MtContextRunnable.get(task);
        assertSame(task, mtContextRunnable.getRunnable());

        // create after new Task, won't see parent value in in task!
        MtContextThreadLocal<String> after = new MtContextThreadLocal<String>();
        after.set("after");
        mtContexts.put("after", after);

        mtContextRunnable.run();

        // Child independent & Inheritable
        assertEquals(4, task.copied.size());
        assertEquals("parent", task.copied.get("parent"));
        assertEquals("p1", task.copied.get("p"));
        assertEquals("after", task.copied.get("after")); // same thread, parent is available from task
        assertEquals("child", task.copied.get("child"));

        // children do not effect parent
        Map<String, Object> copied = Utils.copied(mtContexts);
        assertEquals(4, copied.size());
        assertEquals("parent", copied.get("parent"));
        assertEquals("p", copied.get("p"));
        assertEquals("after", copied.get("after"));
        assertEquals("child", copied.get("child")); // same thread, task set is available from parent 
    }

    @Test
    public void test_MtContextRunnable_withThread() throws Exception {
        ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts = new ConcurrentHashMap<String, MtContextThreadLocal<String>>();

        MtContextThreadLocal<String> parent = new MtContextThreadLocal<String>();
        parent.set("parent");
        mtContexts.put("parent", parent);

        MtContextThreadLocal<String> p = new MtContextThreadLocal<String>();
        p.set("p");
        mtContexts.put("p", p);

        Task task = new Task("1", mtContexts);
        Thread thread1 = new Thread(task);

        // create after new Task, won't see parent value in in task!
        MtContextThreadLocal<String> after = new MtContextThreadLocal<String>();
        after.set("after");
        mtContexts.put("after", after);

        thread1.start();
        thread1.join();

        // Child independent & Inheritable
        System.out.println(task.copied);
        assertEquals(3, task.copied.size());
        assertEquals("parent", task.copied.get("parent"));
        assertEquals("p1", task.copied.get("p"));
        assertEquals("child", task.copied.get("child"));

        // children do not effect parent
        Map<String, Object> copied = Utils.copied(mtContexts);
        assertEquals(3, copied.size());
        assertEquals("parent", copied.get("parent"));
        assertEquals("p", copied.get("p"));
        assertEquals("after", copied.get("after"));
    }

    @Test
    public void test_MtContextRunnable_withExecutorService() throws Exception {
        ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts = new ConcurrentHashMap<String, MtContextThreadLocal<String>>();

        MtContextThreadLocal<String> parent = new MtContextThreadLocal<String>();
        parent.set("parent");
        mtContexts.put("parent", parent);

        MtContextThreadLocal<String> p = new MtContextThreadLocal<String>();
        p.set("p");
        mtContexts.put("p", p);

        Task task = new Task("1", mtContexts);
        MtContextRunnable mtContextRunnable = MtContextRunnable.get(task);
        assertSame(task, mtContextRunnable.getRunnable());

        // create after new Task, won't see parent value in in task!
        MtContextThreadLocal<String> after = new MtContextThreadLocal<String>();
        after.set("after");
        mtContexts.put("after", after);

        executorService.execute(mtContextRunnable);
        Thread.sleep(100);

        // Child independent & Inheritable
        assertEquals(3, task.copied.size());
        assertEquals("parent", task.copied.get("parent"));
        assertEquals("p1", task.copied.get("p"));
        assertEquals("child", task.copied.get("child"));

        // children do not effect parent
        Map<String, Object> copied = Utils.copied(mtContexts);
        assertEquals(3, copied.size());
        assertEquals("parent", copied.get("parent"));
        assertEquals("p", copied.get("p"));
        assertEquals("after", copied.get("after"));
    }

    @Test
    public void test_MtContextRunnable_copyObject() throws Exception {
        ConcurrentMap<String, MtContextThreadLocal<FooPojo>> mtContexts = new ConcurrentHashMap<String, MtContextThreadLocal<FooPojo>>();

        MtContextThreadLocal<FooPojo> parent = new FooMtContextThreadLocal();
        parent.set(new FooPojo("parent", 1));
        mtContexts.put("parent", parent);

        MtContextThreadLocal<FooPojo> p = new FooMtContextThreadLocal();
        p.set(new FooPojo("p", 2));
        mtContexts.put("p", p);

        FooTask task = new FooTask("1", mtContexts);
        MtContextRunnable mtContextRunnable = MtContextRunnable.get(task);
        assertSame(task, mtContextRunnable.getRunnable());

        // create after new Task, won't see parent value in in task!
        MtContextThreadLocal<FooPojo> after = new FooMtContextThreadLocal();
        after.set(new FooPojo("after", 4));
        mtContexts.put("after", after);

        executorService.execute(mtContextRunnable);
        Thread.sleep(100);

        // Child independent & Inheritable
        assertEquals(3, task.copied.size());
        assertEquals(new FooPojo("parent", 1), task.copied.get("parent"));
        assertEquals(new FooPojo("p1", 2), task.copied.get("p"));
        assertEquals(new FooPojo("child", 3), task.copied.get("child"));

        // children do not effect parent
        Map<String, Object> copied = Utils.copied(mtContexts);
        assertEquals(3, copied.size());
        assertEquals(new FooPojo("parent", 1), copied.get("parent"));
        assertEquals(new FooPojo("p", 2), copied.get("p"));
        assertEquals(new FooPojo("after", 4), copied.get("after"));
    }

    @Test
    public void test_idempotent() throws Exception {
        MtContextRunnable task = MtContextRunnable.get(new Task("1", null));
        assertSame(task, MtContextRunnable.get(task));
    }
}
