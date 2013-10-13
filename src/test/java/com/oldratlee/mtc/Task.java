package com.oldratlee.mtc;

/**
 * @author ding.lid
 */
class Task implements Runnable {
    final String value;

    Task(String value) {
        this.value = value;
    }

    MtContext context;

    MtContext copiedContext;

    @Override
    public void run() {
        context = MtContext.getContext();
        context.set("key", value);
        context.set("p", context.get("p") + value);

        copiedContext = new MtContext(context);
    }
}
