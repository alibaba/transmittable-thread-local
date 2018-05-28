package com.alibaba.ttl.testmodel;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.Utils;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class FooTask implements Runnable {
    private final String value;
    private ConcurrentMap<String, TransmittableThreadLocal<FooPojo>> ttlInstances;

    public FooTask(String value, ConcurrentMap<String, TransmittableThreadLocal<FooPojo>> ttlInstances) {
        this.value = value;
        this.ttlInstances = ttlInstances;
    }

    public volatile Map<String, Object> captured;

    @Override
    public void run() {
        try {
            System.out.println("Before Run:");
            Utils.print(ttlInstances);
            System.out.println();

            // Add new
            TransmittableThreadLocal<FooPojo> child = new DeepCopyFooTransmittableThreadLocal();
            child.set(new FooPojo(Utils.CHILD + value, 3));
            ttlInstances.put(Utils.CHILD + value, child);

            // modify the parent key
            ttlInstances.get(Utils.PARENT_MODIFIED_IN_CHILD).get().setName(ttlInstances.get(Utils.PARENT_MODIFIED_IN_CHILD).get().getName() + value);

            System.out.println("After Run:");
            Utils.print(ttlInstances);
            captured = Utils.captured(ttlInstances);

            System.out.println("Task " + value + " finished!");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
