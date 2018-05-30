package com.alibaba.ttl;

import com.alibaba.ttl.testmodel.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import org.junit.AfterClass;
import org.junit.Test;

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
public class TtlRunnableTest {
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
    public void test_ttlRunnable_inSameThread() throws Exception {
        ConcurrentMap<String, TransmittableThreadLocal<String>> ttlInstances = createTestTtlValue();

        Task task = new Task("1", ttlInstances);
        TtlRunnable ttlRunnable = TtlRunnable.get(task);

        // create after new Task
        TransmittableThreadLocal<String> after = new TransmittableThreadLocal<>();
        after.set(PARENT_AFTER_CREATE_TTL_TASK);
        ttlInstances.put(PARENT_AFTER_CREATE_TTL_TASK, after);

        ttlRunnable.run();

        // child Inheritable
        assertTtlInstances(task.captured,
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD + "1", PARENT_MODIFIED_IN_CHILD,
                CHILD + "1", CHILD + "1"
        );

        // child do not effect parent
        assertTtlInstances(captured(ttlInstances),
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD, // restored after call!
                PARENT_AFTER_CREATE_TTL_TASK, PARENT_AFTER_CREATE_TTL_TASK
        );
    }

    @Test
    public void test_ttlRunnable_asyncWithNewThread() throws Exception {
        ConcurrentMap<String, TransmittableThreadLocal<String>> ttlInstances = createTestTtlValue();

        Task task = new Task("1", ttlInstances);
        Thread thread1 = new Thread(task);

        // create after new Task, won't see parent value in in task!
        TransmittableThreadLocal<String> after = new TransmittableThreadLocal<>();
        after.set(PARENT_AFTER_CREATE_TTL_TASK);
        ttlInstances.put(PARENT_AFTER_CREATE_TTL_TASK, after);

        thread1.start();
        thread1.join();

        // child Inheritable
        assertTtlInstances(task.captured,
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD + "1", PARENT_MODIFIED_IN_CHILD,
                CHILD + "1", CHILD + "1"
        );

        // child do not effect parent
        assertTtlInstances(captured(ttlInstances),
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD,
                PARENT_AFTER_CREATE_TTL_TASK, PARENT_AFTER_CREATE_TTL_TASK
        );
    }

