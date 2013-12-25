package com.alibaba.mtc.perf;

import com.alibaba.mtc.MtContextThreadLocal;

/**
 * @author ding.lid
 */
public class MemoryLeak {
    public static void main(String[] args) throws Exception {
        long counter = 0;
        while (true) {
            MtContextThreadLocal<String> threadLocal = new MtContextThreadLocal<String>();
            threadLocal.set(Utils.getRandomString());

            if (counter % 1000 == 0)
                System.out.printf("%04dK\n", counter / 1000);
            counter++;
        }
    }
}
