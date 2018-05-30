package com.alibaba.ttl;

import java.util.Random;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import com.alibaba.ttl.testmodel.CalTask;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.alibaba.ttl.Utils.PARENT_AFTER_CREATE_TTL_TASK;
import static com.alibaba.ttl.Utils.PARENT_MODIFIED_IN_CHILD;
import static com.alibaba.ttl.Utils.PARENT_UNMODIFIED_IN_CHILD;
import static com.alibaba.ttl.Utils.assertTtlInstances;
import static com.alibaba.ttl.Utils.copied;
import static com.alibaba.ttl.Utils.createTestTtlValue;
import static org.junit.Assert.assertEquals;

/**
 * TtlRecursiveTask test class
 *
 * @author LNAmp
 * @since 2.3.0
 */
public class TtlRecursiveTaskTest {

    private static ForkJoinPool pool = new ForkJoinPool();

    private static ForkJoinPool singleThreadPool = new ForkJoinPool(1);

    public static final String PARENT_TAG = "1";

    public static final String CHILD_TAG = "11";

    private static int[] arr;

    private static int[] simpleArr;

    private static Integer total = 0;

    private static Integer simpleTotal = 0;

    @BeforeClass
    public static void beforeClass() {
        arr = new int[1000];
        Random rand = new Random();
        for (int i = 0; i < arr.length; i++) {
            int tmp = rand.nextInt(20);
            total += (arr[i] = tmp);
        }

        simpleArr = new int[5];
        for (int i = 0; i < simpleArr.length; i++) {
            int tmp = rand.nextInt(20);
            simpleTotal += (simpleArr[i] = tmp);
        }
    }

    @AfterClass
    public static void afterClass() throws Exception {
        pool.shutdown();
        singleThreadPool.shutdown();
    }

    @Test
    public void test_TtlRecursiveTask_InSameThread_changeValue() throws Exception {
        ConcurrentMap<String, TransmittableThreadLocal<String>> ttlInstances = createTestTtlValue();

        CalTask calTask = new CalTask(simpleArr, 0, simpleArr.length, PARENT_TAG, ttlInstances, true);

        TransmittableThreadLocal<String> after = new TransmittableThreadLocal<String>();
        after.set(PARENT_AFTER_CREATE_TTL_TASK);
        ttlInstances.put(PARENT_AFTER_CREATE_TTL_TASK, after);

        calTask.exec();

        assertEquals(simpleTotal, calTask.getRawResult());

        // child Inheritable
        assertTtlInstances(calTask.copied,
            PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
            PARENT_MODIFIED_IN_CHILD + PARENT_TAG, PARENT_MODIFIED_IN_CHILD
        );

        // child do not effect parent
        assertTtlInstances(copied(ttlInstances),
            PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
            PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD, // restored after call!
            PARENT_AFTER_CREATE_TTL_TASK, PARENT_AFTER_CREATE_TTL_TASK
        );
    }


    @Test
    public void test_TtlRecursiveTask_asyncWithForkJoinPool_notChangeValue() throws Exception {
        ConcurrentMap<String, TransmittableThreadLocal<String>> ttlInstances = createTestTtlValue();

        CalTask calTask = new CalTask(arr, 0, arr.length, PARENT_TAG, ttlInstances, false);

        TransmittableThreadLocal<String> after = new TransmittableThreadLocal<String>();
        after.set(PARENT_AFTER_CREATE_TTL_TASK);
        ttlInstances.put(PARENT_AFTER_CREATE_TTL_TASK, after);

        Future<Integer> future = pool.submit(calTask);

        Assert.assertEquals(total, future.get());

        // child Inheritable
        assertTtlInstances(calTask.copied,
            PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
            PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD
        );

        // grandchild Inheritable
        assertTtlInstances(calTask.innerLeftCalTask.copied,
            PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
            PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD
        );

        // child do not effect parent
        assertTtlInstances(copied(ttlInstances),
            PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
            PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD,
            PARENT_AFTER_CREATE_TTL_TASK, PARENT_AFTER_CREATE_TTL_TASK
        );
    }

    @Test
    public void test_TtlRecursiveTask_asyncWithForkJoinPool_changeValue() throws Exception {
        ConcurrentMap<String, TransmittableThreadLocal<String>> ttlInstances = createTestTtlValue();

        CalTask calTask = new CalTask(arr, 0, arr.length, PARENT_TAG, ttlInstances, true);

        TransmittableThreadLocal<String> after = new TransmittableThreadLocal<String>();
        after.set(PARENT_AFTER_CREATE_TTL_TASK);
        ttlInstances.put(PARENT_AFTER_CREATE_TTL_TASK, after);

        Future<Integer> future = pool.submit(calTask);

        Assert.assertEquals(total, future.get());

        // child Inheritable
        assertTtlInstances(calTask.copied,
            PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
            PARENT_MODIFIED_IN_CHILD + PARENT_TAG, PARENT_MODIFIED_IN_CHILD
        );

        // grand child Inheritable
        assertTtlInstances(calTask.innerLeftCalTask.copied,
            PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
            PARENT_MODIFIED_IN_CHILD + PARENT_TAG + CHILD_TAG, PARENT_MODIFIED_IN_CHILD
        );

        // child do not effect parent
        assertTtlInstances(copied(ttlInstances),
            PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
            PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD,
            PARENT_AFTER_CREATE_TTL_TASK, PARENT_AFTER_CREATE_TTL_TASK
        );
    }

    @Test
    public void test_TtlRecursiveTask_asyncWithSingleThreadForkJoinPool_changeValue() throws Exception {
        ConcurrentMap<String, TransmittableThreadLocal<String>> ttlInstances = createTestTtlValue();

        CalTask calTask = new CalTask(arr, 0, arr.length, PARENT_TAG, ttlInstances, true);

        TransmittableThreadLocal<String> after = new TransmittableThreadLocal<String>();
        after.set(PARENT_AFTER_CREATE_TTL_TASK);
        ttlInstances.put(PARENT_AFTER_CREATE_TTL_TASK, after);

        Future<Integer> future = singleThreadPool.submit(calTask);

        Assert.assertEquals(total, future.get());

        // child Inheritable
        assertTtlInstances(calTask.copied,
            PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
            PARENT_MODIFIED_IN_CHILD + PARENT_TAG, PARENT_MODIFIED_IN_CHILD
        );

        // grandchild Inheritable
        assertTtlInstances(calTask.innerLeftCalTask.copied,
            PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
            PARENT_MODIFIED_IN_CHILD + PARENT_TAG + CHILD_TAG, PARENT_MODIFIED_IN_CHILD
        );

        // child do not effect parent
        assertTtlInstances(copied(ttlInstances),
            PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
            PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD,
            PARENT_AFTER_CREATE_TTL_TASK, PARENT_AFTER_CREATE_TTL_TASK
        );
    }

}
