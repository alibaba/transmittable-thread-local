package com.alibaba.ttl.threadpool;

import com.alibaba.ttl.TtlRunnable;
import com.alibaba.ttl.spi.TtlWrapper;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Comparator;

/**
 * @see TtlExecutors#getTtlRunnableUnwrapComparator(Comparator)
 * @see TtlExecutors#isTtlRunnableUnwrapComparator(Comparator)
 * @see TtlExecutors#unwrap(Comparator)
 * @since 2.12.3
 */
final class TtlRunnableUnwrapComparator implements Comparator<Runnable>, TtlWrapper<Comparator<Runnable>> {
    private final Comparator<Runnable> comparator;

    public TtlRunnableUnwrapComparator(@NonNull Comparator<Runnable> comparator) {
        this.comparator = comparator;
    }

    @Override
    public int compare(Runnable o1, Runnable o2) {
        return comparator.compare(TtlRunnable.unwrap(o1), TtlRunnable.unwrap(o2));
    }

    @NonNull
    @Override
    public Comparator<Runnable> unwrap() {
        return comparator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TtlRunnableUnwrapComparator that = (TtlRunnableUnwrapComparator) o;

        return comparator.equals(that.comparator);
    }

    @Override
    public int hashCode() {
        return comparator.hashCode();
    }

    @Override
    public String toString() {
        return "TtlRunnableUnwrapComparator{comparator=" + comparator + '}';
    }
}
