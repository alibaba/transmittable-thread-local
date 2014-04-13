package com.alibaba.mtc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

/**
 * @author ding.lid
 */
public class Utils {
    public static final String PARENT_UNMODIFIED_IN_CHILD = "parent-created-unmodified-in-child";
    public static final String PARENT_MODIFIED_IN_CHILD = "parent-created-modified-in-child";
    public static final String PARENT_AFTER_CREATE_MTC_TASK = "parent-created-after-create-MtcTask";
    public static final String CHILD = "child-created";

    public static ConcurrentMap<String, MtContextThreadLocal<String>> createTestMtContexts() {
        ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts = new ConcurrentHashMap<String, MtContextThreadLocal<String>>();

        MtContextThreadLocal<String> p1 = new MtContextThreadLocal<String>();
        p1.set(PARENT_UNMODIFIED_IN_CHILD);
        mtContexts.put(PARENT_UNMODIFIED_IN_CHILD, p1);

        MtContextThreadLocal<String> p2 = new MtContextThreadLocal<String>();
        p2.set(PARENT_MODIFIED_IN_CHILD);
        mtContexts.put(PARENT_MODIFIED_IN_CHILD, p2);

        return mtContexts;
    }

    public static Map<String, Object> modifyMtContexts(String tag, ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts) {
        System.out.println(tag + " Before Run:");
        Utils.print(mtContexts);
        System.out.println();

        // 1. Add new
        String newChildKey = CHILD + tag;
        MtContextThreadLocal<String> child = new MtContextThreadLocal<String>();
        child.set(newChildKey);

        MtContextThreadLocal<String> old = mtContexts.putIfAbsent(newChildKey, child);
        if (old != null) {
            throw new IllegalStateException("already contains key " + newChildKey);
        }
        mtContexts.put(newChildKey, child);

        // 2. modify the parent key
        String p = mtContexts.get(PARENT_MODIFIED_IN_CHILD).get() + tag;
        mtContexts.get(PARENT_MODIFIED_IN_CHILD).set(p);

        // store value in task
        System.out.println(tag + " After Run:");
        Utils.print(mtContexts);

        return Utils.copied(mtContexts);
    }

    public static <T> void print(ConcurrentMap<String, MtContextThreadLocal<T>> mtContexts) {
        for (Map.Entry<String, MtContextThreadLocal<T>> entry : mtContexts.entrySet()) {
            String key = entry.getKey();
            T value = entry.getValue().get();
            System.out.printf("Key %s, value: %s\n", key, value);
        }
    }

    public static <T> Map<String, Object> copied(ConcurrentMap<String, MtContextThreadLocal<T>> mtContexts) {
        Map<String, Object> copiedContent = new HashMap<String, Object>();
        for (Map.Entry<String, MtContextThreadLocal<T>> entry : mtContexts.entrySet()) {
            String key = entry.getKey();
            T value = entry.getValue().get();
            // store value in task
            if (null != value) {
                copiedContent.put(key, value);
            }
        }
        return copiedContent;
    }

    public static void assertMtContext(Map<String, Object> copied, String... asserts) {
        if (asserts.length % 2 != 0) {
            throw new IllegalStateException("should even count!");
        }
        assertEquals(asserts.length / 2, copied.size());
        for (int i = 0; i < asserts.length; i += 2) {
            assertEquals(asserts[i], copied.get(asserts[i + 1]));
        }
    }

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

    public static void expandThreadPool(ExecutorService executor) {
        try {
            List<Future<?>> ret = new ArrayList<Future<?>>();
            for (int i = 0; i < 5; ++i) {
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
