package com.alibaba.ttl.threadpool.agent;

import java.io.IOException;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;

public interface JavassistDynamicMutipleClassTransformlet extends Transformlet {

    boolean needTransform(CtClass clazz, String className);

    void doTransform(CtClass clazz) throws NotFoundException, CannotCompileException, IOException;
}
