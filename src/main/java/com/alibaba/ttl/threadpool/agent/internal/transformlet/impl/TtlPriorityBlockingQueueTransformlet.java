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

/**
 * TTL {@link JavassistTransformlet} for {@link java.util.concurrent.PriorityBlockingQueue PriorityBlockingQueue}.
 * <p>
 * Avoid {@code ClassCastException(TtlRunnable cannot be cast to Comparable)} problem
 * for combination usage:
 * <ul>
 * <li>use {@link java.util.concurrent.PriorityBlockingQueue PriorityBlockingQueue} for {@link java.util.concurrent.ThreadPoolExecutor ThreadPoolExecutor}</li>
 * <li>use {@code TTL Agent} {@link TtlExecutorTransformlet}</li>
 * </ul>
 * More info see <a href="https://github.com/alibaba/transmittable-thread-local/issues/330">issue #330</a>
 *
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @see TtlExecutors#getTtlRunnableUnwrapComparator(Comparator)
 * @see TtlExecutors#getTtlRunnableUnwrapComparatorForComparableRunnable()
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see java.util.concurrent.ThreadPoolExecutor#ThreadPoolExecutor(int, int, long, java.util.concurrent.TimeUnit, java.util.concurrent.BlockingQueue)
 * @see java.util.concurrent.PriorityBlockingQueue
 * @see java.util.concurrent.PriorityBlockingQueue#PriorityBlockingQueue(int, Comparator)
 * @see java.util.PriorityQueue
 * @see java.util.PriorityQueue#PriorityQueue(int, Comparator)
 * @see TtlExecutorTransformlet
 * @since 2.12.3
 */
public class TtlPriorityBlockingQueueTransformlet implements JavassistTransformlet {
    private static final Logger logger = Logger.getLogger(TtlPriorityBlockingQueueTransformlet.class);

    private static final String PRIORITY_BLOCKING_QUEUE_CLASS_NAME = "java.util.concurrent.PriorityBlockingQueue";
    private static final String PRIORITY_QUEUE_CLASS_NAME = "java.util.PriorityQueue";
    private static final String COMPARATOR_CLASS_NAME = "java.util.Comparator";
    private static final String COMPARATOR_FIELD_NAME = "comparator";

    @Override
    public void doTransform(@NonNull ClassInfo classInfo) throws IOException, CannotCompileException, NotFoundException {
        final String className = classInfo.getClassName();

        if (PRIORITY_BLOCKING_QUEUE_CLASS_NAME.equals(className)) {
            updatePriorityBlockingQueueClass(classInfo.getCtClass());
            classInfo.setModified();
        }

        if (PRIORITY_QUEUE_CLASS_NAME.equals(className)) {
            updateBlockingQueueClass(classInfo.getCtClass());
            classInfo.setModified();
        }
    }

    private void updatePriorityBlockingQueueClass(@NonNull final CtClass clazz) throws CannotCompileException, NotFoundException {
        if (!haveComparatorField(clazz)) {
            // In Java 6, PriorityBlockingQueue implementation do not have field comparator,
            // need transform more fundamental class PriorityQueue
            logger.info(PRIORITY_BLOCKING_QUEUE_CLASS_NAME + " do not have field " + COMPARATOR_FIELD_NAME +
                ", transform " + PRIORITY_QUEUE_CLASS_NAME + " instead.");
            return;
        }

        modifyConstructors(clazz);
    }

    private void updateBlockingQueueClass(@NonNull final CtClass clazz) throws CannotCompileException, NotFoundException {
        final CtClass classPriorityBlockingQueue = clazz.getClassPool().getCtClass(PRIORITY_BLOCKING_QUEUE_CLASS_NAME);
        if (haveComparatorField(classPriorityBlockingQueue)) return;

        logger.info(PRIORITY_BLOCKING_QUEUE_CLASS_NAME + " do not have field " + COMPARATOR_FIELD_NAME +
            ", so need transform " + PRIORITY_QUEUE_CLASS_NAME);
        modifyConstructors(clazz);
    }

    private static boolean haveComparatorField(CtClass clazz) {
        try {
            clazz.getDeclaredField(COMPARATOR_FIELD_NAME);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    /**
     * @see #wrapComparator$by$ttl(Comparator)
     */
    private static final String WRAP_METHOD_NAME = "wrapComparator$by$ttl";

    /**
     * wrap comparator field in constructors
     *
     * @see #COMPARATOR_FIELD_NAME
     */
    private static final String AFTER_CODE_REWRITE_FILED = String.format("this.%s = %s.%s(this.%1$s);",
        COMPARATOR_FIELD_NAME, TtlPriorityBlockingQueueTransformlet.class.getName(), WRAP_METHOD_NAME
    );

    private static void modifyConstructors(@NonNull CtClass clazz) throws NotFoundException, CannotCompileException {
        for (CtConstructor constructor : clazz.getDeclaredConstructors()) {
            final CtClass[] parameterTypes = constructor.getParameterTypes();
            final StringBuilder beforeCode = new StringBuilder();
            for (int i = 0; i < parameterTypes.length; i++) {
                ///////////////////////////////////////////////////////////////
                // rewrite Comparator constructor parameter
                ///////////////////////////////////////////////////////////////
                final String paramTypeName = parameterTypes[i].getName();
                if (COMPARATOR_CLASS_NAME.equals(paramTypeName)) {
                    String code = String.format("$%d = %s.%s($%1$d);",
                        i + 1, TtlPriorityBlockingQueueTransformlet.class.getName(), WRAP_METHOD_NAME
                    );
                    beforeCode.append(code);
                }
            }
            if (beforeCode.length() > 0) {
                logger.info("insert code before constructor " + signatureOfMethod(constructor) + " of class " +
                    constructor.getDeclaringClass().getName() + ": " + beforeCode);
                constructor.insertBefore(beforeCode.toString());
            }

            ///////////////////////////////////////////////////////////////
            // rewrite Comparator class field
            ///////////////////////////////////////////////////////////////
            logger.info("insert code after constructor " + signatureOfMethod(constructor) + " of class " +
                constructor.getDeclaringClass().getName() + ": " + AFTER_CODE_REWRITE_FILED);
            constructor.insertAfter(AFTER_CODE_REWRITE_FILED);
        }
    }

    /**
     * @see TtlExecutors#getTtlRunnableUnwrapComparatorForComparableRunnable()
     * @see TtlExecutors#getTtlRunnableUnwrapComparator(Comparator)
     */
    public static Comparator<Runnable> wrapComparator$by$ttl(Comparator<Runnable> comparator) {
        if (comparator == null) return TtlExecutors.getTtlRunnableUnwrapComparatorForComparableRunnable();

        return TtlExecutors.getTtlRunnableUnwrapComparator(comparator);
    }
}
