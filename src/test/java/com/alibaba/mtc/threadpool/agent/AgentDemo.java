package com.alibaba.mtc.threadpool.agent;

import com.alibaba.mtc.MtContextThreadLocal;
import com.alibaba.mtc.Task;
import com.alibaba.mtc.Utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * @author ding.lid
 */
public class AgentDemo {
    static ExecutorService executorService = Executors.newFixedThreadPool(3);
    static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);

    static {
        Utils.expandThreadPool(executorService);
        Utils.expandThreadPool(scheduledExecutorService);
    }

    public static void main(String[] args) throws Exception {
        try {
            System.out.println("Hello, " + AgentDemo.class.getName());

            ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts = new ConcurrentHashMap<String, MtContextThreadLocal<String>>();

            MtContextThreadLocal<String> parent = new MtContextThreadLocal<String>();
            parent.set("parent");
            mtContexts.put("parent", parent);

            MtContextThreadLocal<String> p = new MtContextThreadLocal<String>();
            p.set("p");
            mtContexts.put("p", p);

            checkExecutorService(mtContexts);
            checkScheduledExecutorService(mtContexts);

            System.out.println("OK!");
        } finally {
            executorService.shutdown();
            scheduledExecutorService.shutdown();
            executorService.awaitTermination(3, TimeUnit.MINUTES);
            scheduledExecutorService.awaitTermination(3, TimeUnit.MINUTES);
        }
    }

    static void checkExecutorService(ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts) throws Exception {
        Task task = new Task("1", mtContexts);
        executorService.submit(task);

        // create after new Task, won't see parent value in in task!
        MtContextThreadLocal<String> after = new MtContextThreadLocal<String>();
        after.set("after");
        mtContexts.put("after", after);

        Thread.sleep(1000);

        // Child independent & Inheritable
        assertEquals(3, task.copiedContent.size());
        assertEquals("parent", task.copiedContent.get("parent"));
        assertEquals("p1", task.copiedContent.get("p"));
        assertEquals("child", task.copiedContent.get("child"));

        // children do not effect parent
        Map<String, Object> copied = Utils.copied(mtContexts);
        assertEquals(3, copied.size());
        assertEquals("parent", copied.get("parent"));
        assertEquals("p", copied.get("p"));
        assertEquals("after", copied.get("after"));
    }

    static void checkScheduledExecutorService(ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts) throws Exception {
        Task task = new Task("2", mtContexts);
        ScheduledFuture<?> future = scheduledExecutorService.schedule(task, 200, TimeUnit.MILLISECONDS);

        // create after new Task, won't see parent value in in task!
        MtContextThreadLocal<String> after = new MtContextThreadLocal<String>();
        after.set("after");
        mtContexts.put("after", after);

        future.get();

        // Child independent & Inheritable
        assertEquals(3, task.copiedContent.size());
        assertEquals("parent", task.copiedContent.get("parent"));
        assertEquals("p2", task.copiedContent.get("p"));
        assertEquals("child", task.copiedContent.get("child"));

        // children do not effect parent
        Map<String, Object> copied = Utils.copied(mtContexts);
        assertEquals(3, copied.size());
        assertEquals("parent", copied.get("parent"));
        assertEquals("p", copied.get("p"));
        assertEquals("after", copied.get("after"));
    }
}
