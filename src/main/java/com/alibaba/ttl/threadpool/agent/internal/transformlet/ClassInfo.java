package com.alibaba.ttl.threadpool.agent.internal.transformlet;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 2.11.0
 */
public class ClassInfo {
    private final String className;
    private final byte[] classFileBuffer;
    private final ClassLoader loader;

    public ClassInfo(@Nonnull String className, @Nonnull byte[] classFileBuffer, @Nullable ClassLoader loader) {
        this.className = className;
        this.classFileBuffer = classFileBuffer;
        this.loader = loader;
    }

    @Nonnull
    public String getClassName() {
        return className;
    }

    private CtClass ctClass;

    @Nonnull
    public CtClass getCtClass() throws IOException {
        if (ctClass != null) return ctClass;

        final ClassPool classPool = new ClassPool(true);
        if (loader == null) {
            classPool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
        } else {
            classPool.appendClassPath(new LoaderClassPath(loader));
        }

        final CtClass clazz = classPool.makeClass(new ByteArrayInputStream(classFileBuffer), false);
        clazz.defrost();

        this.ctClass = clazz;
        return clazz;
    }

    private boolean modified = false;

    public boolean isModified() {
        return modified;
    }

    public void setModified() {
        this.modified = true;
    }
}
