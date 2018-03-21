package com.alibaba.ttl.reported_bugs;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.TtlRunnable;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;

/**
 * Bug URL: https://github.com/alibaba/transmittable-thread-local/issues/70
 * Reporter: @aftersss
 */
public class Bug70_Test {
    private static final String HELLO = "hello";

    @Test
    public void test_bug70() throws Exception {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final InheritableThreadLocal<String> threadLocal = new TransmittableThreadLocal<>();

        threadLocal.set(HELLO);
        assertEquals(HELLO, threadLocal.get());

        final FutureTask<String> task1 = new FutureTask<>(threadLocal::get);
        executorService.submit(TtlRunnable.get(task1))
                .get();
        assertEquals(HELLO, task1.get());


        final AtomicReference<FutureTask<String>> taskRef = new AtomicReference<>();
        Thread thread = new Thread(() -> {
            FutureTask<String> task2 = new FutureTask<>(threadLocal::get);
            TtlRunnable runnable = TtlRunnable.get(task2, false, false);
            executorService.submit(runnable);
            taskRef.set(task2);
        });
        thread.start();
        thread.join();
        assertEquals(HELLO, taskRef.get().get());
    }
}
