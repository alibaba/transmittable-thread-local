package com.alibaba.ttl.threadpool.agent;

import com.alibaba.ttl.threadpool.agent.logging.Logger;
import com.alibaba.ttl.threadpool.agent.transformlet.TtlTransformlet;
import edu.umd.cs.findbugs.annotations.NonNull;
import net.bytebuddy.agent.builder.AgentBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 2.13.0
 */
final class TtlExtensionTransformletManager {
    private static final Logger logger = Logger.getLogger(TtlExtensionTransformletManager.class);

    private static final String TTL_AGENT_EXTENSION_TRANSFORMLET_FILE = "META-INF/ttl.agent.transformlets";

    public TtlExtensionTransformletManager() {
    }


    public String extensionTransformletDoTransform(AgentBuilder agentBuilder) throws IOException {
        final Map<String, TtlTransformlet> transformlets = new HashMap<String, TtlTransformlet>();
//            classLoader2ExtensionTransformletsIncludeParentCL.get(classInfo.getClassLoader());
        if (transformlets == null) return null;

        for (Map.Entry<String, TtlTransformlet> entry : transformlets.entrySet()) {
            final String className = entry.getKey();
            final TtlTransformlet transformlet = entry.getValue();
            transformlet.doTransform(agentBuilder);
        }
        // TODO: 增加扩展的包信息
        return null;
    }

    // NOTE: use WeakHashMap as a Set collection, value is always null.
    private final WeakHashMap<ClassLoader, ?> collectedClassLoaderHistory = new WeakHashMap<ClassLoader, Object>(512);

    // Map: ExtensionTransformlet ClassLoader -> ExtensionTransformlet ClassName -> ExtensionTransformlet instance(not include from parent classloader)
    private final WeakHashMap<ClassLoader, Map<String, TtlTransformlet>> classLoader2ExtensionTransformlets =
        new WeakHashMap<ClassLoader, Map<String, TtlTransformlet>>(512);

    // Map: ExtensionTransformlet ClassLoader -> ExtensionTransformlet ClassName -> ExtensionTransformlet instance(include from parent classloader)
    private final WeakHashMap<ClassLoader, Map<String, TtlTransformlet>> classLoader2ExtensionTransformletsIncludeParentCL =
        new WeakHashMap<ClassLoader, Map<String, TtlTransformlet>>(512);

    public void collectExtensionTransformlet() throws IOException {
        final ClassLoader classLoader = null;
//            classInfo.getClassLoader();
        // classloader may null be if the bootstrap loader,
        // which classloader must contains NO Ttl Agent Extension Transformlet, so just safe skip
        if (classLoader == null) return;

        // this classLoader is collected, so skip collection
        if (collectedClassLoaderHistory.containsKey(classLoader)) return;
        collectedClassLoaderHistory.put(classLoader, null);

        logger.info("[TtlExtensionTransformletCollector] collecting TTL Extension Transformlets from classloader " + classLoader);

        final LinkedHashSet<String> extensionTransformletClassNames = readExtensionTransformletClassNames(classLoader);

        final String foundMsgHead = "[TtlExtensionTransformletCollector] found TTL Extension Transformlet class ";
        final String failLoadMsgHead = "[TtlExtensionTransformletCollector] fail to load TTL Extension Transformlet ";
        final Map<ClassLoader, Set<TtlTransformlet>> loadedTransformlet =
            loadExtensionInstances(classLoader, extensionTransformletClassNames, TtlTransformlet.class, foundMsgHead, failLoadMsgHead);

        mergeToClassLoader2ExtensionTransformlet(classLoader2ExtensionTransformlets, loadedTransformlet);

        updateClassLoader2ExtensionTransformletsIncludeParentCL(
            classLoader2ExtensionTransformlets, classLoader2ExtensionTransformletsIncludeParentCL);
    }

    // extension transformlet configuration file URL location string -> URL contained extension transformlet class names
    private final Map<String, LinkedHashSet<String>> redExtensionTransformletFileHistory = new HashMap<String, LinkedHashSet<String>>();

