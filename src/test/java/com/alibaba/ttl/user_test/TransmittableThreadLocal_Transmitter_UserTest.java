package com.alibaba.ttl.user_test;

import com.alibaba.ttl.TransmittableThreadLocal;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.*;

import static com.alibaba.ttl.Utils.expandThreadPool;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test {@link TransmittableThreadLocal.Transmitter} from user code(different package)
 */
public class TransmittableThreadLocal_Transmitter_UserTest {
    private static final String PARENT = "parent: " + new Date();
    private static final String CHILD = "child: " + new Date();

    private static final ExecutorService executorService = Executors.newFixedThreadPool(3);

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
    public void test_crr() throws Exception {
        final TransmittableThreadLocal<String> ttl = new TransmittableThreadLocal<String>();
        ttl.set(PARENT);

        final Object capture = TransmittableThreadLocal.Transmitter.capture();

        Future<?> future = executorService.submit(new Runnable() {
            @Override
            public void run() {
                ttl.set(CHILD);

                Object backup = TransmittableThreadLocal.Transmitter.replay(capture);

                assertEquals(PARENT, ttl.get());

                TransmittableThreadLocal.Transmitter.restore(backup);

                assertEquals(CHILD, ttl.get());
            }
        });

        assertEquals(PARENT, ttl.get());

        future.get(100, TimeUnit.MILLISECONDS);

        assertEquals(PARENT, ttl.get());
    }

    @Test
    public void test_runCallableWithCaptured() throws Exception {
        final TransmittableThreadLocal<String> ttl = new TransmittableThreadLocal<String>();
        ttl.set(PARENT);

        final Object capture = TransmittableThreadLocal.Transmitter.capture();

        Future<?> future = executorService.submit(new Runnable() {
            @Override
            public void run() {
                ttl.set("child");
                try {
                    TransmittableThreadLocal.Transmitter.runCallableWithCaptured(capture, new Callable<Object>() {
                        @Override
                        public Object call() throws Exception {
                            assertEquals(PARENT, ttl.get());
                            return ttl.get();
                        }
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        assertEquals(PARENT, ttl.get());

        future.get(100, TimeUnit.MILLISECONDS);

        assertEquals(PARENT, ttl.get());
    }
}
