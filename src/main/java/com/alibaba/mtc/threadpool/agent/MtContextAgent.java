package com.alibaba.mtc.threadpool.agent;


import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.logging.Logger;


/**
 * @author ding.lid
 * @see <a href="http://docs.oracle.com/javase/6/docs/api/java/lang/instrument/package-summary.html">The mechanism for instrumentation</a>
 * @since 0.9.0
 */
public class MtContextAgent {
    private static final Logger logger = Logger.getLogger(MtContextAgent.class.getName());

    public static void premain(String agentArgs, Instrumentation inst) {
        logger.warning("[MtContextAgent.premain] begin, agentArgs: " + agentArgs);
        install(agentArgs, inst);
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        logger.warning("[MtContextAgent.agentmain] begin, agentArgs: " + agentArgs);
        install(agentArgs, inst);
    }

    static void install(String agentArgs, Instrumentation inst) {
        logger.warning("[MtContextAgent.install] agentArgs: " + agentArgs + ", Instrumentation: " + inst);

        ClassFileTransformer transformer = new MtContextTransformer();
        inst.addTransformer(transformer, true);

        logger.warning("[MtContextAgent.install] addTransformer success.");
    }
}
