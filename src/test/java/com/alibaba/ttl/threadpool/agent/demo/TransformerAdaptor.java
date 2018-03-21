package com.alibaba.ttl.threadpool.agent.demo;

import com.alibaba.ttl.threadpool.agent.TtlTransformer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 */
public class TransformerAdaptor implements ClassFileTransformer {
    private final TtlTransformer ttlTransformer = new TtlTransformer();

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        final byte[] transform = ttlTransformer.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
        if (transform != null) {
            return transform;
        }

        // Your transform code ...

        return null;
    }
}
