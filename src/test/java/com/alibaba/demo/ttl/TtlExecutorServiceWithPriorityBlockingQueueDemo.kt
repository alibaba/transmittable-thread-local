package com.alibaba.demo.ttl

import com.alibaba.ttl.threadpool.TtlExecutors
import java.lang.Thread.sleep
import java.util.concurrent.BlockingQueue
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

fun main() {
    demoSubmitComparableTaskToTtlExecutorServiceWithPriorityBlockingQueue()

    demoSubmitOrderTaskToTtlExecutorServiceWithPriorityBlockingQueue()
}

/**
 * Demo for cooperation TTL executor(ThreadPoolExecutor) with PriorityBlockingQueue, for Comparable Runnable.
 *
 * if you use TTL Agent, no extra work(getTtlRunnableUnwrapComparatorForComparableRunnable) is need.
 * aka. rewriting PriorityBlockingQueue by TTL Agent automatically and transparently.
 */
fun demoSubmitComparableTaskToTtlExecutorServiceWithPriorityBlockingQueue() {
    val comparator: Comparator<Runnable> = TtlExecutors.getTtlRunnableUnwrapComparatorForComparableRunnable()

    // explicit PriorityBlockingQueue Comparator argument
    //   instead of default constructor PriorityBlockingQueue()
    //
    // aka. rewrite
    //     val priorityBlockingQueue                   = PriorityBlockingQueue()
    // to
    val priorityBlockingQueue: BlockingQueue<Runnable> = PriorityBlockingQueue(11, comparator)

    val threadPoolExecutor = ThreadPoolExecutor(1, 2, 1, TimeUnit.SECONDS, priorityBlockingQueue)
    val ttlExecutorService = TtlExecutors.getTtlExecutorService(threadPoolExecutor)!!

    ttlExecutorService.execute(BizComparableTask(0))
    ttlExecutorService.execute(BizComparableTask(1))
    ttlExecutorService.execute(BizComparableTask(2))
    ttlExecutorService.execute(BizComparableTask(42))
    ttlExecutorService.execute(BizComparableTask(9))
    ttlExecutorService.execute(BizComparableTask(8))
    ttlExecutorService.execute(BizComparableTask(7))

    threadPoolExecutor.shutdown()
    threadPoolExecutor.awaitTermination(5, TimeUnit.SECONDS)
}


/**
 * Demo for cooperation TTL executor(ThreadPoolExecutor) with PriorityBlockingQueue, for Runnable Comparator.
 *
 * if you use TTL Agent, no extra work(getTtlRunnableUnwrapComparator) is need.
 * aka. rewriting Comparator by TTL Agent automatically and transparently.
 */
fun demoSubmitOrderTaskToTtlExecutorServiceWithPriorityBlockingQueue() {
    val comparator: Comparator<Runnable> = compareBy { (it as BizOrderTask).order }
    val ttlRunnableUnwrapComparator: Comparator<Runnable>? = TtlExecutors.getTtlRunnableUnwrapComparator(comparator)

    // use TtlRunnableUnwrapComparator instead original comparator
    //
    // aka. rewrite
    //     val priorityBlockingQueue                           = PriorityBlockingQueue(11, comparator)
    // to
    val priorityBlockingQueue: PriorityBlockingQueue<Runnable> = PriorityBlockingQueue(11, ttlRunnableUnwrapComparator)

    val threadPoolExecutor = ThreadPoolExecutor(1, 2, 1, TimeUnit.SECONDS, priorityBlockingQueue)
    val ttlExecutorService = TtlExecutors.getTtlExecutorService(threadPoolExecutor)!!

    ttlExecutorService.execute(BizOrderTask(0))
    ttlExecutorService.execute(BizOrderTask(1))
    ttlExecutorService.execute(BizOrderTask(2))
    ttlExecutorService.execute(BizOrderTask(42))
    ttlExecutorService.execute(BizOrderTask(9))
    ttlExecutorService.execute(BizOrderTask(8))
    ttlExecutorService.execute(BizOrderTask(7))

    threadPoolExecutor.shutdown()
    threadPoolExecutor.awaitTermination(5, TimeUnit.SECONDS)
}


private data class BizComparableTask(val num: Int) : Runnable, Comparable<Runnable> {
    override fun run() {
        sleep(10)
        println("run BizComparableTask $num")
    }

    override fun compareTo(other: Runnable): Int = num - (other as BizComparableTask).num
}

private data class BizOrderTask(val order: Int) : Runnable {
    override fun run() {
        sleep(10)
        println("run BizOrderTask $order")
    }
}
