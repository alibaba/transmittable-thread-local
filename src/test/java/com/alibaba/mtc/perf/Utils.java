package com.alibaba.mtc.perf;

import java.util.Random;

/**
 * @author ding.lid
 */
public class Utils {
    static Random random = new Random();

    public static String bytes2Hex(byte[] b) {
        StringBuilder sb = new StringBuilder(1024);
        for (int n = 0; n < b.length; n++) {
            String s = Integer.toHexString(b[n] & 0xFF);
            sb.append((s.length() == 1) ? "0" + s : s);
        }
        return sb.toString();
    }

    public static byte[] getRandomBytes() {
        byte[] bytes = new byte[1024];
        random.nextBytes(bytes);
        return bytes;
    }

    public static String getRandomString() {
        return bytes2Hex(getRandomBytes());
    }

    private Utils() {
    }
}
