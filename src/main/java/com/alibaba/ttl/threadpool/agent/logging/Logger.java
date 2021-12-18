package com.alibaba.ttl.threadpool.agent.logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

/**
 * Logger adaptor for ttl TTL agent/transformlet. Only use for TTL agent/transformlet!
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see com.alibaba.ttl.threadpool.agent.transformlet.TtlTransformlet
 * @see com.alibaba.ttl.threadpool.agent.TtlAgent
 * @since 2.13.0
 */
public abstract class Logger {
    public static final String STDOUT = "STDOUT";
    public static final String STDERR = "STDERR";

    private static volatile int loggerImplType = -1;

    public static void setLoggerImplType(String type) {
        if (loggerImplType != -1) {
            throw new IllegalStateException("TTL logger implementation type is already set! type = " + loggerImplType);
        }

        if (STDERR.equalsIgnoreCase(type)) loggerImplType = 0;
        else if (STDOUT.equalsIgnoreCase(type)) loggerImplType = 1;
        else loggerImplType = 0;
    }

    /**
     * Only for test code
     */
    public static void setLoggerImplTypeIfNotSetYet(String type) {
        if (loggerImplType == -1) setLoggerImplType(type);
    }

    public static Logger getLogger(Class<?> clazz) {
        if (loggerImplType == -1) throw new IllegalStateException("TTL logger implementation type is NOT set!");

        switch (loggerImplType) {
            case 1:
                return new StdOutLogger(clazz);
            default:
                return new StdErrorLogger(clazz);
        }
    }

    final Class<?> logClass;

    private Logger(Class<?> logClass) {
        this.logClass = logClass;
    }

    public void info(String msg) {
        log(Level.INFO, msg, null);
    }

    public void warn(String msg) {
        log(Level.WARNING, msg, null);
    }

    public void warn(String msg, Throwable thrown) {
        log(Level.WARNING, msg, thrown);
    }

    public void error(String msg) {
        log(Level.SEVERE, msg, null);
    }

    public void error(String msg, Throwable thrown) {
        log(Level.SEVERE, msg, thrown);
    }

    protected abstract void log(Level level, String msg, Throwable thrown);

    private static class StdErrorLogger extends Logger {
        StdErrorLogger(Class<?> clazz) {
            super(clazz);
        }

        @Override
        public void log(Level level, String msg, Throwable thrown) {
            if (level == Level.SEVERE) {
                final String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
                System.err.printf("%s %s [%s] %s: %s%n", time, level, Thread.currentThread().getName(), logClass.getSimpleName(), msg);
                if (thrown != null) thrown.printStackTrace();
            }
        }
    }

    private static class StdOutLogger extends Logger {
        StdOutLogger(Class<?> clazz) {
            super(clazz);
        }

        @Override
        public void log(Level level, String msg, Throwable thrown) {
            final String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
            System.out.printf("%s %s [%s] %s: %s%n", time, level, Thread.currentThread().getName(), logClass.getSimpleName(), msg);
            if (thrown != null) thrown.printStackTrace(System.out);
        }
    }
}
