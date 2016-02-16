package com.alibaba.ttl.threadpool.agent;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.Utils;
import com.alibaba.ttl.testmodel.Task;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.alibaba.ttl.Utils.CHILD;
import static com.alibaba.ttl.Utils.PARENT_AFTER_CREATE_TTL_TASK;
import static com.alibaba.ttl.Utils.PARENT_MODIFIED_IN_CHILD;
import static com.alibaba.ttl.Utils.PARENT_UNMODIFIED_IN_CHILD;
import static com.alibaba.ttl.Utils.assertttlInstances;
import static com.alibaba.ttl.Utils.copied;
import static com.alibaba.ttl.Utils.createTestTtlValue;
import static com.alibaba.ttl.Utils.expandThreadPool;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public final class AgentCheck {
    static ExecutorService executorService = Executors.newFixedThreadPool(3);
    static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);

    private AgentCheck() {
    	throw new InstantiationError( "Must not instantiate this class" );
    }
    static {
        expandThreadPool(executorService);
        expandThreadPool(scheduledExecutorService);
    }

    public static void main(String[] args) throws Exception {
        try {
            ConcurrentMap<String, TransmittableThreadLocal<String>> ttlInstances = createTestTtlValue();

            checkExecutorService(ttlInstances);
            checkScheduledExecutorService(ttlInstances);

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

    static void checkExecutorService(ConcurrentMap<String, TransmittableThreadLocal<String>> ttlInstances) throws Exception {
        Task task = new Task("1", ttlInstances);
        executorService.submit(task);

        // create after new Task, won't see parent value in in task!
        TransmittableThreadLocal<String> after = new TransmittableThreadLocal<String>();
        after.set(PARENT_AFTER_CREATE_TTL_TASK);
        ttlInstances.put(PARENT_AFTER_CREATE_TTL_TASK, after);

        Thread.sleep(1000);

        System.out.println(task.copied);

        // child Inheritable
        Utils.assertttlInstances(task.copied,
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD + "1", PARENT_MODIFIED_IN_CHILD,
                CHILD + "1", CHILD + "1"
        );

        // child do not effect parent
        assertttlInstances(copied(ttlInstances),
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD,
                PARENT_AFTER_CREATE_TTL_TASK, PARENT_AFTER_CREATE_TTL_TASK
        );
    }

    static void checkScheduledExecutorService(ConcurrentMap<String, TransmittableThreadLocal<String>> ttlInstances) throws Exception {
        Task task = new Task("2", ttlInstances);
        ScheduledFuture<?> future = scheduledExecutorService.schedule(task, 200, TimeUnit.MILLISECONDS);

        // create after new Task, won't see parent value in in task!
        TransmittableThreadLocal<String> after = new TransmittableThreadLocal<String>();
        after.set(PARENT_AFTER_CREATE_TTL_TASK);
        ttlInstances.put(PARENT_AFTER_CREATE_TTL_TASK, after);

        future.get();

        // child Inheritable
        assertttlInstances(task.copied,
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD + "2", PARENT_MODIFIED_IN_CHILD,
                CHILD + "2", CHILD + "2"
        );

        // child do not effect parent
        assertttlInstances(copied(ttlInstances),
                PARENT_UNMODIFIED_IN_CHILD, PARENT_UNMODIFIED_IN_CHILD,
                PARENT_MODIFIED_IN_CHILD, PARENT_MODIFIED_IN_CHILD,
                PARENT_AFTER_CREATE_TTL_TASK, PARENT_AFTER_CREATE_TTL_TASK
        );
    }
}
