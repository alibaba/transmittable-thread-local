package com.alibaba.ttl.threadpool.agent;


import com.alibaba.ttl.threadpool.agent.transformlet.TtlExecutorTransformlet;
import com.alibaba.ttl.threadpool.agent.transformlet.TtlForkJoinTransformlet;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.logging.Level;
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
        try {

            logger.info("[TtlAgent.premain] begin, agentArgs: " + agentArgs + ", Instrumentation: " + inst);

            @SuppressWarnings("unchecked") ClassFileTransformer transformer = new TtlTransformer(TtlExecutorTransformlet.class, TtlForkJoinTransformlet.class);
            inst.addTransformer(transformer, true);
            logger.info("[TtlAgent.premain] addTransformer " + transformer.getClass() + " success");

            logger.info("[TtlAgent.premain] end");

        } catch (Exception e) {
            String msg = "Fail to load TtlAgent , cause: " + e.toString();
            if (logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, msg, e);
            }
            throw new IllegalStateException(msg, e);
        }
    }
}
