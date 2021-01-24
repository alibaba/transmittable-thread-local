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
    void doTransform(@NonNull ClassInfo classInfo) throws CannotCompileException, NotFoundException, IOException;
}
