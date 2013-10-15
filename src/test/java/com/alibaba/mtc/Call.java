package com.alibaba.mtc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author ding.lid
 */
public class Call implements Callable<String> {
    public final String value;

    public Call(String value) {
        this.value = value;
    }

    public volatile MtContext context;

    public volatile Map<String, Object> copiedContent;

    @Override
    public String call() {
        try {
            context = MtContext.getContext();
            context.set("key", value);
            context.set("p", context.get("p") + value);

            if (null != context.get("foo")) {
                FooContext foo = context.get("foo");
                foo.setName("child");
                foo.setAge(100);
            }

            copiedContent = new HashMap<String, Object>(context.get());

            return "ok";
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
}
