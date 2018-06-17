package com.alibaba.ttl.threadpool.agent;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TTL {@link ClassFileTransformer} of Java Agent
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 0.9.0
 */
public class TtlTransformer implements ClassFileTransformer {
    private static final Logger logger = Logger.getLogger(TtlTransformer.class.getName());

    private static final byte[] EMPTY_BYTE_ARRAY = {};

    @SuppressWarnings("unchecked")
    final List<JavassistTransformlet> transformletList = new ArrayList();

    @SuppressWarnings("unchecked")
    public TtlTransformer(Class<? extends JavassistTransformlet>... transformletClasses) throws Exception {
        for (Class<? extends JavassistTransformlet> transformletClass : transformletClasses) {
            final JavassistTransformlet transformlet = transformletClass.getConstructor().newInstance();
            transformletList.add(transformlet);

            logger.info("[TtlTransformer] add Transformlet " + transformletClass + " success");
        }
    }

    @Override
    public final byte[] transform(final ClassLoader loader, final String classFile, final Class<?> classBeingRedefined,
                                  final ProtectionDomain protectionDomain, final byte[] classFileBuffer) {
        try {
            // Lambda has no class file, no need to transform, just return.
            if (classFile == null) {
                return EMPTY_BYTE_ARRAY;
            }

            final String className = toClassName(classFile);
            for (JavassistTransformlet transformlet : transformletList) {
                if (transformlet.needTransform(className)) {
                    logger.info("Transforming class " + className);
                    final CtClass clazz = getCtClass(classFileBuffer, loader);
                    transformlet.doTransform(clazz);
                    return clazz.toBytecode();
                }
            }
        } catch (Throwable t) {
            String msg = "Fail to transform class " + classFile + ", cause: " + t.toString();
            if (logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, msg, t);
            }
            throw new IllegalStateException(msg, t);
        }

        return EMPTY_BYTE_ARRAY;
    }

    private static String toClassName(final String classFile) {
        return classFile.replace('/', '.');
    }

    private static CtClass getCtClass(final byte[] classFileBuffer, final ClassLoader classLoader) throws IOException {
        final ClassPool classPool = new ClassPool(true);
        if (classLoader == null) {
            classPool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
        } else {
            classPool.appendClassPath(new LoaderClassPath(classLoader));
        }

        final CtClass clazz = classPool.makeClass(new ByteArrayInputStream(classFileBuffer), false);
        clazz.defrost();
        return clazz;
    }
}
