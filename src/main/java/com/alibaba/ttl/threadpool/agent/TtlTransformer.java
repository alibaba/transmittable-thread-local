package com.alibaba.ttl.threadpool.agent;

import com.alibaba.ttl.threadpool.agent.internal.logging.Logger;
import com.alibaba.ttl.threadpool.agent.internal.transformlet.ClassInfo;
import com.alibaba.ttl.threadpool.agent.internal.transformlet.JavassistTransformlet;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.Utils.isClassOrInnerClass;
import static com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.Utils.isClassUnderPackage;

/**
 * TTL {@link ClassFileTransformer} of Java Agent
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see ClassFileTransformer
 * @see <a href="https://docs.oracle.com/en/java/javase/21/docs/api/java.instrument/java/lang/instrument/package-summary.html">The mechanism for instrumentation</a>
 * @since 0.9.0
 */
public class TtlTransformer implements ClassFileTransformer {
    private static final Logger logger = Logger.getLogger(TtlTransformer.class);

    /**
     * "<code>null</code> if no transform is performed",
     * see {@code @return} of {@link ClassFileTransformer#transform(ClassLoader, String, Class, ProtectionDomain, byte[])}
     */
    @SuppressFBWarnings({"EI_EXPOSE_REP"})
    // [ERROR] com.alibaba.ttl.threadpool.agent.TtlTransformer.transform(ClassLoader, String, Class, ProtectionDomain, byte[])
    //         may expose internal representation by returning TtlTransformer.NO_TRANSFORM
    // the value is null, so there is NO "EI_EXPOSE_REP" problem actually.
    private static final byte[] NO_TRANSFORM = null;

    private final List<JavassistTransformlet> transformletList = new ArrayList<>();

    TtlTransformer(List<? extends JavassistTransformlet> transformletList) {
        for (JavassistTransformlet transformlet : transformletList) {
            this.transformletList.add(transformlet);
            logger.info("[TtlTransformer] add Transformlet " + transformlet.getClass() + " success");
        }
    }

    private static final String[] NO_TRANSFORM_PACKAGES_INCLUDING_SUB_PACKAGE = {
            "com.alibaba.ttl",
            "java.lang",
    };
    private static final String[] NO_TRANSFORM_ENCLOSING_CLASS_NAME = {
            // work-around ClassCircularityError: https://github.com/alibaba/transmittable-thread-local/issues/399
            "java.util.concurrent.ConcurrentHashMap",
            "java.util.concurrent.ConcurrentSkipListMap",
            "java.util.concurrent.ConcurrentSkipListSet",
            "java.util.concurrent.CopyOnWriteArrayList",
            "java.util.concurrent.CopyOnWriteArraySet",
    };

    @Override
    public final byte[] transform(@Nullable final ClassLoader loader, @Nullable final String classFile, final Class<?> classBeingRedefined,
                                  final ProtectionDomain protectionDomain, @NonNull final byte[] classFileBuffer) {
        try {
            // Lambda has no class file, no need to transform, just return.
            if (classFile == null) return NO_TRANSFORM;

            final String className = toClassName(classFile);
            for (String packageName : NO_TRANSFORM_PACKAGES_INCLUDING_SUB_PACKAGE) {
                if (isClassUnderPackage(className, packageName)) return NO_TRANSFORM;
            }
            for (String enclosingClassName : NO_TRANSFORM_ENCLOSING_CLASS_NAME) {
                if (isClassOrInnerClass(className, enclosingClassName)) return NO_TRANSFORM;
            }

            final ClassInfo classInfo = new ClassInfo(className, classFileBuffer, loader);

            for (JavassistTransformlet transformlet : transformletList) {
                transformlet.doTransform(classInfo);
                if (classInfo.isModified()) return classInfo.getCtClass().toBytecode();
            }
        } catch (Throwable t) {
            String msg = "Fail to transform class " + classFile + ", cause: " + t;
            logger.log(Level.SEVERE, msg, t);
            throw new IllegalStateException(msg, t);
        }

        return NO_TRANSFORM;
    }

    private static String toClassName(@NonNull final String classFile) {
        return classFile.replace('/', '.');
    }
}
