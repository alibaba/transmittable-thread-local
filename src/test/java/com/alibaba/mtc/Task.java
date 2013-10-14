package com.alibaba.mtc;

/**
 * @author ding.lid
 */
public class Task implements Runnable {
    public final String value;

    public Task(String value) {
        this.value = value;
    }

    public MtContext context;

    public MtContext copiedContext;

    @Override
    public void run() {
        context = MtContext.getContext();
        context.set("key", value);
        context.set("p", context.get("p") + value);

        copiedContext = new MtContext(context);
    }
}
