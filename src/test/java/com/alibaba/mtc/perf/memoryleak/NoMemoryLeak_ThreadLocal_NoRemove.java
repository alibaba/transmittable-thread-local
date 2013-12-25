package com.alibaba.mtc.perf.memoryleak;

import com.alibaba.mtc.perf.Utils;

/**
 * @author ding.lid
 */
public class NoMemoryLeak_ThreadLocal_NoRemove {
    public static void main(String[] args) throws Exception {
        long counter = 0;
        while (true) {
            ThreadLocal<String> threadLocal = new ThreadLocal<String>();
            threadLocal.set(Utils.getRandomString());

            if (counter % 1000 == 0)
                System.out.printf("%05dK\n", counter / 1000);
            counter++;
        }
    }
}
