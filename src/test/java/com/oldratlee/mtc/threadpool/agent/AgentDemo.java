package com.oldratlee.mtc.threadpool.agent;

import com.oldratlee.mtc.MtContext;
import com.oldratlee.mtc.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author ding.lid
 */
public class AgentDemo {
    static ExecutorService executorService = Executors.newFixedThreadPool(3);

    public static void main(String[] args) throws Exception {
        System.out.println("Hello, " + AgentDemo.class.getName());
        
        MtContext.getContext().set("key7", "value7");

        Task task = new Task("1");
        executorService.submit(task);

        executorService.shutdown();
        executorService.awaitTermination(3, TimeUnit.MINUTES);

        assert "value7".equals(task.copiedContext.get("key7"));

        System.out.println("OK");
    }
}
