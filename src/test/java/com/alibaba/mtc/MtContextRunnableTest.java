package com.alibaba.mtc;

import com.alibaba.mtc.testmodel.FooMtContextThreadLocal;
import com.alibaba.mtc.testmodel.FooPojo;
import com.alibaba.mtc.testmodel.FooTask;
import com.alibaba.mtc.testmodel.Task;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
public class MtContextRunnableTest {
    static ExecutorService executorService = Executors.newFixedThreadPool(3);

    static {
        expandThreadPool(executorService);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        executorService.shutdown();
    }

    @Test
    public void test_MtContextRunnable_inSameThread() throws Exception {
        ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts = createTestMtContexts();

        Task task = new Task("1", mtContexts);
        MtContextRunnable mtContextRunnable = MtContextRunnable.get(task);

        // create after new Task
        MtContextThreadLocal<String> after = new MtContextThreadLocal<String>();
        after.set(PARENT_AFTER_CREATE_MTC_TASK);
        mtContexts.put(PARENT_AFTER_CREATE_MTC_TASK, after);

        mtContextRunnable.run();

        // child Inheritable
        assertMtContext(task.copied,
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD + "1", PARENT_MODIFIED_IN_CHILD,
                CHILD + "1", CHILD + "1"
        );

        // child do not effect parent
        assertMtContext(copied(mtContexts),
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD, // restored after call!
                PARENT_AFTER_CREATE_MTC_TASK, PARENT_AFTER_CREATE_MTC_TASK
        );
    }

    @Test
    public void test_MtContextRunnable_withThread() throws Exception {
        ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts = createTestMtContexts();

        Task task = new Task("1", mtContexts);
        Thread thread1 = new Thread(task);

        // create after new Task, won't see parent value in in task!
        MtContextThreadLocal<String> after = new MtContextThreadLocal<String>();
        after.set(PARENT_AFTER_CREATE_MTC_TASK);
        mtContexts.put(PARENT_AFTER_CREATE_MTC_TASK, after);

        thread1.start();
        thread1.join();

        // child Inheritable
        assertMtContext(task.copied,
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD + "1", PARENT_MODIFIED_IN_CHILD,
                CHILD + "1", CHILD + "1"
        );

        // child do not effect parent
        assertMtContext(copied(mtContexts),
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD,
                PARENT_AFTER_CREATE_MTC_TASK, PARENT_AFTER_CREATE_MTC_TASK
        );
    }

