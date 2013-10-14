package com.alibaba.mtc.threadpool;

import com.alibaba.mtc.Call;
import com.alibaba.mtc.MtContext;
import com.alibaba.mtc.Task;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

/**
 * @author ding.lid
 */
public class ExecutorServiceMtcWrapperTest {
    static ExecutorService executorService = new ExecutorServiceMtcWrapper(Executors.newFixedThreadPool(3));

    @AfterClass
    public static void afterClass() throws Exception {
        executorService.shutdown();
    }

    @Test
    public void test_MtContextRunnable() throws Exception {
        MtContext.getContext().set("parent", "parent");
        MtContext.getContext().set("p", "p0");

        Task task = new Task("1");
        executorService.execute(task);

        Thread.sleep(100);

        // Child independent & Inheritable
        assertEquals("1", task.copiedContext.get("key"));
        assertEquals("p01", task.copiedContext.get("p"));
        assertEquals("parent", task.copiedContext.get("parent"));

        // restored
        assertEquals(0, task.context.get().size());

        // children do not effect parent
        assertEquals(2, MtContext.getContext().get().size());
        assertEquals("parent", MtContext.getContext().get("parent"));
        assertEquals("p0", MtContext.getContext().get("p"));
    }

    @Test
    public void test_MtContextCallable() throws Exception {
        MtContext.getContext().set("parent", "parent");
        MtContext.getContext().set("p", "p0");

        Call call = new Call("1");
        Future future = executorService.submit(call);

        Thread.sleep(100);
        assertEquals("ok", future.get());

        // Child independent & Inheritable
        assertEquals("1", call.copiedContext.get("key"));
        assertEquals("p01", call.copiedContext.get("p"));
        assertEquals("parent", call.copiedContext.get("parent"));

        // restored
        assertEquals(0, call.context.get().size());

        // children do not effect parent
        assertEquals(2, MtContext.getContext().get().size());
        assertEquals("parent", MtContext.getContext().get("parent"));
        assertEquals("p0", MtContext.getContext().get("p"));
    }
}
