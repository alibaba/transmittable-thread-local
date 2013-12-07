package com.alibaba.mtc;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @author ding.lid
 */
public class Task implements Runnable {
    public final String value;
    private ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts;

    public Task(String value, ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts) {
        this.value = value;
        this.mtContexts = mtContexts;
    }

    public volatile Map<String, Object> copiedContent;
    
    @Override
    public void run() {
        try {
            System.out.println("Before Run:");
            Utils.print(mtContexts);
            System.out.println();

            // Add new
            MtContextThreadLocal<String> child = new MtContextThreadLocal<String>();
            child.set("child");
            mtContexts.put("child", child);

            // modify the parent key
            mtContexts.get("p").set(mtContexts.get("p").get() + value);


            System.out.println("After Run:");
            Utils.print(mtContexts);
            copiedContent = Utils.copied(mtContexts);

            System.out.println("Task " + value + " finished!");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
