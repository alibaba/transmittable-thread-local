package com.alibaba.ttl;

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
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public final class Utils {
    public static final String PARENT_UNMODIFIED_IN_CHILD = "parent-created-unmodified-in-child";
    public static final String PARENT_MODIFIED_IN_CHILD = "parent-created-modified-in-child";
    public static final String PARENT_AFTER_CREATE_TTL_TASK = "parent-created-after-create-TtlTask";
    public static final String CHILD = "child-created";

    private Utils() {
        throw new InstantiationError("Must not instantiate this class");
    }

    public static ConcurrentMap<String, TransmittableThreadLocal<String>> createTestTtlValue() {
        ConcurrentMap<String, TransmittableThreadLocal<String>> ttlInstances = new ConcurrentHashMap<String, TransmittableThreadLocal<String>>();

        TransmittableThreadLocal<String> p1 = new TransmittableThreadLocal<String>();
        p1.set(PARENT_UNMODIFIED_IN_CHILD);
        ttlInstances.put(PARENT_UNMODIFIED_IN_CHILD, p1);

        TransmittableThreadLocal<String> p2 = new TransmittableThreadLocal<String>();
        p2.set(PARENT_MODIFIED_IN_CHILD);
        ttlInstances.put(PARENT_MODIFIED_IN_CHILD, p2);

        return ttlInstances;
    }

    public static Map<String, Object> modifyTtlInstances(String tag, ConcurrentMap<String, TransmittableThreadLocal<String>> ttlInstances) {
        System.out.println(tag + " Before Run:");
        Utils.print(ttlInstances);
        System.out.println();

        // 1. Add new
        String newChildKey = CHILD + tag;
        TransmittableThreadLocal<String> child = new TransmittableThreadLocal<String>();
        child.set(newChildKey);

        TransmittableThreadLocal<String> old = ttlInstances.putIfAbsent(newChildKey, child);
        if (old != null) {
            throw new IllegalStateException("already contains key " + newChildKey);
        }
        ttlInstances.put(newChildKey, child);

        // 2. modify the parent key
        String p = ttlInstances.get(PARENT_MODIFIED_IN_CHILD).get() + tag;
        ttlInstances.get(PARENT_MODIFIED_IN_CHILD).set(p);

        // store value in task
        System.out.println(tag + " After Run:");
        Utils.print(ttlInstances);

        return Utils.copied(ttlInstances);
    }

    public static <T> void print(ConcurrentMap<String, TransmittableThreadLocal<T>> ttlInstances) {
        for (Map.Entry<String, TransmittableThreadLocal<T>> entry : ttlInstances.entrySet()) {
            String key = entry.getKey();
            T value = entry.getValue().get();
            System.out.printf("Key %s, value: %s%n", key, value);
        }
    }

    public static <T> Map<String, Object> copied(ConcurrentMap<String, TransmittableThreadLocal<T>> ttlInstances) {
        Map<String, Object> copiedContent = new HashMap<String, Object>();
        for (Map.Entry<String, TransmittableThreadLocal<T>> entry : ttlInstances.entrySet()) {
            String key = entry.getKey();
            T value = entry.getValue().get();
            // store value in task
            if (null != value) {
                copiedContent.put(key, value);
            }
        }
        return copiedContent;
    }

    public static void assertTtlInstances(Map<String, Object> copied, String... asserts) {
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
