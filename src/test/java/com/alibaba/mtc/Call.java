package com.alibaba.mtc;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author ding.lid
 */
public class Call implements Callable<String> {
    public final String value;
    private ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts;

    public Call(String value, ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts) {
        this.value = value;
        this.mtContexts = mtContexts;
    }

    public volatile Map<String, Object> copied;

    @Override
    public String call() {
        try {
            ConcurrentMap<String, MtContextThreadLocal<String>> myMtContexts = new ConcurrentHashMap<String, MtContextThreadLocal<String>>(mtContexts);

            long tick = System.currentTimeMillis();
            System.out.println(tick + " Before Run:");
            Utils.print(mtContexts);
            System.out.println();

            // Add new
            MtContextThreadLocal<String> child = new MtContextThreadLocal<String>();
            child.set("child");
            mtContexts.putIfAbsent("child", child);
            myMtContexts.put("child", child);

            // modify the parent key
            String p = mtContexts.get("p").get() + value;
            mtContexts.get("p").set(p);
            myMtContexts.put("p", mtContexts.get("p"));

            // store value in task
            System.out.println(tick + " After Run:");
            Utils.print(mtContexts);
            copied = Utils.copied(myMtContexts);

            System.out.println(tick + " Task " + value + " finished!");

            return "ok";
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
}
