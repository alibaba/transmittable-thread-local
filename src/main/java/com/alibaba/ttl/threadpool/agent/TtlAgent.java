package com.alibaba.ttl.threadpool.agent;


import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.logging.Logger;


/**
 * TTL Java Agent.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/instrument/package-summary.html">The mechanism for instrumentation</a>
 * @since 0.9.0
 */
public final class TtlAgent {
    private static final Logger logger = Logger.getLogger(TtlAgent.class.getName());

    private TtlAgent() {
        throw new InstantiationError("Must not instantiate this class");
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        logger.info("[TtlAgent.premain] begin, agentArgs: " + agentArgs);
        install(agentArgs, inst);
    }

    static void install(String agentArgs, Instrumentation inst) {
        logger.info("[TtlAgent.install] agentArgs: " + agentArgs + ", Instrumentation: " + inst);

        ClassFileTransformer transformer = new TtlTransformer();
        inst.addTransformer(transformer, true);

        logger.info("[TtlAgent.install] addTransformer success.");
    }
}
