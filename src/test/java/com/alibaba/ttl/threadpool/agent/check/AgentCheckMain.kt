@file:JvmName("AgentCheckMain")

package com.alibaba.ttl.threadpool.agent.check

import com.alibaba.support.junit.conditional.BelowJava7

fun main(args: Array<String>) {
    com.alibaba.ttl.threadpool.agent.check.executor.main(args)

    if (!BelowJava7().isSatisfied)
        com.alibaba.ttl.threadpool.agent.check.forkjoin.main(args)
}