    private LinkedHashSet<String> readExtensionTransformletClassNames(ClassLoader classLoader) throws IOException {
        final Enumeration<URL> extensionFiles = classLoader.getResources(TTL_AGENT_EXTENSION_TRANSFORMLET_FILE);

        final Pair<LinkedHashSet<String>, Set<String>> pair = readLinesFromExtensionFiles(extensionFiles, redExtensionTransformletFileHistory);
        final LinkedHashSet<String> extensionTransformletClassNames = pair.first;
        final Set<String> stringUrls = pair.second;
        if (!stringUrls.isEmpty())
            logger.info("[TtlExtensionTransformletCollector] found TTL Extension Transformlet configuration files from classloader "
                + classLoader + " : " + stringUrls);

        return extensionTransformletClassNames;
    }

    private static void mergeToClassLoader2ExtensionTransformlet(
        Map<ClassLoader, Map<String, TtlTransformlet>> destination, Map<ClassLoader, Set<TtlTransformlet>> loadedTransformlets
    ) {
        for (Map.Entry<ClassLoader, Set<TtlTransformlet>> entry : loadedTransformlets.entrySet()) {
            final ClassLoader classLoader = entry.getKey();
            final Set<TtlTransformlet> transformlets = entry.getValue();

            Map<String, TtlTransformlet> className2Transformlets = destination.get(classLoader);
            if (className2Transformlets == null) {
                className2Transformlets = new HashMap<String, TtlTransformlet>();
                destination.put(classLoader, className2Transformlets);
            }

            for (TtlTransformlet t : transformlets) {
                final String className = t.getClass().getName();
                if (className2Transformlets.containsKey(className)) continue;

                className2Transformlets.put(className, t);
                logger.info("[TtlExtensionTransformletCollector] add TTL Extension Transformlet " + className + " success");
            }
        }
    }

    static void updateClassLoader2ExtensionTransformletsIncludeParentCL(
        Map<ClassLoader, Map<String, TtlTransformlet>> classLoader2ExtensionTransformlets,
        Map<ClassLoader, Map<String, TtlTransformlet>> classLoader2ExtensionTransformletsIncludeParentCL
    ) {
        for (Map.Entry<ClassLoader, Map<String, TtlTransformlet>> entry : classLoader2ExtensionTransformlets.entrySet()) {
            final ClassLoader classLoader = entry.getKey();
            final Map<String, TtlTransformlet> merged = childClassLoaderFirstMergeTransformlets(classLoader2ExtensionTransformlets, classLoader);
            classLoader2ExtensionTransformletsIncludeParentCL.put(classLoader, merged);
        }
    }

    static Map<String, TtlTransformlet> childClassLoaderFirstMergeTransformlets(
        Map<ClassLoader, Map<String, TtlTransformlet>> classLoader2Transformlet, ClassLoader classLoader
    ) {
        Map<String, TtlTransformlet> ret = new HashMap<String, TtlTransformlet>();

        final ArrayDeque<ClassLoader> chain = new ArrayDeque<ClassLoader>();
        chain.add(classLoader);
        while (classLoader.getParent() != null) {
            classLoader = classLoader.getParent();

            chain.addFirst(classLoader);
        }

        for (ClassLoader loader : chain) {
            final Map<String, TtlTransformlet> m = classLoader2Transformlet.get(loader);
            if (m == null) continue;

            ret.putAll(m);
        }

        return ret;
    }

    // ======== Extension load util methods ========

    static <T> Map<ClassLoader, Set<T>> loadExtensionInstances(
        ClassLoader classLoader, LinkedHashSet<String> instanceClassNames, Class<T> superType,
        String foundMsgHead, String failLoadMsgHead
    ) {
        Map<ClassLoader, Set<T>> ret = new HashMap<ClassLoader, Set<T>>();

        for (final String className : instanceClassNames) {
            try {
                final Class<?> clazz = classLoader.loadClass(className);
                if (!superType.isAssignableFrom(clazz)) {
                    final String msg = foundMsgHead + className
                        + " from classloader " + classLoader
                        //+ " at location " + getLocationUrlOfClass(clazz)
                        + ", but NOT subtype of " + superType.getName() + ", ignored!";
                    logger.error(msg);
                    continue;
                }

                Object instance = clazz.getDeclaredConstructor().newInstance();

                final ClassLoader actualClassLoader = instance.getClass().getClassLoader();
                Set<T> set = ret.get(actualClassLoader);
                if (set == null) {
                    set = new HashSet<T>();
                    ret.put(actualClassLoader, set);
                }
                set.add(superType.cast(instance));

                final String msg = foundMsgHead + className
                    + ", and loaded from classloader " + classLoader;
                logger.info(msg);
            } catch (ClassNotFoundException e) {
                final String msg = failLoadMsgHead + className + " from classloader " + classLoader + ", cause: " + e.toString();
                logger.warn(msg, e);
            } catch (IllegalAccessException e) {
                final String msg = failLoadMsgHead + className + " from classloader " + classLoader + ", cause: " + e.toString();
                logger.error(msg, e);
            } catch (InstantiationException e) {
                final String msg = failLoadMsgHead + className + " from classloader " + classLoader + ", cause: " + e.toString();
                logger.error(msg, e);
            } catch (NoSuchMethodException e) {
                final String msg = failLoadMsgHead + className + " from classloader " + classLoader + ", cause: " + e.toString();
                logger.error(msg, e);
            } catch (InvocationTargetException e) {
                final String msg = failLoadMsgHead + className + " from classloader " + classLoader + ", cause: " + e.toString();
                logger.error(msg, e);
            }
        }

        return ret;
    }

