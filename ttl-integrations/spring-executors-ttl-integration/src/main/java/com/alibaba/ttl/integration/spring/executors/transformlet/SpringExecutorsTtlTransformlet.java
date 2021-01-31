package com.alibaba.ttl.integration.spring.executors.transformlet;

import com.alibaba.ttl.threadpool.agent.transformlet.ClassInfo;
import com.alibaba.ttl.threadpool.agent.transformlet.TtlTransformlet;
import com.alibaba.ttl.threadpool.agent.transformlet.javassist.CannotCompileException;
import com.alibaba.ttl.threadpool.agent.transformlet.javassist.NotFoundException;

import java.io.IOException;

public class SpringExecutorsTtlTransformlet implements TtlTransformlet {
    @Override
    public void doTransform(ClassInfo classInfo) throws CannotCompileException, NotFoundException, IOException {

    }
}
