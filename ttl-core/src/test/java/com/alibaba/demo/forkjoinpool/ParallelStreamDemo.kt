package com.alibaba.demo.forkjoinpool

import java.util.concurrent.ConcurrentSkipListSet

/**
 * Parallel Stream use demo.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
fun main() {
    println("availableProcessors: ${Runtime.getRuntime().availableProcessors()}")

    val threadNames: MutableSet<String> = ConcurrentSkipListSet()

    (0..100).toList().stream().parallel().mapToInt {
        threadNames.add(Thread.currentThread().name)
        Thread.sleep(10)
        println("map $it @ thread ${Thread.currentThread().name}")

        it
    }.sum().let {
        println("sum result: $it")
    }

    println(threadNames.joinToString(
        separator = "\n\t",
        prefix = "run threads(${threadNames.size}):\n\t"
    ))
}

/*
Output:

availableProcessors: 12
map 78 @ thread ForkJoinPool.commonPool-worker-7
map 76 @ thread ForkJoinPool.commonPool-worker-21
map 91 @ thread ForkJoinPool.commonPool-worker-19
map 71 @ thread ForkJoinPool.commonPool-worker-13
map 65 @ thread main
map 97 @ thread ForkJoinPool.commonPool-worker-3
map 32 @ thread ForkJoinPool.commonPool-worker-5
map 15 @ thread ForkJoinPool.commonPool-worker-9
map 79 @ thread ForkJoinPool.commonPool-worker-17
map 82 @ thread ForkJoinPool.commonPool-worker-27
map 53 @ thread ForkJoinPool.commonPool-worker-31
map 57 @ thread ForkJoinPool.commonPool-worker-23
map 77 @ thread ForkJoinPool.commonPool-worker-21
map 86 @ thread ForkJoinPool.commonPool-worker-7
map 72 @ thread ForkJoinPool.commonPool-worker-13
map 92 @ thread ForkJoinPool.commonPool-worker-19
map 66 @ thread main
map 83 @ thread ForkJoinPool.commonPool-worker-27
map 58 @ thread ForkJoinPool.commonPool-worker-23
map 54 @ thread ForkJoinPool.commonPool-worker-31
map 98 @ thread ForkJoinPool.commonPool-worker-3
map 33 @ thread ForkJoinPool.commonPool-worker-5
map 16 @ thread ForkJoinPool.commonPool-worker-9
map 80 @ thread ForkJoinPool.commonPool-worker-17
map 75 @ thread ForkJoinPool.commonPool-worker-21
map 87 @ thread ForkJoinPool.commonPool-worker-7
map 93 @ thread ForkJoinPool.commonPool-worker-19
map 73 @ thread ForkJoinPool.commonPool-worker-13
map 67 @ thread main
map 81 @ thread ForkJoinPool.commonPool-worker-27
map 56 @ thread ForkJoinPool.commonPool-worker-23
map 55 @ thread ForkJoinPool.commonPool-worker-31
map 17 @ thread ForkJoinPool.commonPool-worker-9
map 99 @ thread ForkJoinPool.commonPool-worker-3
map 31 @ thread ForkJoinPool.commonPool-worker-5
map 84 @ thread ForkJoinPool.commonPool-worker-17
map 63 @ thread ForkJoinPool.commonPool-worker-19
map 89 @ thread ForkJoinPool.commonPool-worker-7
map 74 @ thread ForkJoinPool.commonPool-worker-13
map 95 @ thread ForkJoinPool.commonPool-worker-21
map 62 @ thread ForkJoinPool.commonPool-worker-27
map 60 @ thread ForkJoinPool.commonPool-worker-23
map 100 @ thread ForkJoinPool.commonPool-worker-3
map 51 @ thread ForkJoinPool.commonPool-worker-31
map 13 @ thread ForkJoinPool.commonPool-worker-9
map 35 @ thread ForkJoinPool.commonPool-worker-5
map 85 @ thread ForkJoinPool.commonPool-worker-17
map 64 @ thread ForkJoinPool.commonPool-worker-19
map 90 @ thread ForkJoinPool.commonPool-worker-7
map 69 @ thread ForkJoinPool.commonPool-worker-13
map 96 @ thread ForkJoinPool.commonPool-worker-21
map 94 @ thread ForkJoinPool.commonPool-worker-27
map 61 @ thread ForkJoinPool.commonPool-worker-23
map 14 @ thread ForkJoinPool.commonPool-worker-9
map 36 @ thread ForkJoinPool.commonPool-worker-5
map 44 @ thread ForkJoinPool.commonPool-worker-3
map 52 @ thread ForkJoinPool.commonPool-worker-31
map 88 @ thread ForkJoinPool.commonPool-worker-17
map 68 @ thread ForkJoinPool.commonPool-worker-19
map 40 @ thread ForkJoinPool.commonPool-worker-7
map 70 @ thread ForkJoinPool.commonPool-worker-13
map 48 @ thread ForkJoinPool.commonPool-worker-21
map 46 @ thread ForkJoinPool.commonPool-worker-27
map 59 @ thread ForkJoinPool.commonPool-worker-23
map 12 @ thread ForkJoinPool.commonPool-worker-9
map 34 @ thread ForkJoinPool.commonPool-worker-5
map 50 @ thread ForkJoinPool.commonPool-worker-31
map 38 @ thread ForkJoinPool.commonPool-worker-17
map 45 @ thread ForkJoinPool.commonPool-worker-3
map 41 @ thread ForkJoinPool.commonPool-worker-7
map 43 @ thread ForkJoinPool.commonPool-worker-19
map 28 @ thread ForkJoinPool.commonPool-worker-13
map 49 @ thread ForkJoinPool.commonPool-worker-21
map 47 @ thread ForkJoinPool.commonPool-worker-27
map 7 @ thread ForkJoinPool.commonPool-worker-23
map 21 @ thread ForkJoinPool.commonPool-worker-9
map 25 @ thread ForkJoinPool.commonPool-worker-3
map 3 @ thread ForkJoinPool.commonPool-worker-31
map 26 @ thread ForkJoinPool.commonPool-worker-5
map 39 @ thread ForkJoinPool.commonPool-worker-17
map 42 @ thread ForkJoinPool.commonPool-worker-7
map 19 @ thread ForkJoinPool.commonPool-worker-19
map 29 @ thread ForkJoinPool.commonPool-worker-13
map 1 @ thread ForkJoinPool.commonPool-worker-21
map 0 @ thread ForkJoinPool.commonPool-worker-27
map 8 @ thread ForkJoinPool.commonPool-worker-23
map 27 @ thread ForkJoinPool.commonPool-worker-5
map 22 @ thread ForkJoinPool.commonPool-worker-9
map 37 @ thread ForkJoinPool.commonPool-worker-17
map 4 @ thread ForkJoinPool.commonPool-worker-3
map 10 @ thread ForkJoinPool.commonPool-worker-31
map 20 @ thread ForkJoinPool.commonPool-worker-19
map 18 @ thread ForkJoinPool.commonPool-worker-7
map 30 @ thread ForkJoinPool.commonPool-worker-13
map 2 @ thread ForkJoinPool.commonPool-worker-21
map 23 @ thread ForkJoinPool.commonPool-worker-23
map 6 @ thread ForkJoinPool.commonPool-worker-27
map 9 @ thread ForkJoinPool.commonPool-worker-5
map 5 @ thread ForkJoinPool.commonPool-worker-3
map 11 @ thread ForkJoinPool.commonPool-worker-31
map 24 @ thread ForkJoinPool.commonPool-worker-23
sum result: 5050
run threads(12):
	ForkJoinPool.commonPool-worker-13
	ForkJoinPool.commonPool-worker-17
	ForkJoinPool.commonPool-worker-19
	ForkJoinPool.commonPool-worker-21
	ForkJoinPool.commonPool-worker-23
	ForkJoinPool.commonPool-worker-27
	ForkJoinPool.commonPool-worker-3
	ForkJoinPool.commonPool-worker-31
	ForkJoinPool.commonPool-worker-5
	ForkJoinPool.commonPool-worker-7
	ForkJoinPool.commonPool-worker-9
	main

 */
