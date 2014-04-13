package com.alibaba.mtc;

import com.alibaba.mtc.testmodel.Call;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.AfterClass;
import org.junit.Test;

import static com.alibaba.mtc.Utils.CHILD;
import static com.alibaba.mtc.Utils.PARENT_AFTER_CREATE_MTC_TASK;
import static com.alibaba.mtc.Utils.PARENT_MODIFIED_IN_CHILD;
import static com.alibaba.mtc.Utils.PARENT_UNMODIFIED_IN_CHILD;
import static com.alibaba.mtc.Utils.assertMtContext;
import static com.alibaba.mtc.Utils.copied;
import static com.alibaba.mtc.Utils.createTestMtContexts;
import static com.alibaba.mtc.Utils.expandThreadPool;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


/**
 * @author ding.lid
 */
public class MtContextCallableTest {
    static ExecutorService executorService = Executors.newFixedThreadPool(3);

    static {
        expandThreadPool(executorService);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        executorService.shutdown();
    }

    @Test
    public void test_MtContextCallable_inSameThread() throws Exception {
        ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts = createTestMtContexts();

        Call call = new Call("1", mtContexts);
        MtContextCallable<String> mtContextCallable = MtContextCallable.get(call);

        // create after new Task, won't see parent value in in task!
        MtContextThreadLocal<String> after = new MtContextThreadLocal<String>();
        after.set(PARENT_AFTER_CREATE_MTC_TASK);
        mtContexts.put(PARENT_AFTER_CREATE_MTC_TASK, after);

        String ret = mtContextCallable.call();
        assertEquals("ok", ret);

        // child Inheritable
        assertMtContext(call.copied,
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD + 1, PARENT_MODIFIED_IN_CHILD,
                CHILD + 1, CHILD + 1
        );

        // child do not effect parent
        assertMtContext(copied(mtContexts),
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD, // restored after call!
                PARENT_AFTER_CREATE_MTC_TASK, PARENT_AFTER_CREATE_MTC_TASK
        );
    }

    @Test
    public void test_MtContextCallable_withExecutorService() throws Exception {
        ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts = createTestMtContexts();

        Call call = new Call("1", mtContexts);
        MtContextCallable<String> mtContextCallable = MtContextCallable.get(call);

        // create after new Task, won't see parent value in in task!
        MtContextThreadLocal<String> after = new MtContextThreadLocal<String>();
        after.set(PARENT_AFTER_CREATE_MTC_TASK);
        mtContexts.put(PARENT_AFTER_CREATE_MTC_TASK, after);

        Future future = executorService.submit(mtContextCallable);
        assertEquals("ok", future.get());

        // child Inheritable
        assertMtContext(call.copied,
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD + 1, PARENT_MODIFIED_IN_CHILD,
                CHILD + 1, CHILD + 1
        );

        // child do not effect parent
        assertMtContext(copied(mtContexts),
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD,
                PARENT_AFTER_CREATE_MTC_TASK, PARENT_AFTER_CREATE_MTC_TASK
        );
    }

    @Test
    public void test_testRemove() throws Exception {
        ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts = createTestMtContexts();
        mtContexts.get(PARENT_UNMODIFIED_IN_CHILD).remove();

        Call call = new Call("1", mtContexts);
        MtContextCallable<String> mtContextCallable = MtContextCallable.get(call);

        // create after new Task, won't see parent value in in task!
        MtContextThreadLocal<String> after = new MtContextThreadLocal<String>();
        after.set(PARENT_AFTER_CREATE_MTC_TASK);
        mtContexts.put(PARENT_AFTER_CREATE_MTC_TASK, after);

        Future future = executorService.submit(mtContextCallable);
        assertEquals("ok", future.get());

        // child Inheritable
        assertMtContext(call.copied,
                PARENT_MODIFIED_IN_CHILD + 1, PARENT_MODIFIED_IN_CHILD,
                CHILD + 1, CHILD + 1
        );

        // child do not effect parent
        assertMtContext(copied(mtContexts),
                PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD,
                PARENT_AFTER_CREATE_MTC_TASK, PARENT_AFTER_CREATE_MTC_TASK
        );
    }

    @Test
    public void test_releaseMtContextAfterCall() throws Exception {
        ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts = createTestMtContexts();

        Call call = new Call("1", mtContexts);
        MtContextCallable<String> mtContextCallable = MtContextCallable.get(call, true);
        assertSame(call, mtContextCallable.getCallable());

        Future future = executorService.submit(mtContextCallable);
        assertEquals("ok", future.get());

        future = executorService.submit(mtContextCallable);
        try {
            future.get();
            fail();
        } catch (ExecutionException expected) {
            assertThat(expected.getCause(), instanceOf(IllegalStateException.class));
            assertThat(expected.getMessage(), containsString("MtContext is released!"));
        }
    }

    @Test
    public void test_sameCall() throws Exception {
        Call call = new Call("1", null);
        MtContextCallable<String> mtContextCallable = MtContextCallable.get(call);
        assertSame(call, mtContextCallable.getCallable());
    }

    @Test
    public void test_get_idempotent() throws Exception {
        MtContextCallable<String> call = MtContextCallable.get(new Call("1", null));
        try {
            MtContextCallable.get(call);
            fail();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString("Already MtContextCallable"));
        }
    }

    @Test
    public void test_get_nullInput() throws Exception {
        assertNull(MtContextCallable.get(null));
    }

    @Test
    public void test_gets() throws Exception {
        Callable<String> call1 = new Call("1", null);
        Callable<String> call2 = new Call("1", null);
        Callable<String> call3 = new Call("1", null);

        List<MtContextCallable<String>> callList = MtContextCallable.gets(
                Arrays.asList(call1, call2, null, call3));

        assertEquals(4, callList.size());
        assertThat(callList.get(0), instanceOf(MtContextCallable.class));
        assertThat(callList.get(1), instanceOf(MtContextCallable.class));
        assertNull(callList.get(2));
        assertThat(callList.get(3), instanceOf(MtContextCallable.class));
    }
}
