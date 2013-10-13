package com.oldratlee.mtc.threadpool.agent;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;


/**
 * @author ding.lid
 * @since 0.9.0
 */
public class MtContextAgent {
    private static Logger logger = LoggerFactory.getLogger(MtContextAgent.class);

    public static void premain(String agentArgs, Instrumentation inst) {
        logger.warn("[MtContextAgent.premain] begin, agentArgs: {}.", agentArgs);
        install(agentArgs, inst);
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        logger.warn("[MtContextAgent.agentmain] begin, agentArgs: {}.", agentArgs);
        install(agentArgs, inst);
    }

    static void install(String agentArgs, Instrumentation inst) {
        logger.warn("[MtContextAgent] agentArgs: {}, Instrumentation: {}.", agentArgs, inst);

        ClassFileTransformer transformer = new MtContextTransformer();
        inst.addTransformer(transformer, true);

        logger.warn("[MtContextAgent] addTransformer success.");
    }
}
