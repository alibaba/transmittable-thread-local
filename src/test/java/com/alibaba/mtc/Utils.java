package com.alibaba.mtc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author ding.lid
 */
public class Utils {
    private static class SleepTask implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public static void expandThreadPool(ExecutorService executor)  {
        try {
            List<Future<?>> ret = new ArrayList<Future<?>>();
            for (int i = 0; i < 4; ++i) {
                Future<?> submit = executor.submit(new SleepTask());
                ret.add(submit);
            }

            for (Future<?> future : ret) {
                future.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
}