    @Test
    public void test_TtlRunnable_asyncWithExecutorService() throws Exception {
        ConcurrentMap<String, TransmittableThreadLocal<String>> ttlInstances = createTestTtlValue();

        Task task = new Task("1", ttlInstances);
        TtlRunnable ttlRunnable = TtlRunnable.get(task);

        // create after new Task, won't see parent value in in task!
        TransmittableThreadLocal<String> after = new TransmittableThreadLocal<>();
        after.set(PARENT_AFTER_CREATE_TTL_TASK);
        ttlInstances.put(PARENT_AFTER_CREATE_TTL_TASK, after);

        Future<?> submit = executorService.submit(ttlRunnable);
        submit.get();

        // child Inheritable
        assertTtlInstances(task.captured,
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD + "1", PARENT_MODIFIED_IN_CHILD,
                CHILD + "1", CHILD + "1"
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

        // remove TransmittableThreadLocal
        ttlInstances.get(PARENT_UNMODIFIED_IN_CHILD).remove();

        Task task = new Task("1", ttlInstances);
        TtlRunnable ttlRunnable = TtlRunnable.get(task);

        // create after new Task, won't see parent value in in task!
        TransmittableThreadLocal<String> after = new TransmittableThreadLocal<>();
        after.set(PARENT_AFTER_CREATE_TTL_TASK);
        ttlInstances.put(PARENT_AFTER_CREATE_TTL_TASK, after);

        Future<?> submit = executorService.submit(ttlRunnable);
        submit.get();

        // child Inheritable
        assertTtlInstances(task.captured,
                PARENT_MODIFIED_IN_CHILD + "1", PARENT_MODIFIED_IN_CHILD,
                CHILD + 1, CHILD + 1
        );

        // child do not effect parent
        assertTtlInstances(captured(ttlInstances),
                PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD,
                PARENT_AFTER_CREATE_TTL_TASK, PARENT_AFTER_CREATE_TTL_TASK
        );
    }

    @Test
    public void test_callback_copy_beforeExecute_afterExecute() throws Exception {
        CallbackTestTransmittableThreadLocal callbackTestTransmittableThreadLocal = new CallbackTestTransmittableThreadLocal();

        callbackTestTransmittableThreadLocal.set(new FooPojo("jerry", 42));

        Runnable task1 = () -> {
        };
        // do copy when decorate runnable
        TtlRunnable ttlRunnable1 = TtlRunnable.get(task1);

        executorService.submit(ttlRunnable1).get();

        assertEquals(1, callbackTestTransmittableThreadLocal.copyCounter.get());
        assertEquals(1, callbackTestTransmittableThreadLocal.beforeExecuteCounter.get());
        assertEquals(1, callbackTestTransmittableThreadLocal.afterExecuteCounter.get());


        executorService.submit(ttlRunnable1).get();

        assertEquals(1, callbackTestTransmittableThreadLocal.copyCounter.get());
        assertEquals(2, callbackTestTransmittableThreadLocal.beforeExecuteCounter.get());
        assertEquals(2, callbackTestTransmittableThreadLocal.afterExecuteCounter.get());


        Runnable task2 = () -> { };
        // do copy when decorate runnable
        TtlRunnable ttlRunnable2 = TtlRunnable.get(task2);


        executorService.submit(ttlRunnable2).get();

        assertEquals(2, callbackTestTransmittableThreadLocal.copyCounter.get());
        assertEquals(3, callbackTestTransmittableThreadLocal.beforeExecuteCounter.get());
        assertEquals(3, callbackTestTransmittableThreadLocal.afterExecuteCounter.get());
    }

    @Test
    public void test_TtlRunnable_copyObject() throws Exception {
        ConcurrentMap<String, TransmittableThreadLocal<FooPojo>> ttlInstances =
                new ConcurrentHashMap<>();

        TransmittableThreadLocal<FooPojo> parent = new DeepCopyFooTransmittableThreadLocal();
        parent.set(new FooPojo(PARENT_UNMODIFIED_IN_CHILD, 1));
        ttlInstances.put(PARENT_UNMODIFIED_IN_CHILD, parent);

        TransmittableThreadLocal<FooPojo> p = new DeepCopyFooTransmittableThreadLocal();
        p.set(new FooPojo(PARENT_MODIFIED_IN_CHILD, 2));
        ttlInstances.put(PARENT_MODIFIED_IN_CHILD, p);

        FooTask task = new FooTask("1", ttlInstances);
        TtlRunnable ttlRunnable = TtlRunnable.get(task);

        // create after new Task, won't see parent value in in task!
        TransmittableThreadLocal<FooPojo> after = new DeepCopyFooTransmittableThreadLocal();
        after.set(new FooPojo(PARENT_AFTER_CREATE_TTL_TASK, 4));
        ttlInstances.put(PARENT_AFTER_CREATE_TTL_TASK, after);

        Future<?> submit = executorService.submit(ttlRunnable);
        submit.get();

        // child Inheritable
        assertEquals(3, task.captured.size());
        assertEquals(new FooPojo(PARENT_UNMODIFIED_IN_CHILD, 1), task.captured.get(PARENT_UNMODIFIED_IN_CHILD));
        assertEquals(new FooPojo(PARENT_MODIFIED_IN_CHILD + "1", 2), task.captured.get(PARENT_MODIFIED_IN_CHILD));
        assertEquals(new FooPojo(CHILD + 1, 3), task.captured.get(CHILD + 1));

        // child do not effect parent
        Map<String, Object> captured = captured(ttlInstances);
        assertEquals(3, captured.size());
        assertEquals(new FooPojo(PARENT_UNMODIFIED_IN_CHILD, 1), captured.get(PARENT_UNMODIFIED_IN_CHILD));
        assertEquals(new FooPojo(PARENT_MODIFIED_IN_CHILD, 2), captured.get(PARENT_MODIFIED_IN_CHILD));
        assertEquals(new FooPojo(PARENT_AFTER_CREATE_TTL_TASK, 4), captured.get(PARENT_AFTER_CREATE_TTL_TASK));
    }

    @Test
    public void test_releaseTtlValueReferenceAfterRun() throws Exception {
        ConcurrentMap<String, TransmittableThreadLocal<String>> ttlInstances = createTestTtlValue();

        Task task = new Task("1", ttlInstances);
        TtlRunnable ttlRunnable = TtlRunnable.get(task, true);

        Future<?> future = executorService.submit(ttlRunnable);
        assertNull(future.get());

        future = executorService.submit(ttlRunnable);
        try {
            future.get();
            fail();
        } catch (ExecutionException expected) {
            assertThat(expected.getCause(), instanceOf(IllegalStateException.class));
            assertThat(expected.getMessage(), containsString("TTL value reference is released after run!"));
        }
    }

    @Test
    public void test_get_same() throws Exception {
        Task task = new Task("1", null);
        TtlRunnable ttlRunnable = TtlRunnable.get(task);
        assertSame(task, ttlRunnable.getRunnable());
    }

    @Test
    public void test_get_idempotent() throws Exception {
        TtlRunnable task = TtlRunnable.get(new Task("1", null));
        try {
            TtlRunnable.get(task);
            fail();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString("Already TtlRunnable"));
        }
    }

    @Test
    public void test_get_nullInput() throws Exception {
        assertNull(TtlRunnable.get(null));
    }

    @Test
    public void test_gets() throws Exception {
        Task task1 = new Task("1", null);
        Task task2 = new Task("1", null);
        Runnable task3 = new Task("1", null);

        List<TtlRunnable> taskList = TtlRunnable.gets(Arrays.asList(task1, task2, null, task3));

        assertEquals(4, taskList.size());
        assertThat(taskList.get(0), instanceOf(TtlRunnable.class));
        assertThat(taskList.get(1), instanceOf(TtlRunnable.class));
        assertNull(taskList.get(2));
        assertThat(taskList.get(3), instanceOf(TtlRunnable.class));
    }
}
