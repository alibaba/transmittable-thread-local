@file:JvmName("AgentCheckMain")

package com.alibaba.ttl.threadpool.agent.check

import org.apache.commons.lang3.JavaVersion
import org.apache.commons.lang3.SystemUtils.isJavaVersionAtLeast

fun main(args: Array<String>) {
    try {
        com.alibaba.ttl.threadpool.agent.check.executor.main(args)

        if (isJavaVersionAtLeast(JavaVersion.JAVA_1_7))
            com.alibaba.ttl.threadpool.agent.check.forkjoin.main(args)

    } catch (e: Throwable) {
        println("Exception when run AgentCheck: ")
        e.printStackTrace(System.out)
        System.exit(2)
    }
}
