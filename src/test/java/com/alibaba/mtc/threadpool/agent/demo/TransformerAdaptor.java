package com.alibaba.mtc.threadpool.agent.demo;

import com.alibaba.mtc.threadpool.agent.MtContextTransformer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * @author ding.lid
 */
public class TransformerAdaptor implements ClassFileTransformer {
    final MtContextTransformer mtContextTransformer = new MtContextTransformer();

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        final byte[] transform = mtContextTransformer.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
        if (transform != null) {
            return transform;
        }

        // Your transform code ...

        return null;
    }
}
