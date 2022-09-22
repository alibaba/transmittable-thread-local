package com.alibaba.demo.ttl;

import com.alibaba.ttl.TtlRunnable;
import com.alibaba.ttl.threadpool.TtlExecutors;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Simple demo code for issue
 * https://github.com/alibaba/transmittable-thread-local/issues/340
 */
public class CustomizedBlockingQueueWithTtlDemo {
    public static void main(String[] args) throws Exception {
        final MyBlockingQueue myBlockingQueue = new MyBlockingQueue();

        final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                1, 1, 1L, TimeUnit.SECONDS, myBlockingQueue);
        final ExecutorService ttlExecutorService = TtlExecutors.getTtlExecutorService(threadPoolExecutor);

        ttlExecutorService.execute(new MyTask("accept-1"));
        ttlExecutorService.execute(new MyTask("DISCARDED"));
        ttlExecutorService.execute(new MyTask("accept-2"));

        ttlExecutorService.shutdown();
        if (!ttlExecutorService.awaitTermination(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Fail to shutdown executor service");
        }
    }

    private static class MyTask implements Runnable {
        private final String msg;

        private MyTask(String msg) {
            this.msg = msg;
        }

        @Override
        public void run() {
            System.out.println("MyTask: " + msg);
        }
    }

    private static class MyBlockingQueue extends ArrayBlockingQueue<Runnable> {
        public MyBlockingQueue() {
            super(16);
        }

        @Override
        public boolean offer(final Runnable runnable) {
            // unwrap TtlRunnable first
            final Runnable unwrap  = TtlRunnable.unwrap(runnable);

            final MyTask myTask = (MyTask) unwrap;
            if (myTask.msg.startsWith("accept-")) {
                // ignore result/return value of offer, BAD!!
                //   does not follow the contract of method offer
                // this is just a simple demo!
                super.offer(runnable);
            }

            // always return true even if discard the task, BAD!!
            //   does not follow the contract of method offer
            // this is just a simple demo!
            return true;
        }

        @Override
        public void put(final Runnable runnable) throws InterruptedException {
            // unwrap TtlRunnable first
            final Runnable unwrap  = TtlRunnable.unwrap(runnable);

            final MyTask myTask = (MyTask) unwrap;
            // discard task if not satisfied, BAD!!
            //   does not follow the contract of method put
            // this is just a simple demo!
            if (myTask.msg.startsWith("accept-")) {
                super.put(runnable);
            }
        }
    }
}

