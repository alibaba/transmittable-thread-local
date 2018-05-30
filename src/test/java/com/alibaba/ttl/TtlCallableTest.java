package com.alibaba.ttl;

import com.alibaba.ttl.testmodel.Call;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static com.alibaba.ttl.Utils.CHILD;
import static com.alibaba.ttl.Utils.PARENT_AFTER_CREATE_TTL_TASK;
import static com.alibaba.ttl.Utils.PARENT_MODIFIED_IN_CHILD;
import static com.alibaba.ttl.Utils.PARENT_UNMODIFIED_IN_CHILD;
import static com.alibaba.ttl.Utils.assertTtlInstances;
import static com.alibaba.ttl.Utils.captured;
import static com.alibaba.ttl.Utils.createTestTtlValue;
import static com.alibaba.ttl.Utils.expandThreadPool;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class TtlCallableTest {
    private static ExecutorService executorService = Executors.newFixedThreadPool(3);

    static {
        expandThreadPool(executorService);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        executorService.shutdown();
        executorService.awaitTermination(100, TimeUnit.MILLISECONDS);
        if (!executorService.isTerminated()) fail("Fail to shutdown thread pool");
    }

    @Test
    public void test_TtlCallable_inSameThread() throws Exception {
        ConcurrentMap<String, TransmittableThreadLocal<String>> ttlInstances = createTestTtlValue();

        Call call = new Call("1", ttlInstances);
        TtlCallable<String> ttlCallable = TtlCallable.get(call);

        // create after new Task, won't see parent value in in task!
        TransmittableThreadLocal<String> after = new TransmittableThreadLocal<>();
        after.set(PARENT_AFTER_CREATE_TTL_TASK);
        ttlInstances.put(PARENT_AFTER_CREATE_TTL_TASK, after);

        String ret = ttlCallable.call();
        assertEquals("ok", ret);

        // child Inheritable
        assertTtlInstances(call.captured,
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD + 1, PARENT_MODIFIED_IN_CHILD,
                CHILD + 1, CHILD + 1
        );

        // child do not effect parent
        assertTtlInstances(captured(ttlInstances),
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD, // restored after call!
                PARENT_AFTER_CREATE_TTL_TASK, PARENT_AFTER_CREATE_TTL_TASK
        );
    }

    @Test
    public void test_TtlCallable_asyncWithExecutorService() throws Exception {
        ConcurrentMap<String, TransmittableThreadLocal<String>> ttlInstances = createTestTtlValue();

        Call call = new Call("1", ttlInstances);
        TtlCallable<String> ttlCallable = TtlCallable.get(call);

        // create after new Task, won't see parent value in in task!
        TransmittableThreadLocal<String> after = new TransmittableThreadLocal<>();
        after.set(PARENT_AFTER_CREATE_TTL_TASK);
        ttlInstances.put(PARENT_AFTER_CREATE_TTL_TASK, after);

        Future<String> future = executorService.submit(ttlCallable);
        assertEquals("ok", future.get());

        // child Inheritable
        assertTtlInstances(call.captured,
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD + 1, PARENT_MODIFIED_IN_CHILD,
                CHILD + 1, CHILD + 1
        );

        // child do not effect parent
        assertTtlInstances(captured(ttlInstances),
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD,
                PARENT_AFTER_CREATE_TTL_TASK, PARENT_AFTER_CREATE_TTL_TASK
        );
    }

    @Test
    public void test_removeSameAsNotSet() throws Exception {
        ConcurrentMap<String, TransmittableThreadLocal<String>> ttlInstances = createTestTtlValue();
        ttlInstances.get(PARENT_UNMODIFIED_IN_CHILD).remove();

        Call call = new Call("1", ttlInstances);
        TtlCallable<String> ttlCallable = TtlCallable.get(call);

        // create after new Task, won't see parent value in in task!
        TransmittableThreadLocal<String> after = new TransmittableThreadLocal<>();
        after.set(PARENT_AFTER_CREATE_TTL_TASK);
        ttlInstances.put(PARENT_AFTER_CREATE_TTL_TASK, after);

        Future<String> future = executorService.submit(ttlCallable);
        assertEquals("ok", future.get());

        // child Inheritable
        assertTtlInstances(call.captured,
                PARENT_MODIFIED_IN_CHILD + 1, PARENT_MODIFIED_IN_CHILD,
                CHILD + 1, CHILD + 1
        );

        // child do not effect parent
        assertTtlInstances(captured(ttlInstances),
                PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD,
                PARENT_AFTER_CREATE_TTL_TASK, PARENT_AFTER_CREATE_TTL_TASK
        );
    }

    @Test
    public void test_releaseTtlValueReferenceAfterCall() throws Exception {
        ConcurrentMap<String, TransmittableThreadLocal<String>> ttlInstances = createTestTtlValue();

        Call call = new Call("1", ttlInstances);
        TtlCallable<String> ttlCallable = TtlCallable.get(call, true);
        assertSame(call, ttlCallable.getCallable());

        Future<String> future = executorService.submit(ttlCallable);
        assertEquals("ok", future.get());

        future = executorService.submit(ttlCallable);
        try {
            future.get();
            fail();
        } catch (ExecutionException expected) {
            assertThat(expected.getCause(), instanceOf(IllegalStateException.class));
            assertThat(expected.getMessage(), containsString("TTL value reference is released after call!"));
        }
    }

    @Test
    public void test_get_same() throws Exception {
        Call call = new Call("1", null);
        TtlCallable<String> ttlCallable = TtlCallable.get(call);
        assertSame(call, ttlCallable.getCallable());
    }

    @Test
    public void test_get_idempotent() throws Exception {
        TtlCallable<String> call = TtlCallable.get(new Call("1", null));
        try {
            TtlCallable.get(call);
            fail();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString("Already TtlCallable"));
        }
    }

    @Test
    public void test_get_nullInput() throws Exception {
        assertNull(TtlCallable.get(null));
    }

    @Test
    public void test_gets() throws Exception {
        Callable<String> call1 = new Call("1", null);
        Callable<String> call2 = new Call("1", null);
        Callable<String> call3 = new Call("1", null);

        @SuppressWarnings("unchecked")
        List<TtlCallable<String>> callList = TtlCallable.gets(
                Arrays.asList(call1, call2, null, call3));

        assertEquals(4, callList.size());
        assertThat(callList.get(0), instanceOf(TtlCallable.class));
        assertThat(callList.get(1), instanceOf(TtlCallable.class));
        assertNull(callList.get(2));
        assertThat(callList.get(3), instanceOf(TtlCallable.class));
    }
}
