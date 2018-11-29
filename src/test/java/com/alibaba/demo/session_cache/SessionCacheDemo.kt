package com.alibaba.demo.session_cache

import com.alibaba.expandThreadPool
import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.ttl.TtlRunnable
import com.alibaba.ttl.threadpool.TtlExecutors
import io.reactivex.Flowable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.*
import java.util.concurrent.*


class SessionCacheDemo {
    @Test
    fun invokeInThreadOfThreadPool() {
        val bizService = BizService()
        val printer: () -> Unit = { System.out.printf("[%20s] cache: %s%n", Thread.currentThread().name, bizService.getCacheItem()) }

        executorService.submit(Callable<Item> {
            bizService.getItemByCache().also { printer() }
        }).get()

        printer()

        // here service invocation will use item cache
        bizService.getItemByCache()
        printer()
    }

    @Test
    fun invokeInThreadOfRxJava() {
        val bizService = BizService()
        val printer: (Item) -> Unit = { System.out.printf("[%30s] cache: %s%n", Thread.currentThread().name, bizService.getCacheItem()) }

        Flowable.just(bizService)
                .observeOn(Schedulers.io())
                .map<Item>(BizService::getItemByCache)
                .doOnNext(printer)
                .blockingSubscribe(printer)

        // here service invocation will use item cache
        bizService.getItemByCache()
                .let(printer)
    }

    companion object {
        private val executorService = Executors.newFixedThreadPool(3).let {
            expandThreadPool(it)
            // TTL integration for thread pool
            TtlExecutors.getTtlExecutorService(it)!!
        }

        @BeforeClass
        @JvmStatic
        @Suppress("unused")
        fun beforeClass() {
            // expand Schedulers.io()
            (0 until Runtime.getRuntime().availableProcessors() * 2)
                    .map {
                        FutureTask {
                            Thread.sleep(10)
                            it
                        }.apply { Schedulers.io().scheduleDirect(this) }
                    }
                    .forEach { it.get() }

            // TTL integration for RxJava
            RxJavaPlugins.setScheduleHandler(TtlRunnable::get)
        }

        @AfterClass
        @JvmStatic
        @Suppress("unused")
        fun afterClass() {
            executorService.shutdown()
            executorService.awaitTermination(100, TimeUnit.MILLISECONDS)
            if (!executorService.isTerminated) Assert.fail("Fail to shutdown thread pool")
        }
    }

    @After
    fun tearDown() {
        BizService.clearCache()
    }
}

/**
 * Mock Service
 */
private class BizService {
    init {
        // NOTE: AVOID cache object lazy init
        getCache()
    }

    fun getItem(): Item = Item(ThreadLocalRandom.current().nextInt(0, 10_000))

    /**
     * get biz data, usually use spring cache. here is simple implementation
     */
    fun getItemByCache(): Item {
        return getCache().computeIfAbsent(ONLY_KEY) { getItem() }
    }

    fun getCacheItem(): Item? = getCache()[ONLY_KEY]

    companion object {
        private const val ONLY_KEY = "ONLY_KEY"

        private val cacheContext = object : TransmittableThreadLocal<ConcurrentMap<String, Item>>() {
            // init cache
            override fun initialValue(): ConcurrentMap<String, Item> = ConcurrentHashMap()
        }

        private fun getCache() = cacheContext.get()

        fun clearCache() {
            getCache().clear()
        }
    }
}

/**
 * Mock Cache Data
 */
private data class Item(val id: Int)
