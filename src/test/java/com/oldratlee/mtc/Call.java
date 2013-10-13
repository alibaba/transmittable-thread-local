package com.oldratlee.mtc;

import java.util.concurrent.Callable;

/**
 * @author ding.lid
 */
class Call implements Callable<String> {
    final String value;

    Call(String value) {
        this.value = value;
    }

    MtContext context;

    MtContext copiedContext;

    @Override
    public String call() {
        context = MtContext.getContext();
        context.set("key", value);
        context.set("p", context.get("p") + value);

        copiedContext = new MtContext(context);

        return "ok";
    }
}
