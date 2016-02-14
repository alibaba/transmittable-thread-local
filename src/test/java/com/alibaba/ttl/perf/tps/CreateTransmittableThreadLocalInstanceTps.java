package com.alibaba.ttl.perf.tps;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.perf.Utils;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class CreateTransmittableThreadLocalInstanceTps {
    public static void main(String[] args) throws Exception {
        TpsCounter tpsCounter = new TpsCounter(2);
        tpsCounter.run(new Runnable() {
            @Override
            public void run() {
                TransmittableThreadLocal<String> threadLocal = new TransmittableThreadLocal<String>();
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
