package com.alibaba.ttl.demo.distributed_tracer;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.Utils;
import com.alibaba.ttl.threadpool.TtlExecutors;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * DistributedTracer(DT) use demo.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class DistributedTracerUseDemo_WeakReferenceInsteadOfRefCounter {
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
        public LeafSpanIdInfo leafSpanIdInfo;

        public DtTransferInfo(String traceId, String baseSpanId, LeafSpanIdInfo leafSpanIdInfo) {
            this.traceId = traceId;
            this.baseSpanId = baseSpanId;
            this.leafSpanIdInfo = leafSpanIdInfo;
        }

        @Override
        public String toString() {
            return "DtTransferInfo{" +
                    "traceId='" + traceId + '\'' +
                    ", baseSpanId='" + baseSpanId + '\'' +
                    ", leafSpanIdInfo=" + leafSpanIdInfo +
                    '}';
        }

        // Output GC operation
        @Override
        protected void finalize() throws Throwable {
            System.out.printf("DEBUG: gc DtTransferInfo traceId %s in thread %s: %s%n",
                    traceId, Thread.currentThread().getName(), this);
            super.finalize();
        }
    }

    private static TransmittableThreadLocal<DtTransferInfo> transferInfo = new TransmittableThreadLocal<DtTransferInfo>();

    static class LeafSpanIdInfo {
        AtomicInteger current = new AtomicInteger(1);

        @Override
        public String toString() {
            return "LeafSpanIdInfo{current=" + current + '}';
        }
    }

    static int increaseLeafSpanCurrentAndReturn() {
        DtTransferInfo dtTransferInfo = transferInfo.get();
        return dtTransferInfo.leafSpanIdInfo.current.getAndIncrement();
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 100; i++) {
            rpcInvokeIn();
        }

        Thread.sleep(1000);
        System.out.println("Call System.gc");
        // help to check GC status
        System.gc();
        System.out.println("Called System.gc");
        Thread.sleep(1000);
        System.out.println("Exit Main.");
    }


    static AtomicLong traceIdCounter = new AtomicLong();

    static void rpcInvokeIn() {
        ////////////////////////////////////////////////
        // DistributedTracer Framework Code
        ////////////////////////////////////////////////

        // Get Trace Id and Span Id from RPC Context
        String traceId = "traceId_XXXYYY" + traceIdCounter.getAndIncrement();
        String baseSpanId = "1.1";

        LeafSpanIdInfo leafSpanIdInfo = new LeafSpanIdInfo();
        transferInfo.set(new DtTransferInfo(traceId, baseSpanId, leafSpanIdInfo));


        ////////////////////////////////////////////////
        // Biz Code
        ////////////////////////////////////////////////
        syncMethod();


        ////////////////////////////////////////////////
        // DistributedTracer Framework Code
        ////////////////////////////////////////////////
        System.out.printf("Finished Rpc call %s with span %s.%n", traceId, leafSpanIdInfo);

        // release context in ThreadLocal, avoid to be hold by thread, GC friendly.
        transferInfo.remove();
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
