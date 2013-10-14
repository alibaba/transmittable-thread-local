package com.alibaba.mtc;

import org.junit.AfterClass;
import org.junit.Test;

import java.util.HashMap;
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
        MtContext.getContext().set(new HashMap<String, Object>());
        Utils.expandThreadPool(executorService);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        executorService.shutdown();
    }

    @Test
    public void test_MtContextRunnable() throws Exception {
        MtContext.getContext().set(new HashMap<String, Object>());
        MtContext.getContext().set("parent", "parent");
        MtContext.getContext().set("p", "p0");

        Task task = new Task("1");
        MtContextRunnable mtContextRunnable = MtContextRunnable.get(task);
        assertEquals(task, mtContextRunnable.getRunnable());
        executorService.execute(mtContextRunnable);

        Thread.sleep(100);

        // Child independent & Inheritable
        assertEquals("1", task.copiedContent.get("key"));
        assertEquals("p01", task.copiedContent.get("p"));
        assertEquals("parent", task.copiedContent.get("parent"));

        // restored
        assertEquals(0, task.context.get().size());

        // children do not effect parent
        assertEquals(2, MtContext.getContext().get().size());
        assertEquals("parent", MtContext.getContext().get("parent"));
        assertEquals("p0", MtContext.getContext().get("p"));
    }

    @Test
    public void test_idempotent() throws Exception {
        MtContextRunnable task = MtContextRunnable.get(new Task("1"));
        assertSame(task, MtContextRunnable.get(task));
    }
}
