package com.alibaba.ttl.threadpool.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.alibaba.ttl.classloader.TtlAgentJarUtil;
import com.alibaba.ttl.threadpool.agent.transformlet.TtlClassloaderTransformlet;
import com.alibaba.ttl.threadpool.agent.transformlet.TtlExecutorTransformlet;
import com.alibaba.ttl.threadpool.agent.transformlet.TtlForkJoinTransformlet;

public class TtlAgent {

    private static final Logger logger = Logger.getLogger(TtlAgent.class.getName());

    private TtlAgent() {
        throw new InstantiationError("Must not instantiate this class");
    }

    public static void premain(String agentArgs, Instrumentation paramInstrumentation) {
        try {
            //even api can append agent jar to xbootclasspath,we still need enhance subclassloaders,
            //because in tomcat's WebAppBaseClassLoader, our business class such as filter serlvet still can't load TransmittableThreadLocal
            JarFile localJarFile = TtlAgentJarUtil.getJarFileByPath(TtlAgentJarUtil.getAgentJarFilePath());
            paramInstrumentation.appendToBootstrapClassLoaderSearch(localJarFile);
            paramInstrumentation.appendToSystemClassLoaderSearch(localJarFile);
            logger.info("[TtlAgent.premain] begin, agentArgs: " + agentArgs + ", Instrumentation: " + paramInstrumentation);

            @SuppressWarnings("unchecked")
            ClassFileTransformer transformer = new TtlTransformer(
                    TtlClassloaderTransformlet.class, 
                    TtlExecutorTransformlet.class,
                    TtlForkJoinTransformlet.class);

            paramInstrumentation.addTransformer(transformer, true);
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
