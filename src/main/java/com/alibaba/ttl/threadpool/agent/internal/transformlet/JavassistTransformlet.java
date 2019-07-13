package com.alibaba.ttl.threadpool.agent.internal.transformlet;

import edu.umd.cs.findbugs.annotations.NonNull;
import javassist.CannotCompileException;
import javassist.NotFoundException;

import java.io.IOException;

/**
 * TTL {@code Transformlet} by {@code Javassist}.
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 2.5.1
 */
public interface JavassistTransformlet {
    void doTransform(@NonNull ClassInfo classInfo) throws IOException, NotFoundException, CannotCompileException;
}
