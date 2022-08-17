package com.alibaba.ttl3.threadpool.agent.transformlet;

import com.alibaba.ttl3.threadpool.agent.TtlTransformer;
import edu.umd.cs.findbugs.annotations.NonNull;
import javassist.CannotCompileException;
import javassist.NotFoundException;

import java.io.IOException;

/**
 * TTL {@code Transformlet}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public interface TtlTransformlet {
    /**
     * info about class loader: may be <code>null</code> if the bootstrap loader.
     * <p>
     * more info see {@link java.lang.instrument.ClassFileTransformer#transform(ClassLoader, String, Class, java.security.ProtectionDomain, byte[])}
     *
     * @see TtlTransformer#transform(ClassLoader, String, Class, java.security.ProtectionDomain, byte[])
     * @see java.lang.instrument.ClassFileTransformer#transform
     */
    void doTransform(@NonNull ClassInfo classInfo) throws CannotCompileException, NotFoundException, IOException;
}
