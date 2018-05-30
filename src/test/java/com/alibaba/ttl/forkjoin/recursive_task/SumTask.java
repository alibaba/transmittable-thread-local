package com.alibaba.ttl.forkjoin.recursive_task;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.TtlRecursiveTask;
import com.alibaba.ttl.Utils;

/**
 * A test demo class
 *
 * @author LNAmp
 * @see com.alibaba.ttl.TtlRecursiveTask
 */
class SumTask extends TtlRecursiveTask<Integer> {
    static final String CHANGE_POSTFIX = " + 1";

    private final int[] numbers;
    private final int start;
    private final int end;

    private final ConcurrentMap<String, TransmittableThreadLocal<String>> ttlMap;
    private final boolean changeTtlValue;

    volatile Map<String, Object> copied;
    volatile SumTask leftSubTask;
    volatile SumTask rightSubTask;

    public SumTask(int[] numbers, int start, int end,
                   ConcurrentMap<String, TransmittableThreadLocal<String>> ttlMap, boolean changeTtlValue) {
        this.numbers = numbers;
        this.start = start;
        this.end = end;

        this.ttlMap = ttlMap;
        this.changeTtlValue = changeTtlValue;
    }

    @Override
    protected Integer compute() {
        if (changeTtlValue) {
            Utils.modifyValuesExistInTtlInstances(CHANGE_POSTFIX, ttlMap);
        }

        try {
            int sum = 0;
            if (end - start < 5) {
                for (int i = start; i < end; i++) {
                    sum += numbers[i];
                }
                return sum;
            } else {
                int mid = (start + end) / 2;

                // left -> change! right -> not change.
                final SumTask left = new SumTask(numbers, start, mid, ttlMap, true);
                final SumTask right = new SumTask(numbers, mid, end, ttlMap, false);
                this.leftSubTask = left;
                this.rightSubTask = right;

                left.fork();
                right.fork();
                return left.join() + right.join();
            }
        } finally {
            this.copied = Utils.captured(this.ttlMap);
        }
    }
}
