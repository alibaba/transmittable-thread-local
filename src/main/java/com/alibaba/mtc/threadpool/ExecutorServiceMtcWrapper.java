package com.alibaba.mtc.threadpool;

import com.alibaba.mtc.MtContextCallable;
import com.alibaba.mtc.MtContextRunnable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * {@link com.alibaba.mtc.MtContextThreadLocal} Wrapper of {@link ExecutorService},
 * transmit the {@link com.alibaba.mtc.MtContextThreadLocal} from the task submit time of {@link Runnable} or {@link Callable}
 * to the execution time of {@link Runnable} or {@link Callable}.
 *
 * @author ding.lid
 * @since 0.9.0
 */
class ExecutorServiceMtcWrapper extends ExecutorMtcWrapper implements ExecutorService {
    final ExecutorService executorService;

    public ExecutorServiceMtcWrapper(ExecutorService executorService) {
        super(executorService);
        this.executorService = executorService;
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return executorService.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return executorService.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return executorService.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return executorService.submit(MtContextCallable.get(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return executorService.submit(MtContextRunnable.get(task), result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return executorService.submit(MtContextRunnable.get(task));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return executorService.invokeAll(MtContextCallable.gets(tasks));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.invokeAll(MtContextCallable.gets(tasks), timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return executorService.invokeAny(MtContextCallable.gets(tasks));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return executorService.invokeAny(MtContextCallable.gets(tasks), timeout, unit);
    }
}
