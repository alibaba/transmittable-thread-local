package com.alibaba.ttl.threadpool.agent.transformlet;

import edu.umd.cs.findbugs.annotations.NonNull;
import javassist.CannotCompileException;
import javassist.NotFoundException;

import java.io.IOException;

/**
 * TTL {@code Transformlet}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 2.13.0
 */
public interface TtlTransformlet {
    /**
     * info about class loader: may be <code>null</code> if the bootstrap loader.
     * <p>
     * more info see {@link java.lang.instrument.ClassFileTransformer#transform(java.lang.ClassLoader, java.lang.String, java.lang.Class, java.security.ProtectionDomain, byte[])}
     *
     * @see com.alibaba.ttl.threadpool.agent.TtlTransformer#transform(ClassLoader, String, Class, java.security.ProtectionDomain, byte[])
     * @see java.lang.instrument.ClassFileTransformer#transform
     */
    void doTransform(@NonNull ClassInfo classInfo) throws CannotCompileException, NotFoundException, IOException;
}
