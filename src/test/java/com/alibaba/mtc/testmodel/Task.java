package com.alibaba.mtc.testmodel;

import com.alibaba.mtc.MtContextThreadLocal;
import com.alibaba.mtc.Utils;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @author ding.lid
 */
public class Task implements Runnable {
    public final String tag;
    private ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts;

    public Task(String tag, ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts) {
        this.tag = tag;
        this.mtContexts = mtContexts;
    }

    public volatile Map<String, Object> copied;

    @Override
    public void run() {
        try {
            copied = Utils.modifyMtContexts(tag, mtContexts);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
