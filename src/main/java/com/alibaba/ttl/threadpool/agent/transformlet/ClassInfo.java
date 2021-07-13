package com.alibaba.ttl.threadpool.agent.transformlet;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;

import static com.alibaba.ttl.threadpool.agent.transformlet.helper.TtlTransformletHelper.getLocationUrlOfClass;

/**
 * Class Info for {@link TtlTransformlet}.
 *
 * <B><I>Caution:</I></B><br>
 * Do <b>NOT</b> load {@link Class} which is transforming, or the transform will lose effectiveness.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 2.13.0
 */
public class ClassInfo {
    private final String transformerClassFile;
    private final String className;
    private final byte[] classFileBuffer;
    private final ClassLoader loader;

    // SuppressFBWarnings for classFileBuffer/loader parameter:
    //   [ERROR] new com.alibaba.ttl.threadpool.agent.transformlet.ClassInfo(String, byte[], ClassLoader)
    //   may expose internal representation by storing an externally mutable object
    //   into ClassInfo.classFileBuffer/loader
    public ClassInfo(@NonNull String transformerClassFile,
                     @NonNull @SuppressFBWarnings({"EI_EXPOSE_REP2"}) byte[] classFileBuffer,
                     @Nullable @SuppressFBWarnings({"EI_EXPOSE_REP2"}) ClassLoader loader) {
        this.transformerClassFile = transformerClassFile;
        this.className = toClassName(transformerClassFile);
        this.classFileBuffer = classFileBuffer;
        this.loader = loader;
    }

    @NonNull
    public String getClassName() {
        return className;
    }

    private CtClass ctClass;

    public URL getLocationUrl() throws IOException {
        return getLocationUrlOfClass(getCtClass());
    }

    @NonNull
    @SuppressFBWarnings({"EI_EXPOSE_REP"})
    // [ERROR] Medium: com.alibaba.ttl.threadpool.agent.transformlet.ClassInfo.getCtClass()
    // may expose internal representation
    // by returning ClassInfo.ctClass [com.alibaba.ttl.threadpool.agent.transformlet.ClassInfo]
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

    @SuppressFBWarnings({"EI_EXPOSE_REP"})
    // [ERROR] Medium: com.alibaba.ttl.threadpool.agent.transformlet.ClassInfo.getClassLoader()
    // may expose internal representation
    // by returning ClassInfo.loader [com.alibaba.ttl.threadpool.agent.transformlet.ClassInfo]
    public ClassLoader getClassLoader() {
        return loader;
    }

    private static String toClassName(@NonNull final String classFile) {
        return classFile.replace('/', '.');
    }
}
