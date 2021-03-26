package com.alibaba.ttl.threadpool.agent

import com.alibaba.ttl.threadpool.agent.logging.Logger

object TtlAgentLoggerInitializer {
    init {
        if (!TtlAgent.isTtlAgentLoaded()) {
            Logger.setLoggerImplType(Logger.STDOUT)
        }
    }
}
