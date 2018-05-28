package com.alibaba.ttl.testmodel;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.Utils;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class Task implements Runnable {
    private final String tag;
    private ConcurrentMap<String, TransmittableThreadLocal<String>> ttlInstances;

    public Task(String tag, ConcurrentMap<String, TransmittableThreadLocal<String>> ttlInstances) {
        this.tag = tag;
        this.ttlInstances = ttlInstances;
    }

    public volatile Map<String, Object> captured;

    @Override
    public void run() {
        try {
            captured = Utils.modifyTtlInstances(tag, ttlInstances);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
