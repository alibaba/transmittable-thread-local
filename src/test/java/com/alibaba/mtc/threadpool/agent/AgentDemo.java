package com.alibaba.mtc.threadpool.agent;

import com.alibaba.mtc.MtContext;
import com.alibaba.mtc.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ding.lid
 */
public class AgentDemo {
    static ExecutorService executorService = Executors.newFixedThreadPool(3, new ThreadFactory() {
        AtomicInteger counter = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            String name = "thread " + counter.getAndIncrement();
            System.out.println(name);
            Thread thread = new Thread(r, name);
            thread.setDaemon(true);
            return thread;
        }
    });

    static {
        for (int i = 0; i < 10; ++i) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Warm task!");
                }
            });
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        System.out.println("Warmed!");
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Hello, " + AgentDemo.class.getName());

        MtContext.getContext().set("key7", "value7");

        Task task = new Task("1");
        executorService.submit(task);

        executorService.shutdown();
        executorService.awaitTermination(3, TimeUnit.MINUTES);

        if ("value7".equals(task.copiedContent.get("key7")))
            System.out.println("OK");
        else {
            System.err.println("!!!!Fail!!!!");
        }
    }
}
