@file:JvmName("AgentCheckMain")

package com.alibaba.ttl.threadpool.agent.check

import com.alibaba.support.junit.conditional.BelowJava7

fun main(args: Array<String>) {
    try {
        com.alibaba.ttl.threadpool.agent.check.executor.main(args)

        if (!BelowJava7().isSatisfied)
            com.alibaba.ttl.threadpool.agent.check.forkjoin.main(args)

    } catch (e: Throwable) {
        println("Exception when run AgentCheck: ")
        e.printStackTrace(System.out)
        System.exit(2)
    }
}
