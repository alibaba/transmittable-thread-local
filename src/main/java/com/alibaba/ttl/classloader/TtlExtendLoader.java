package com.alibaba.ttl.classloader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TtlExtendLoader {

    private static final TtlClassCache<Class<?>> LOAD_CLASS_TREE = new TtlClassCache<Class<?>>();

    private static final Map<String, byte[]> CLASS_BYTE_MAP = new HashMap<String, byte[]>(30);

    private static final Logger logger = Logger.getLogger(TtlExtendLoader.class.getName());

    public static boolean isExtendLoadClass(String className) {
        if (CLASS_BYTE_MAP.isEmpty()) {
            init();
        }
        return CLASS_BYTE_MAP.containsKey(className);
    }

    public static boolean isAgentLoadClass(String className) {
        return className.startsWith("com.alibaba.ttl.");
    }

    public static Class<?> getClazz(ClassLoader paramClassLoader, String className) {
        return (Class<?>) LOAD_CLASS_TREE.loadClassByName(paramClassLoader, className);
    }

    public static void setClazzLoaderMap(ClassLoader paramClassLoader, String className, Class<?> paramClass) {
        LOAD_CLASS_TREE.setClazzLoaderMap(paramClassLoader, className, paramClass);
    }

    public static byte[] getClazzBytes(String paramString) {
        if (CLASS_BYTE_MAP.isEmpty()) {
            init();
        }
        return (byte[]) CLASS_BYTE_MAP.get(paramString);
    }

    private static final int EOF = -1;

    public static void init() {
        synchronized (TtlExtendLoader.class) {
            if (CLASS_BYTE_MAP.isEmpty()) {
                JarInputStream jarInputStream = null;
                try {
                    URL agentJarUrl = TtlAgentJarUtil.getAgentJarFilePath();
                    JarFile jarfile = TtlAgentJarUtil.getJarFileByPath(agentJarUrl);
                    jarInputStream = new JarInputStream(agentJarUrl.openStream());
                    JarEntry agentJarEntry = null;
                    while ((agentJarEntry = jarInputStream.getNextJarEntry()) != null) {
                        if (agentJarEntry.getName().endsWith(".class")) {
                            ByteArrayOutputStream output = new ByteArrayOutputStream();
                            copy(jarfile.getInputStream(agentJarEntry), output);
                            byte[] classBytes = output.toByteArray();
                            final String className = agentJarEntry.getName()
                                    .substring(0, agentJarEntry.getName().length() - 6).replace('/', '.');
                            CLASS_BYTE_MAP.put(className, classBytes);
                        }
                    }
                } catch (Exception t) {
                    if (logger.isLoggable(Level.WARNING)) {
                        logger.log(Level.WARNING, "TTL exception when load class, cause: " + t.toString(), t);
                    }
                } finally {
                    try {
                        if (jarInputStream != null) {
                            jarInputStream.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    public static long copyLarge(InputStream input, OutputStream output) throws IOException {
        return copyLarge(input, output, new byte[DEFAULT_BUFFER_SIZE]);
    }

    public static long copyLarge(InputStream input, OutputStream output, byte[] buffer) throws IOException {
        long count = 0;
        int n = 0;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

}
