package com.alibaba.mtc.threadpool.agent;

import com.alibaba.mtc.MtContextThreadLocal;
import com.alibaba.mtc.Utils;
import com.alibaba.mtc.testmodel.Task;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.alibaba.mtc.Utils.CHILD;
import static com.alibaba.mtc.Utils.PARENT_AFTER_CREATE_MTC_TASK;
import static com.alibaba.mtc.Utils.PARENT_MODIFIED_IN_CHILD;
import static com.alibaba.mtc.Utils.PARENT_UNMODIFIED_IN_CHILD;
import static com.alibaba.mtc.Utils.assertMtContext;
import static com.alibaba.mtc.Utils.copied;
import static com.alibaba.mtc.Utils.createTestMtContexts;
import static com.alibaba.mtc.Utils.expandThreadPool;

/**
 * @author ding.lid
 */
public class AgentCheck {
    static ExecutorService executorService = Executors.newFixedThreadPool(3);
    static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);

    static {
        expandThreadPool(executorService);
        expandThreadPool(scheduledExecutorService);
    }

    public static void main(String[] args) throws Exception {
        try {
            ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts = createTestMtContexts();

            checkExecutorService(mtContexts);
            checkScheduledExecutorService(mtContexts);

            System.out.println();
            System.out.println("====================================");
            System.out.println("OK!");
            System.out.println("====================================");
        } finally {
            executorService.shutdown();
            scheduledExecutorService.shutdown();
            executorService.awaitTermination(3, TimeUnit.MINUTES);
            scheduledExecutorService.awaitTermination(3, TimeUnit.MINUTES);
        }
    }

    static void checkExecutorService(ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts) throws Exception {
        Task task = new Task("1", mtContexts);
        executorService.submit(task);

        // create after new Task, won't see parent value in in task!
        MtContextThreadLocal<String> after = new MtContextThreadLocal<String>();
        after.set(PARENT_AFTER_CREATE_MTC_TASK);
        mtContexts.put(PARENT_AFTER_CREATE_MTC_TASK, after);

        Thread.sleep(1000);

        System.out.println(task.copied);

        // child Inheritable
        Utils.assertMtContext(task.copied,
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

    static void checkScheduledExecutorService(ConcurrentMap<String, MtContextThreadLocal<String>> mtContexts) throws Exception {
        Task task = new Task("2", mtContexts);
        ScheduledFuture<?> future = scheduledExecutorService.schedule(task, 200, TimeUnit.MILLISECONDS);

        // create after new Task, won't see parent value in in task!
        MtContextThreadLocal<String> after = new MtContextThreadLocal<String>();
        after.set(PARENT_AFTER_CREATE_MTC_TASK);
        mtContexts.put(PARENT_AFTER_CREATE_MTC_TASK, after);

        future.get();

        // child Inheritable
        assertMtContext(task.copied,
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD + "2", PARENT_MODIFIED_IN_CHILD,
                CHILD + "2", CHILD + "2"
        );

        // child do not effect parent
        assertMtContext(copied(mtContexts),
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD,
                PARENT_AFTER_CREATE_MTC_TASK, PARENT_AFTER_CREATE_MTC_TASK
        );
    }
}
