package com.alibaba.ttl.threadpool.agent.internal.transformlet.impl;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.alibaba.ttl.threadpool.agent.internal.logging.Logger;
import com.alibaba.ttl.threadpool.agent.internal.transformlet.ClassInfo;
import com.alibaba.ttl.threadpool.agent.internal.transformlet.JavassistTransformlet;
import edu.umd.cs.findbugs.annotations.NonNull;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;

import java.io.IOException;
import java.util.Comparator;

import static com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.Utils.signatureOfMethod;

public class TtlPriorityBlockingQueueTransformlet implements JavassistTransformlet {
    private static final Logger logger = Logger.getLogger(TtlPriorityBlockingQueueTransformlet.class);

    private static final String PRIORITY_BLOCKING_QUEUE_CLASS_NAME = "java.util.concurrent.PriorityBlockingQueue";

    @Override
    public void doTransform(@NonNull ClassInfo classInfo) throws IOException, CannotCompileException, NotFoundException {
        if (!PRIORITY_BLOCKING_QUEUE_CLASS_NAME.equals(classInfo.getClassName())) return;

        updatePriorityBlockingQueueClass(classInfo.getCtClass());
        classInfo.setModified();
    }

    /**
     * @see #rewriteComparator$by$ttl(Comparator)
     */
    private void updatePriorityBlockingQueueClass(@NonNull final CtClass clazz) throws CannotCompileException, NotFoundException {
        // wrap comparator field
        final String code = "this.comparator = com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.TtlPriorityBlockingQueueTransformlet.rewriteComparator$by$ttl(this.comparator);";

        for (CtConstructor constructor : clazz.getDeclaredConstructors()) {
            logger.info("insert code after constructor " + signatureOfMethod(constructor) + " of class " + constructor.getDeclaringClass().getName() + ": " + code);
            constructor.insertAfter(code);
        }
    }

    /**
     * @see TtlExecutors#getTtlRunnableUnwrapComparator(Comparator)
     */
    public static Comparator<Runnable> rewriteComparator$by$ttl(Comparator<Runnable> comparator) {
        if (comparator == null) return TtlExecutors.getTtlRunnableUnwrapComparatorForComparableRunnable();

        return TtlExecutors.getTtlRunnableUnwrapComparator(comparator);
    }
}
