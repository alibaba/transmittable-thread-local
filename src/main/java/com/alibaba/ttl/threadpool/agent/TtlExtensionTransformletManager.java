package com.alibaba.ttl.threadpool.agent;

import com.alibaba.ttl.threadpool.agent.logging.Logger;
import com.alibaba.ttl.threadpool.agent.transformlet.ClassInfo;
import com.alibaba.ttl.threadpool.agent.transformlet.TtlTransformlet;
import edu.umd.cs.findbugs.annotations.NonNull;
import javassist.CannotCompileException;
import javassist.NotFoundException;

import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link TtlTransformlet} for {@link java.util.concurrent.Executor}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @author wuwen5 (wuwen.55 at aliyun dot com)
 * @see java.util.concurrent.Executor
 * @see java.util.concurrent.ExecutorService
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see java.util.concurrent.ScheduledThreadPoolExecutor
 * @see java.util.concurrent.Executors
 * @since 2.13.0
 */
final class TtlExtensionTransformletManager {
    private static final Logger logger = Logger.getLogger(TtlExtensionTransformletManager.class);

    private final List<String> extensionTransformletClassNameList;

    private static class ExtensionTransformletInfo {
        TtlTransformlet transformlet;
    }

    // Map: ClassLoader -> ExtensionTransformlet ClassName -> ExtensionTransformlet instance
    private final ConcurrentMap<ClassLoader, ConcurrentMap<String, ExtensionTransformletInfo>> classLoader2ClassName2JavassistTransformlet =
        new ConcurrentHashMap<ClassLoader, ConcurrentMap<String, ExtensionTransformletInfo>>();

    public TtlExtensionTransformletManager(@NonNull List<String> extensionTransformletClassNameList) {
        //this.extensionTransformletClassNameList = extensionTransformletClassNameList;
        this.extensionTransformletClassNameList = new ArrayList<String>();
        this.extensionTransformletClassNameList.add("com.alibaba.ttl.integration.vertx4.agent.transformlet.NettySingleThreadEventExecutorTtlTransformlet");
        this.extensionTransformletClassNameList.add("com.alibaba.ttl.integration.vertx4.agent.transformlet.VertxFutureTtlTransformlet");
    }

    public void collectExtensionTransformlet(@NonNull final ClassInfo classInfo) {
        final ClassLoader classLoader = classInfo.getClassLoader();
        // class loader may null be if the bootstrap loader,
        // which class loader must contains NO Ttl Agent Extension Transformlet, so just safe skip
        if (classLoader == null) return;

        ConcurrentMap<String, ExtensionTransformletInfo> className2Instance = classLoader2ClassName2JavassistTransformlet.get(classLoader);
        if (className2Instance != null) return;

        className2Instance = new ConcurrentHashMap<String, ExtensionTransformletInfo>();
        classLoader2ClassName2JavassistTransformlet.put(classLoader, className2Instance);

        final String foundMsgHead = "[TtlExtensionTransformletCollector] found TTL Extension Transformlet ";
        final String failMsgHead = "[TtlExtensionTransformletCollector] fail to load TTL Extension Transformlet ";
        for (final String transformletClassName : extensionTransformletClassNameList) {
            try {
                ExtensionTransformletInfo extensionTransformletInfo = className2Instance.get(transformletClassName);
                if (extensionTransformletInfo != null) continue;

                extensionTransformletInfo = new ExtensionTransformletInfo();
                className2Instance.put(transformletClassName, extensionTransformletInfo);

                final Class<?> clazz = classLoader.loadClass(transformletClassName);
                if (!TtlTransformlet.class.isAssignableFrom(clazz)) {
                    final String msg = foundMsgHead + transformletClassName
                        + " from class loader " + classLoader + " @ " + getFileLocationOfClass(clazz)
                        + ", but NOT subtype of " + TtlTransformlet.class.getName() + ", ignored!";
                    logger.error(msg);
                    continue;
                }

                extensionTransformletInfo.transformlet = (TtlTransformlet) clazz.newInstance();
                final String msg = foundMsgHead + transformletClassName
                    + ", and loaded from class loader " + classLoader + " @ " + getFileLocationOfClass(clazz);
                logger.info(msg);
            } catch (ClassNotFoundException e) {
                // do nothing
            } catch (IllegalAccessException e) {
                final String msg = failMsgHead + transformletClassName + " from class loader " + classLoader + ", cause: " + e.toString();
                logger.error(msg, e);
            } catch (InstantiationException e) {
                final String msg = failMsgHead + transformletClassName + " from class loader " + classLoader + ", cause: " + e.toString();
                logger.error(msg, e);
            }
        }
    }

    public void extensionTransformletDoTransform(@NonNull final ClassInfo classInfo) throws NotFoundException, CannotCompileException, IOException {
        for (final Map.Entry<ClassLoader, ConcurrentMap<String, ExtensionTransformletInfo>> entry : classLoader2ClassName2JavassistTransformlet.entrySet()) {
            final ClassLoader classLoader = entry.getKey();
            if (classInfo.getClassLoader() != classLoader) continue;

            final ConcurrentMap<String, ExtensionTransformletInfo> className2JavassistTransformlet = entry.getValue();
            if (className2JavassistTransformlet == null) continue;

            for (final Map.Entry<String, ExtensionTransformletInfo> ee : className2JavassistTransformlet.entrySet()) {
                final ExtensionTransformletInfo extensionTransformletInfo = ee.getValue();
                if (extensionTransformletInfo == null) continue;

                final TtlTransformlet transformlet = extensionTransformletInfo.transformlet;
                if (transformlet == null) continue;

                transformlet.doTransform(classInfo);
            }
        }
    }

    private static String getFileLocationOfClass(Class<?> clazz) {
        final ProtectionDomain protectionDomain = clazz.getProtectionDomain();
        if (protectionDomain == null) return null;

        final CodeSource codeSource = protectionDomain.getCodeSource();
        if (codeSource == null) return null;

        final URL location = codeSource.getLocation();
        if (location == null) return null;
        return location.getFile();
    }
}
