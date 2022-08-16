package com.alibaba.demo.ttl.agent;

import com.alibaba.ttl.threadpool.agent.TtlAgent;

import java.lang.instrument.Instrumentation;
import java.util.logging.Logger;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public final class YourXxxAgent {
    private static final Logger logger = Logger.getLogger(YourXxxAgent.class.getName());

    public static void premain(String agentArgs, Instrumentation inst) throws Exception {
        TtlAgent.premain(agentArgs, inst); // add TTL Transformer

        // add your Transformer
        // ...
    }
}
