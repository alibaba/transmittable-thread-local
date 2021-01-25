package com.alibaba.ttl.threadpool.agent;

import com.alibaba.ttl.threadpool.agent.logging.Logger;
import com.alibaba.ttl.threadpool.agent.transformlet.ClassInfo;
import com.alibaba.ttl.threadpool.agent.transformlet.TtlTransformlet;
import edu.umd.cs.findbugs.annotations.NonNull;
import javassist.CannotCompileException;
import javassist.NotFoundException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.alibaba.ttl.threadpool.agent.transformlet.helper.TtlTransformletHelper.getLocationUrlOfClass;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 2.13.0
 */
final class TtlExtensionTransformletManager {
    private static final Logger logger = Logger.getLogger(TtlExtensionTransformletManager.class);

    private final List<String> extensionTransformletClassNameList;

    private static class ExtensionTransformletInfo {
        TtlTransformlet transformlet;
    }

    // Map: ClassLoader -> ExtensionTransformlet ClassName -> ExtensionTransformlet instance
    private final ConcurrentMap<ClassLoader, ConcurrentMap<String, ExtensionTransformletInfo>> classLoader2ClassName2Transformlet =
        new ConcurrentHashMap<ClassLoader, ConcurrentMap<String, ExtensionTransformletInfo>>();

    public TtlExtensionTransformletManager(@NonNull List<String> extensionTransformletClassNameList) {
        this.extensionTransformletClassNameList = extensionTransformletClassNameList;
    }

    public void collectExtensionTransformlet(@NonNull final ClassInfo classInfo) {
        final ClassLoader classLoader = classInfo.getClassLoader();
        // classloader may null be if the bootstrap loader,
        // which classloader must contains NO Ttl Agent Extension Transformlet, so just safe skip
        if (classLoader == null) return;

        ConcurrentMap<String, ExtensionTransformletInfo> className2Instance = classLoader2ClassName2Transformlet.get(classLoader);
        if (className2Instance != null) return;

        className2Instance = new ConcurrentHashMap<String, ExtensionTransformletInfo>();
        classLoader2ClassName2Transformlet.put(classLoader, className2Instance);

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
                        + " from classloader " + classInfo.getClassLoader()
                        + " at location " + getLocationUrlOfClass(clazz)
                        + ", but NOT subtype of " + TtlTransformlet.class.getName() + ", ignored!";
                    logger.error(msg);
                    continue;
                }

                extensionTransformletInfo.transformlet = (TtlTransformlet) clazz.getDeclaredConstructor().newInstance();
                final String msg = foundMsgHead + transformletClassName
                    + ", and loaded from classloader " + classInfo.getClassLoader()
                    + " at location " + getLocationUrlOfClass(clazz);
                logger.info(msg);
            } catch (ClassNotFoundException e) {
                final String msg = failMsgHead + transformletClassName + " from classloader " + classLoader + ", cause: " + e.toString();
                logger.warn(msg, e);
            } catch (IllegalAccessException e) {
                final String msg = failMsgHead + transformletClassName + " from classloader " + classLoader + ", cause: " + e.toString();
                logger.error(msg, e);
            } catch (InstantiationException e) {
                final String msg = failMsgHead + transformletClassName + " from classloader " + classLoader + ", cause: " + e.toString();
                logger.error(msg, e);
            } catch (NoSuchMethodException e) {
                final String msg = failMsgHead + transformletClassName + " from classloader " + classLoader + ", cause: " + e.toString();
                logger.error(msg, e);
            } catch (InvocationTargetException e) {
                final String msg = failMsgHead + transformletClassName + " from classloader " + classLoader + ", cause: " + e.toString();
                logger.error(msg, e);
            }
        }
    }

    public String extensionTransformletDoTransform(@NonNull final ClassInfo classInfo) throws NotFoundException, CannotCompileException, IOException {
        for (final Map.Entry<ClassLoader, ConcurrentMap<String, ExtensionTransformletInfo>> entry : classLoader2ClassName2Transformlet.entrySet()) {
            final ClassLoader classLoader = entry.getKey();
            if (classInfo.getClassLoader() != classLoader) continue;

            final ConcurrentMap<String, ExtensionTransformletInfo> className2Transformlet = entry.getValue();
            if (className2Transformlet == null) continue;

            for (final Map.Entry<String, ExtensionTransformletInfo> ee : className2Transformlet.entrySet()) {
                final String className = ee.getKey();
                final ExtensionTransformletInfo extensionTransformletInfo = ee.getValue();
                if (extensionTransformletInfo == null) continue;

                final TtlTransformlet transformlet = extensionTransformletInfo.transformlet;
                if (transformlet == null) continue;

                transformlet.doTransform(classInfo);
                if (classInfo.isModified()) {
                    return className;
                }
            }
        }

        return null;
    }
}
