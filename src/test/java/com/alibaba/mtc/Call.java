package com.alibaba.mtc;

import java.util.concurrent.Callable;

/**
 * @author ding.lid
 */
public class Call implements Callable<String> {
    public final String value;

    public Call(String value) {
        this.value = value;
    }

    public MtContext context;

    public MtContext copiedContext;

    @Override
    public String call() {
        context = MtContext.getContext();
        context.set("key", value);
        context.set("p", context.get("p") + value);

        copiedContext = new MtContext(context);

        return "ok";
    }
}
