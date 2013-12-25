package com.alibaba.mtc.perf.tps;

import com.alibaba.mtc.perf.Utils;

/**
 * @author ding.lid
 */
public class CreateThreadLocalInstanceTps {
    public static void main(String[] args) throws Exception {
        TpsCounter tpsCounter = new TpsCounter(2);
        tpsCounter.run(new Runnable() {
            @Override
            public void run() {
                ThreadLocal<String> threadLocal = new ThreadLocal<String>();
                threadLocal.set(Utils.getRandomString());
            }
        });

        while (true) {
            long start = tpsCounter.getCount();
            Thread.sleep(1000);
            System.out.printf("tps: %d\n", tpsCounter.getCount() - start);
        }
    }
}
