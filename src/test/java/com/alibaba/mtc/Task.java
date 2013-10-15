package com.alibaba.mtc;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ding.lid
 */
public class Task implements Runnable {
    public final String value;

    public Task(String value) {
        this.value = value;
    }

    public volatile MtContext context;

    public volatile Map<String, Object> copiedContent;

    @Override
    public void run() {
        try {
            context = MtContext.getContext();
            System.out.println("Task " + value + " running1: " + context.get());

            context.set("key", value);
            context.set("p", context.get("p") + value);
            System.out.println("Task " + value + " running2: " + context.get());

            if (null != context.get("foo")) {
                FooContext foo = context.get("foo");
                foo.setName("child");
                foo.setAge(100);
            }

            copiedContent = new HashMap<String, Object>(context.get());

            System.out.println("Task " + value + " finished!");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
