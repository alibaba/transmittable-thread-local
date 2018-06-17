package com.alibaba.ttl.threadpool.agent;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;

import java.io.IOException;

/**
 * TTL {@code Transformlet} by {@code Javassist}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 2.5.1
 */
public interface JavassistTransformlet {
    boolean needTransform(String className);

    void doTransform(CtClass clazz) throws NotFoundException, CannotCompileException, IOException;
}
