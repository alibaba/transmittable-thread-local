package com.alibaba.ttl.testmodel;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.TtlRecursiveAction;
import com.alibaba.ttl.TtlRecursiveTaskTest;
import com.alibaba.ttl.Utils;

/**
 * A test demo class
 *
 * @see com.alibaba.ttl.TtlRecursiveAction
 * @author LNAmp
 * @since 2.3.0
 */
public class PrintAction extends TtlRecursiveAction {
    private final int[] toCal;
    private final int start;
    private final int end;

    private final String tag;

    private final boolean changeTtlValue;

    public volatile Map<String, Object> copied;

    public volatile PrintAction innerLeftPrintAction;

    private final ConcurrentMap<String, TransmittableThreadLocal<String>> ttlMap;

    public PrintAction(int[] toCal, int start, int end, String tag, ConcurrentMap<String, TransmittableThreadLocal<String>> ttlMap,
                   boolean changeTtlValue) {
        super();
        this.tag = tag;
        this.changeTtlValue = changeTtlValue;
        this.ttlMap = ttlMap;
        this.toCal = toCal;
        this.start = start;
        this.end = end;
    }

    @Override
    protected void compute() {
        if (changeTtlValue) {
            Utils.modifyValuesExistInTtlInstances(tag, ttlMap);
        }

        try {
            if (end - start < 5) {
                for (int i = start; i < end; i++) {
                    System.out.println("num: " + toCal[i]);
                }

            } else {
                int mid = (start + end) / 2;
                PrintAction left = new PrintAction(toCal, start, mid, TtlRecursiveTaskTest.CHILD_TAG, ttlMap, changeTtlValue);
                PrintAction right = new PrintAction(toCal, mid, end, TtlRecursiveTaskTest.CHILD_TAG, ttlMap, false);
                innerLeftPrintAction = left;
                left.fork();
                right.fork();
                left.join();
                right.join();
            }
        } finally {
            this.copied = Utils.copied(this.ttlMap);
        }
    }
}
