package com.alibaba.ttl.testmodel;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.Utils;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class Call implements Callable<String> {
    public final String tag;
    private ConcurrentMap<String, TransmittableThreadLocal<String>> ttlInstances;

    public Call(String tag, ConcurrentMap<String, TransmittableThreadLocal<String>> ttlInstances) {
        this.tag = tag;
        this.ttlInstances = ttlInstances;
    }

    public volatile Map<String, Object> copied;

    @Override
    public String call() {
        copied = Utils.modifyTtlInstances(tag, ttlInstances);
        return "ok";
    }
}
