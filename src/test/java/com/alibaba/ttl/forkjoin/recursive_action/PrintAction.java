package com.alibaba.ttl.forkjoin.recursive_action;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.TtlRecursiveAction;
import com.alibaba.ttl.Utils;

/**
 * A test demo class
 *
 * @author LNAmp
 */
class PrintAction extends TtlRecursiveAction {
    static final String CHANGE_POSTFIX = " + 1";

    private final int[] numbers;
    private final int start;
    private final int end;

    private final ConcurrentMap<String, TransmittableThreadLocal<String>> ttlMap;
    private final boolean changeTtlValue;

    volatile Map<String, Object> copied;
    volatile PrintAction leftSubAction;
    volatile PrintAction rightSubAction;

    public PrintAction(int[] numbers, int start, int end,
                       ConcurrentMap<String, TransmittableThreadLocal<String>> ttlMap, boolean changeTtlValue) {
        this.numbers = numbers;
        this.start = start;
        this.end = end;

        this.ttlMap = ttlMap;
        this.changeTtlValue = changeTtlValue;
    }

    @Override
    protected void compute() {
        if (changeTtlValue) {
            Utils.modifyValuesExistInTtlInstances(CHANGE_POSTFIX, ttlMap);
        }

        try {
            if (end - start < 5) {
                for (int i = start; i < end; i++) {
                    System.out.println("num: " + numbers[i]);
                }

            } else {
                int mid = (start + end) / 2;

                // left -> change! right -> not change.
                PrintAction left = new PrintAction(numbers, start, mid, ttlMap, true);
                PrintAction right = new PrintAction(numbers, mid, end, ttlMap, false);
                leftSubAction = left;
                rightSubAction = right;

                left.fork();
                right.fork();
                left.join();
                right.join();
            }
        } finally {
            this.copied = Utils.captured(this.ttlMap);
        }
    }
}
