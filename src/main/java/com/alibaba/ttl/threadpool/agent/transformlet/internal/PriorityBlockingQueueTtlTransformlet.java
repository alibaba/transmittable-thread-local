package com.alibaba.ttl.threadpool.agent.transformlet.internal;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.alibaba.ttl.threadpool.agent.logging.Logger;
import com.alibaba.ttl.threadpool.agent.transformlet.ClassInfo;
import com.alibaba.ttl.threadpool.agent.transformlet.TtlTransformlet;
import edu.umd.cs.findbugs.annotations.NonNull;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;

import java.io.IOException;
import java.util.Comparator;

import static com.alibaba.ttl.threadpool.agent.transformlet.helper.TtlTransformletHelper.signatureOfMethod;

/**
 * TTL {@link TtlTransformlet} for {@link java.util.concurrent.PriorityBlockingQueue PriorityBlockingQueue}.
 * <p>
 * Avoid {@code ClassCastException(TtlRunnable cannot be cast to Comparable)} problem
 * for combination usage:
 * <ul>
 * <li>use {@link java.util.concurrent.PriorityBlockingQueue PriorityBlockingQueue} for {@link java.util.concurrent.ThreadPoolExecutor ThreadPoolExecutor}</li>
 * <li>use {@code TTL Agent} {@link JdkExecutorTtlTransformlet}</li>
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
 * @see JdkExecutorTtlTransformlet
 * @since 2.12.3
 */
public class PriorityBlockingQueueTtlTransformlet implements TtlTransformlet {
    private static final Logger logger = Logger.getLogger(PriorityBlockingQueueTtlTransformlet.class);

    private static final String PRIORITY_BLOCKING_QUEUE_CLASS_NAME = "java.util.concurrent.PriorityBlockingQueue";
    private static final String PRIORITY_QUEUE_CLASS_NAME = "java.util.PriorityQueue";
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
     * wrap comparator field in constructors
     */
    private static final String CODE = "this." + COMPARATOR_FIELD_NAME + " = "
            + PriorityBlockingQueueTtlTransformlet.class.getName() +
            ".overwriteComparatorField$by$ttl(this." + COMPARATOR_FIELD_NAME + ");";

    /**
     * @see #overwriteComparatorField$by$ttl(Comparator)
     */
    private static void modifyConstructors(@NonNull CtClass clazz) throws NotFoundException, CannotCompileException {
        for (CtConstructor constructor : clazz.getDeclaredConstructors()) {
            logger.info("insert code after constructor " + signatureOfMethod(constructor) + " of class " +
                    constructor.getDeclaringClass().getName() + ": " + CODE);

            constructor.insertAfter(CODE);
        }
    }

    /**
     * @see TtlExecutors#getTtlRunnableUnwrapComparator(Comparator)
     */
    public static Comparator<Runnable> overwriteComparatorField$by$ttl(Comparator<Runnable> comparator) {
        if (comparator == null) return TtlExecutors.getTtlRunnableUnwrapComparatorForComparableRunnable();

        return TtlExecutors.getTtlRunnableUnwrapComparator(comparator);
    }
}
