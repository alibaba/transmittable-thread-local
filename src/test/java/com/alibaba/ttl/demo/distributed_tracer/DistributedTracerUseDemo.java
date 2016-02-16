package com.alibaba.ttl.demo.distributed_tracer;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.Utils;
import com.alibaba.ttl.threadpool.TtlExecutors;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DistributedTracer(DT) use demo.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class DistributedTracerUseDemo {
    static ThreadFactory threadFactory = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "Executors");
            thread.setDaemon(true);
            return thread;
        }
    };


    static ExecutorService executorService = TtlExecutors.getTtlExecutorService(Executors.newFixedThreadPool(1, threadFactory));

    static {
        // 挤满线程, 保证线程不是用的时候new的, 确保验证TTL的传递功能
        Utils.expandThreadPool(executorService);
    }

    static class DtTransferInfo {
        public String traceId;
        public String baseSpanId;

        public DtTransferInfo(String traceId, String baseSpanId) {
            this.traceId = traceId;
            this.baseSpanId = baseSpanId;
        }
    }

    private static TransmittableThreadLocal<DtTransferInfo> transferInfo = new TransmittableThreadLocal<DtTransferInfo>() {
        /*
        @Override
        protected DtTransferInfo childValue(DtTransferInfo parentValue) {
            // **注意**：
            // 新建线程时，从父线程继承值时，计数加1
            // 对应线程结束时，没有回调以清理ThreadLocal中的Context！，Bug！！
            // InheritableThreadLocal 没有提供 对应的拦截方法。。。 计数不配对了。。。
            // 但是一个线程就一个Context没清，线程数有限，Context占用内存一般很小，可以接受。
            increaseSpanIdRefCounter();

            return super.childValue(parentValue);
        }
        */

        @Override
        protected DtTransferInfo copy(DtTransferInfo parentValue) {
            increaseSpanIdRefCounter();
            return super.childValue(parentValue);
        }

        @Override
        protected void afterExecute() {
            decreaseSpanIdRefCounter();
        }
    };

    static class LeafSpanIdInfo {
        AtomicInteger current = new AtomicInteger(1);
        AtomicInteger refCounter = new AtomicInteger(0);
    }


    private static Map<String, LeafSpanIdInfo> traceId2LeafSpanIdInfo = new ConcurrentHashMap<String, LeafSpanIdInfo>();

    static void increaseSpanIdRefCounter() {
        DtTransferInfo dtTransferInfo = transferInfo.get();
        String traceId = dtTransferInfo.traceId;
        int refCounter = traceId2LeafSpanIdInfo.get(traceId).refCounter.incrementAndGet();

        System.out.printf("DEBUG: Increase reference counter(%s) for traceId %s in thread %s%n", refCounter, traceId, Thread.currentThread().getName());
    }

    static void decreaseSpanIdRefCounter() {
        DtTransferInfo dtTransferInfo = transferInfo.get();
        String traceId = dtTransferInfo.traceId;
        LeafSpanIdInfo leafSpanIdInfo = traceId2LeafSpanIdInfo.get(traceId);

        int refCounter = leafSpanIdInfo.refCounter.decrementAndGet();
        System.out.printf("DEBUG: Decrease reference counter(%s) for traceId %s in thread %s%n", refCounter, traceId, Thread.currentThread().getName());

        if (refCounter == 0) {
            traceId2LeafSpanIdInfo.remove(traceId);

            System.out.printf("DEBUG: Clear traceId2LeafSpanIdInfo for traceId %s in thread %s%n", traceId, Thread.currentThread().getName());
        } else if (refCounter < 0) {
            throw new IllegalStateException("Leaf Span Id Info Reference counter has Bug!!");
        }
    }

    static int increaseLeafSpanCurrentAndReturn() {
        DtTransferInfo dtTransferInfo = transferInfo.get();
        String traceId = dtTransferInfo.traceId;
        LeafSpanIdInfo leafSpanIdInfo = traceId2LeafSpanIdInfo.get(traceId);

        return leafSpanIdInfo.current.getAndIncrement();
    }

    public static void main(String[] args) throws Exception {
        rpcInvokeIn();

        Thread.sleep(1000);
    }

    static void rpcInvokeIn() {
        ////////////////////////////////////////////////
        // DistributedTracer Framework Code
        ////////////////////////////////////////////////

        // Get Trace Id and Span Id from RPC Context
        String traceId = "traceId_XXXYYY";
        String baseSpanId = "1.1";

        transferInfo.set(new DtTransferInfo(traceId, baseSpanId));
        traceId2LeafSpanIdInfo.put(traceId, new LeafSpanIdInfo());

        increaseSpanIdRefCounter();

        ////////////////////////////////////////////////
        // Biz Code
        ////////////////////////////////////////////////
        syncMethod();

        ////////////////////////////////////////////////
        // DistributedTracer Framework Code
        ////////////////////////////////////////////////
        decreaseSpanIdRefCounter();
    }

    static void syncMethod() {
        // async call by TTL Executor, Test OK!
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                asyncMethod();

            }
        });

        // async call by new Thread
        // FIXME Bug!! 没有 Increase/Decrease reference counter操作!
        new Thread(new Runnable() {
            @Override
            public void run() {
                syncMethod_ByNewThread();
            }
        }, "Thread-by-new").start();

        invokeServerWithRpc("server 1");
    }

    static void asyncMethod() {
        invokeServerWithRpc("server 2");
    }

    static void syncMethod_ByNewThread() {
        invokeServerWithRpc("server 3");
    }


    // RPC invoke
    static void invokeServerWithRpc(String server) {
        ////////////////////////////////////////////////
        // DistributedTracer Framework Code
        ////////////////////////////////////////////////

        int leafSpanCurrent = increaseLeafSpanCurrentAndReturn();

        // Set RpcContext
        // Mocked, should use RPC util to get Rpc Context instead
        Map<String, String> rpcContext = new ConcurrentHashMap<String, String>();

        rpcContext.put("traceId", transferInfo.get().traceId);
        rpcContext.put("spanId", transferInfo.get().baseSpanId + "." + leafSpanCurrent);

        // Do Rpc
        // ...
        System.out.printf("Do Rpc invocation to server %s with %s%n", server, rpcContext);
    }
}
