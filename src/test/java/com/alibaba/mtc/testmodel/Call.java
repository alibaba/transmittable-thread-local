package com.alibaba.mtc.testmodel;

import com.alibaba.mtc.MtContextThreadLocal;
import com.alibaba.mtc.Utils;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;

/**
 * @author ding.lid
 */
public class Call implements Callable<String> {
    public final String tag;
    private ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts;

    public Call(String tag, ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts) {
        this.tag = tag;
        this.mtContexts = mtContexts;
    }

    public volatile Map<String, Object> copied;

    @Override
    public String call() {
        copied = Utils.modifyMtContexts(tag, mtContexts);
        return "ok";
    }
}