    // return: read lines from URL, url strings
    @NonNull
    static Pair<LinkedHashSet<String>, Set<String>> readLinesFromExtensionFiles(
        /* input */ @NonNull Enumeration<URL> extensionFiles,
        /* input/output, map url string -> content lines */ @NonNull Map<String, LinkedHashSet<String>> redExtensionFilesHistory
    ) {
        final LinkedHashSet<String> mergedLines = new LinkedHashSet<String>();
        final Set<String> stringUrls = new HashSet<String>();

        while (extensionFiles.hasMoreElements()) {
            final URL url = extensionFiles.nextElement();

            final String urlString = url.toString();
            stringUrls.add(urlString);

            LinkedHashSet<String> lines;
            if (redExtensionFilesHistory.containsKey(urlString)) {
                lines = redExtensionFilesHistory.get(urlString);
            } else {
                lines = readLines(url);

                redExtensionFilesHistory.put(urlString, lines);
            }

            mergedLines.addAll(lines);
        }

        return new Pair<LinkedHashSet<String>, Set<String>>(mergedLines, stringUrls);
    }

    /**
     * this method is modified based on {@link java.util.ServiceLoader}
     */
    @SuppressWarnings("StatementWithEmptyBody")
    static LinkedHashSet<String> readLines(URL extensionFile) {
        InputStream inputStream = null;
        BufferedReader reader = null;

        LinkedHashSet<String> names = new LinkedHashSet<String>();
        try {
            inputStream = extensionFile.openStream();
            reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            int lineNum = 1;
            while ((lineNum = parseLine(extensionFile, reader, lineNum, names)) >= 0) ;
        } catch (IOException x) {
            logger.error("Error reading configuration file " + extensionFile, x);
        } finally {
            try {
                if (reader != null) reader.close();
                if (inputStream != null) inputStream.close();
            } catch (IOException y) {
                logger.warn("Error closing configuration file " + extensionFile, y);
            }
        }

        return names;
    }


    /**
     * this method is modified based on {@link java.util.ServiceLoader}
     */
    private static int parseLine(URL url, BufferedReader reader, int lineNum, LinkedHashSet<String> names) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return -1;
        }

        // remove comments that start with `#`
        int ci = line.indexOf('#');
        if (ci >= 0) line = line.substring(0, ci);

        line = line.trim();

        int n = line.length();
        if (n != 0) {
            if ((line.indexOf(' ') >= 0) || (line.indexOf('\t') >= 0)) {
                logger.error("Illegal syntax " + line + "in configuration file" + url + ", contains space or tab; ignore this line!");
                return lineNum + 1;
            }

            int cp = line.codePointAt(0);
            if (!Character.isJavaIdentifierStart(cp)) {
                logger.error("Illegal extension class name " + line + " in configuration file " + url + "; ignore this line!");
                return lineNum + 1;
            }
            for (int i = Character.charCount(cp); i < n; i += Character.charCount(cp)) {
                cp = line.codePointAt(i);
                if (!Character.isJavaIdentifierPart(cp) && (cp != '.')) {
                    logger.error("Illegal extension class name: " + line + " in configuration file " + url + "; ignore this line!");
                    return lineNum + 1;
                }
            }

            names.add(line);
        }

        return lineNum + 1;
    }

    static class Pair<T, U> {
        T first;
        U second;

        public Pair(T first, U second) {
            this.first = first;
            this.second = second;
        }
    }
}
