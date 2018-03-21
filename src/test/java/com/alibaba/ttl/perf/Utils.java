package com.alibaba.ttl.perf;

import java.util.Random;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class Utils {
    private static Random random = new Random();

    private static String bytes2Hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(1024);
        for (byte b : bytes) {
            String s = Integer.toHexString(b & 0xFF);
            sb.append((s.length() == 1) ? "0" + s : s);
        }
        return sb.toString();
    }

    private static byte[] getRandomBytes() {
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
