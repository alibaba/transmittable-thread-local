package com.alibaba.mtc.testmodel;

import com.alibaba.mtc.MtContextThreadLocal;
import com.alibaba.mtc.Utils;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @author ding.lid
 */
public class FooTask implements Runnable {
    public final String value;
    private ConcurrentMap<String, MtContextThreadLocal<FooPojo>> mtContexts;

    public FooTask(String value, ConcurrentMap<String, MtContextThreadLocal<FooPojo>> mtContexts) {
        this.value = value;
        this.mtContexts = mtContexts;
    }

    public volatile Map<String, Object> copied;

    @Override
    public void run() {
        try {
            System.out.println("Before Run:");
            Utils.print(mtContexts);
            System.out.println();

            // Add new
            MtContextThreadLocal<FooPojo> child = new FooMtContextThreadLocal();
            child.set(new FooPojo(Utils.CHILD  + value, 3));
            mtContexts.put(Utils.CHILD + value, child);

            // modify the parent key
            mtContexts.get(Utils.PARENT_MODIFIED_IN_CHILD).get().setName(mtContexts.get(Utils.PARENT_MODIFIED_IN_CHILD).get().getName() + value);

            System.out.println("After Run:");
            Utils.print(mtContexts);
            copied = Utils.copied(mtContexts);

            System.out.println("Task " + value + " finished!");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
