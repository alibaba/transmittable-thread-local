package com.alibaba.ttl.threadpool.agent.transformlet.helper;

import com.alibaba.ttl.threadpool.agent.logging.Logger;
import com.alibaba.ttl.threadpool.agent.transformlet.TtlTransformlet;

import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

/**
 * Helper methods for {@link TtlTransformlet} implementation.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 2.13.0
 */
public final class TtlTransformletHelper {
    private static final Logger logger = Logger.getLogger(TtlTransformletHelper.class);

    public static URL getLocationUrlOfClass(Class<?> clazz) {
        try {
            // proxy classes is dynamic, no class file
            if (clazz.getName().startsWith("com.sun.proxy.")) return null;

            final ProtectionDomain protectionDomain = clazz.getProtectionDomain();
            if (protectionDomain == null) return null;

            final CodeSource codeSource = protectionDomain.getCodeSource();
            if (codeSource == null) return null;

            return codeSource.getLocation();
        } catch (Exception e) {
            logger.warn("Fail to getLocationUrlOfClass " + clazz.getName() + ", cause: " + e.toString());
            return null;
        }
    }

    public static String getLocationFileOfClass(Class<?> clazz) {
        final URL location = getLocationUrlOfClass(clazz);
        if (location == null) return null;

        return location.getFile();
    }

    private TtlTransformletHelper() {
        throw new InstantiationError("Must not instantiate this class");
    }
}
