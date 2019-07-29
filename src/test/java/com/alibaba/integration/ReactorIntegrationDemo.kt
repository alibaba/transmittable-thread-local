@file:JvmName("ReactorIntegrationDemo")

package com.alibaba.integration

import com.alibaba.ttl.TransmittableThreadLocal
import com.alibaba.ttl.threadpool.TtlExecutors
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers

fun main() {
    // TTL integration for Reactor
    Schedulers.addExecutorServiceDecorator("TransmittableThreadLocal") { _, scheduledExecutorService ->
        TtlExecutors.getTtlScheduledExecutorService(scheduledExecutorService)
    }

    val ttl = TransmittableThreadLocal<String?>()
    ttl.set("init")
    // expand thread pool
    Flux.range(1, 20)
        .flatMap {
            Flux.just(it)
                .subscribeOn(Schedulers.parallel())
                .doOnNext {
                    Thread.sleep(2)
                    println("expand thread pool: [${Thread.currentThread().name}] $it ${ttl.get()}")
                }
        }
        .collectList()
        .block()

    ttl.set("jerry")
    Flux.just("Hello")
        .subscribeOn(Schedulers.parallel())
        .doOnNext {
            println("[${Thread.currentThread().name}] $it ${ttl.get()}")
        }
        .collectList()
        .block()

    ttl.set("tom")
    Flux.just("Hello")
        .subscribeOn(Schedulers.parallel())
        .doOnNext {
            println("[${Thread.currentThread().name}] $it ${ttl.get()}")
        }
        .collectList()
        .block()
}
