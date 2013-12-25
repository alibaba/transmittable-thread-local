package com.alibaba.mtc.perf.memoryleak;

import com.alibaba.mtc.MtContextThreadLocal;
import com.alibaba.mtc.perf.Utils;

/**
 * @author ding.lid
 */
public class NoMemoryLeak_MtContextThreadLocal_NoRemove {
    public static void main(String[] args) throws Exception {
        long counter = 0;
        while (true) {
            MtContextThreadLocal<String> threadLocal = new MtContextThreadLocal<String>();
            threadLocal.set(Utils.getRandomString());

            if (counter % 1000 == 0)
                System.out.printf("%05dK\n", counter / 1000);
            counter++;
        }
    }
}
