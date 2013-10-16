package com.alibaba.mtc.threadpool.agent;

import com.alibaba.mtc.MtContext;
import com.alibaba.mtc.Task;
import com.alibaba.mtc.Utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

            MtContext.getContext().set("parent", "parent");
            MtContext.getContext().set("p", "p");

            checkExecutorService();
            checkScheduledExecutorService();

            System.out.println("OK!");
        } finally {
            executorService.shutdown();
            scheduledExecutorService.shutdown();
            executorService.awaitTermination(3, TimeUnit.MINUTES);
            scheduledExecutorService.awaitTermination(3, TimeUnit.MINUTES);
        }
    }

    static void checkExecutorService() throws Exception {
        Task task = new Task("1");
        executorService.submit(task);
        Thread.sleep(1000);

        assert task.copiedContent.size() == 3;
        assert "parent".equals(task.copiedContent.get("parent"));
        assert "1".equals(task.copiedContent.get("key"));
        assert "p1".equals(task.copiedContent.get("p"));

        assert MtContext.getContext().get().size() == 2;
        assert "parent".equals(MtContext.getContext().get("parent"));
        assert "p".equals(MtContext.getContext().get("p"));
    }

    static void checkScheduledExecutorService() throws Exception {
        Task task = new Task("2");
        ScheduledFuture<?> future = scheduledExecutorService.schedule(task, 200, TimeUnit.MILLISECONDS);
        future.get();

        assert task.copiedContent.size() == 3;
        assert "parent".equals(task.copiedContent.get("parent"));
        assert "2".equals(task.copiedContent.get("key"));
        assert "p2".equals(task.copiedContent.get("p"));

        assert MtContext.getContext().get().size() == 2;
        assert "parent".equals(MtContext.getContext().get("parent"));
        assert "p".equals(MtContext.getContext().get("p"));
    }
}
