package com.alibaba.ttl.testmodel;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.TtlRecursiveTask;
import com.alibaba.ttl.TtlRecursiveTaskTest;
import com.alibaba.ttl.Utils;

/**
 * A test demo class
 *
 * @see com.alibaba.ttl.TtlRecursiveTask
 * @author LNAmp
 * @since 2.3.0
 */
public class CalTask extends TtlRecursiveTask<Integer> {

    private final int[] toCal;
    private final int start;
    private final int end;

    private final String tag;

    private final boolean changeTtlValue;

    public volatile Map<String, Object> copied;

    public volatile CalTask innerLeftCalTask;

    private final ConcurrentMap<String, TransmittableThreadLocal<String>> ttlMap;

    public CalTask(int[] toCal, int start, int end, String tag, ConcurrentMap<String, TransmittableThreadLocal<String>> ttlMap,
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
    protected Integer compute() {
        if (changeTtlValue) {
            Utils.modifyValuesExistInTtlInstances(tag, ttlMap);
        }

        try {
            int sum = 0;
            if (end - start < 5) {
                for (int i = start; i < end; i++) {
                    sum += toCal[i];
                }
                return sum;

            } else {
                int mid = (start + end) / 2;
                CalTask left = new CalTask(toCal, start, mid, TtlRecursiveTaskTest.CHILD_TAG, ttlMap, changeTtlValue);
                CalTask right = new CalTask(toCal, mid, end, TtlRecursiveTaskTest.CHILD_TAG, ttlMap, false);
                innerLeftCalTask = left;
                left.fork();
                right.fork();
                return left.join() + right.join();
            }
        } finally {
            this.copied = Utils.copied(this.ttlMap);
        }
    }
}
