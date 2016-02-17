package com.alibaba.ttl.perf.memoryleak;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.perf.Utils;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class NoMemoryLeak_TransmittableThreadLocal_NoRemove {
    public static void main(String[] args) throws Exception {
        long counter = 0;
        while (true) {
            TransmittableThreadLocal<String> threadLocal = new TransmittableThreadLocal<String>();
            threadLocal.set(Utils.getRandomString());

            if (counter % 1000 == 0)
                System.out.printf("%05dK%n", counter / 1000);
            counter++;
        }
    }
}
