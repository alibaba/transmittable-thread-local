package com.alibaba.ttl.classloader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.alibaba.ttl.threadpool.agent.TtlAgent;

public class TtlAgentJarUtil {

    private static final Logger logger = Logger.getLogger(TtlAgentJarUtil.class.getName());

    private static final Pattern CLASS_NAME = Pattern.compile(TtlAgent.class.getName().replace('.', '/') + ".class");

    private static URL cachedURL = null;

    @SuppressWarnings({ "rawtypes" })
    public static URL getAgentJarFilePath() {

        if (cachedURL != null) {
            return cachedURL;
        }
        ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();
        if ((sysClassLoader instanceof URLClassLoader)) {
            URL[] jarUrls = ((URLClassLoader) sysClassLoader).getURLs();
            for (URL jarFullPath : jarUrls) {
                if ((jarFullPath.getFile().endsWith(".jar"))
                        && (jarFullPath.getFile().indexOf("transmittable-thread-local") != -1)) {
                    Collection<String> jarClassCollection = getClassCollectionsByJarUrl(jarFullPath, CLASS_NAME);
                    if (!jarClassCollection.isEmpty()) {
                        cachedURL = jarFullPath;
                        return cachedURL;
                    }
                }
            }
        }
        // JDK 10 and later needs this code
        Class loadClasssysClassLoader = sysClassLoader.getClass();
        if (loadClasssysClassLoader.getName().equals("jdk.internal.loader.BuiltinClassLoader")
                || loadClasssysClassLoader.getSuperclass().getName().equals("jdk.internal.loader.BuiltinClassLoader")) {
            try {

                //ucp field has class jar file system location information that we need
                Field classPathField = loadClasssysClassLoader.getDeclaredField("ucp");
                Object classLoaderModule = java.lang.Class.class.getDeclaredMethod("getModule")
                        .invoke(loadClasssysClassLoader);
                //get system classloader's module
                Field moduleField = java.lang.Class.class.getDeclaredField("module");
                moduleField.setAccessible(true);

                // set agent class module the same as classloader's module
                moduleField.set(TtlAgentJarUtil.class, classLoaderModule);

                // in jdk10 after caller( in this case TtlAgentJarUtil.class) has the same module with target class (systemClassLoad of jdk 10)
                // then we can call setAccessible(true) to access private field
                classPathField.setAccessible(true);
                Object urlclasspath = classPathField.get(sysClassLoader);

                // jdk.internal.loader.URLClassPath is JDK 10 special class, we can only reflect invoke
                Method getUrlMethod = Class.forName("jdk.internal.loader.URLClassPath").getDeclaredMethod("getURLs");
                URL[] jarUrls = (URL[]) getUrlMethod.invoke(urlclasspath);
                for (URL jarFullPath : jarUrls) {
                    if ((jarFullPath.getFile().endsWith(".jar"))
                            && (jarFullPath.getFile().indexOf("transmittable-thread-local") != -1)) {
                        Collection<String> jarClassCollection = getClassCollectionsByJarUrl(jarFullPath, CLASS_NAME);
                        if (!jarClassCollection.isEmpty()) {
                            cachedURL = jarFullPath;
                            return cachedURL;
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        cachedURL = TtlAgent.class.getProtectionDomain().getCodeSource().getLocation();
        return cachedURL;
    }

    private static Collection<String> getClassCollectionsByJarUrl(URL paramURL, Pattern classNamePattern) {
        JarFile localJarFile = null;
        try {
            localJarFile = getJarFileByPath(paramURL);
            List<String> destinationClassList = new ArrayList<String>();
            Enumeration<JarEntry> jarEntry = localJarFile.entries();
            while (jarEntry.hasMoreElements()) {
                JarEntry jarEntryObject = jarEntry.nextElement();
                if (classNamePattern.matcher(jarEntryObject.getName()).matches()) {
                    destinationClassList.add(jarEntryObject.getName());
                }
            }
            return destinationClassList;
        } catch (Exception t) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, "Unable to search the agent jar for " + classNamePattern.pattern(), t);
            }
            return Collections.emptyList();
        } finally {
            if (localJarFile != null) {
                try {
                    localJarFile.close();
                } catch (IOException localIOException3) {
                }
            }
        }
    }

    public static Collection<String> getClassCollectionsByClassPattern(Pattern classPattern) {
        URL jarUrl = getAgentJarFilePath();
        return getClassCollectionsByJarUrl(jarUrl, classPattern);
    }

    public static File getJarFile() {
        URL argentUrl = getAgentJarFilePath();
        if (argentUrl != null) {
            File jarFile = new File(replaceJarFullPath(argentUrl));
            if (jarFile.exists()) {
                return jarFile.getParentFile();
            }
        }
        return null;
    }

    public static JarFile getJarFileByPath(URL jarFullPath) {
        if (jarFullPath != null) {
            try {
                return new JarFile(replaceJarFullPath(jarFullPath));
            } catch (IOException localIOException) {
            }
        }
        return null;
    }

    private static String replaceJarFullPath(URL jarFullPath) {
        if (jarFullPath == null) {
            return null;
        }
        try {
            return URLDecoder.decode(jarFullPath.getFile().replace("+", "%2B"), "UTF-8");
        } catch (IOException localIOException) {
        }
        return null;
    }

}
