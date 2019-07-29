@file:JvmName("RxJavaIntegrationDemo")

package com.alibaba.integration

import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.ttl.TtlRunnable
import io.reactivex.Flowable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers

fun main() {
    val ttl = TransmittableThreadLocal<String?>()
    ttl.set("init")
    // expand thread pool
    Flowable.range(1, 20)
        .flatMap {
            Flowable.just(it)
                .observeOn(Schedulers.computation())
                .doOnNext {
                    Thread.sleep(2)
                    println("expand thread pool: [${Thread.currentThread().name}] $it ${ttl.get()}")
                }
        }
        .toList()
        .blockingGet()

    // TTL integration for RxJava
    RxJavaPlugins.setScheduleHandler(TtlRunnable::get)

    ttl.set("jerry")
    Flowable.just("Hello")
        .observeOn(Schedulers.computation())
        .doOnNext {
            println("[${Thread.currentThread().name}] $it ${ttl.get()}")
        }
        .toList()
        .blockingGet()

    ttl.set("tom")
    Flowable.just("Hello")
        .subscribeOn(Schedulers.computation())
        .doOnNext {
            println("[${Thread.currentThread().name}] $it ${ttl.get()}")
        }
        .toList()
        .blockingGet()
}