    @Test
    public void test_MtContextRunnable_withExecutorService() throws Exception {
        ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts = createTestMtContexts();

        Task task = new Task("1", mtContexts);
        MtContextRunnable mtContextRunnable = MtContextRunnable.get(task);

        // create after new Task, won't see parent value in in task!
        MtContextThreadLocal<String> after = new MtContextThreadLocal<String>();
        after.set(PARENT_AFTER_CREATE_MTC_TASK);
        mtContexts.put(PARENT_AFTER_CREATE_MTC_TASK, after);

        executorService.execute(mtContextRunnable);
        Thread.sleep(100);

        // child Inheritable
        assertMtContext(task.copied,
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD + "1", PARENT_MODIFIED_IN_CHILD,
                CHILD + "1", CHILD + "1"
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

        // remove MtContextThreadLocal
        mtContexts.get(PARENT_UNMODIFIED_IN_CHILD).remove();

        Task task = new Task("1", mtContexts);
        MtContextRunnable mtContextRunnable = MtContextRunnable.get(task);

        // create after new Task, won't see parent value in in task!
        MtContextThreadLocal<String> after = new MtContextThreadLocal<String>();
        after.set(PARENT_AFTER_CREATE_MTC_TASK);
        mtContexts.put(PARENT_AFTER_CREATE_MTC_TASK, after);

        executorService.execute(mtContextRunnable);
        Thread.sleep(100);

        // child Inheritable
        assertMtContext(task.copied,
                PARENT_MODIFIED_IN_CHILD + "1", PARENT_MODIFIED_IN_CHILD,
                CHILD + 1, CHILD + 1
        );

        // child do not effect parent
        assertMtContext(copied(mtContexts),
                PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD,
                PARENT_AFTER_CREATE_MTC_TASK, PARENT_AFTER_CREATE_MTC_TASK
        );
    }

    @Test
    public void test_MtContextRunnable_copyObject() throws Exception {
        ConcurrentMap<String, MtContextThreadLocal<FooPojo>> mtContexts =
                new ConcurrentHashMap<String, MtContextThreadLocal<FooPojo>>();

        MtContextThreadLocal<FooPojo> parent = new FooMtContextThreadLocal();
        parent.set(new FooPojo(PARENT_UNMODIFIED_IN_CHILD, 1));
        mtContexts.put(PARENT_UNMODIFIED_IN_CHILD, parent);

        MtContextThreadLocal<FooPojo> p = new FooMtContextThreadLocal();
        p.set(new FooPojo(PARENT_MODIFIED_IN_CHILD, 2));
        mtContexts.put(PARENT_MODIFIED_IN_CHILD, p);

        FooTask task = new FooTask("1", mtContexts);
        MtContextRunnable mtContextRunnable = MtContextRunnable.get(task);

        // create after new Task, won't see parent value in in task!
        MtContextThreadLocal<FooPojo> after = new FooMtContextThreadLocal();
        after.set(new FooPojo(PARENT_AFTER_CREATE_MTC_TASK, 4));
        mtContexts.put(PARENT_AFTER_CREATE_MTC_TASK, after);

        executorService.execute(mtContextRunnable);
        Thread.sleep(100);

        // child Inheritable
        assertEquals(3, task.copied.size());
        assertEquals(new FooPojo(PARENT_UNMODIFIED_IN_CHILD, 1), task.copied.get(PARENT_UNMODIFIED_IN_CHILD));
        assertEquals(new FooPojo(PARENT_MODIFIED_IN_CHILD + "1", 2), task.copied.get(PARENT_MODIFIED_IN_CHILD));
        assertEquals(new FooPojo(CHILD + 1, 3), task.copied.get(CHILD + 1));

        // child do not effect parent
        Map<String, Object> copied = copied(mtContexts);
        assertEquals(3, copied.size());
        assertEquals(new FooPojo(PARENT_UNMODIFIED_IN_CHILD, 1), copied.get(PARENT_UNMODIFIED_IN_CHILD));
        assertEquals(new FooPojo(PARENT_MODIFIED_IN_CHILD, 2), copied.get(PARENT_MODIFIED_IN_CHILD));
        assertEquals(new FooPojo(PARENT_AFTER_CREATE_MTC_TASK, 4), copied.get(PARENT_AFTER_CREATE_MTC_TASK));
    }

    @Test
    public void test_releaseMtContextAfterRun() throws Exception {
        ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts = createTestMtContexts();

        Task task = new Task("1", mtContexts);
        MtContextRunnable mtContextRunnable = MtContextRunnable.get(task, true);

        Future<?> future = executorService.submit(mtContextRunnable);
        assertNull(future.get());

        future = executorService.submit(mtContextRunnable);
        try {
            future.get();
            fail();
        } catch (ExecutionException expected) {
            assertThat(expected.getCause(), instanceOf(IllegalStateException.class));
            assertThat(expected.getMessage(), containsString("MtContext is released!"));
        }
    }

    @Test
    public void test_sameTask() throws Exception {
        Task task = new Task("1", null);
        MtContextRunnable mtContextRunnable = MtContextRunnable.get(task);
        assertSame(task, mtContextRunnable.getRunnable());
    }

    @Test
    public void test_get_idempotent() throws Exception {
        MtContextRunnable task = MtContextRunnable.get(new Task("1", null));
        try {
            MtContextRunnable.get(task);
            fail();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), containsString("Already MtContextRunnable"));
        }
    }

    @Test
    public void test_get_nullInput() throws Exception {
        assertNull(MtContextRunnable.get(null));
    }

    @Test
    public void test_gets() throws Exception {
        Task task1 = new Task("1", null);
        Task task2 = new Task("1", null);
        Runnable task3 = new Task("1", null);

        List<MtContextRunnable> taskList = MtContextRunnable.gets(Arrays.asList(task1, task2, null, task3));

        assertEquals(4, taskList.size());
        assertThat(taskList.get(0), instanceOf(MtContextRunnable.class));
        assertThat(taskList.get(1), instanceOf(MtContextRunnable.class));
        assertNull(taskList.get(2));
        assertThat(taskList.get(3), instanceOf(MtContextRunnable.class));
    }
}
