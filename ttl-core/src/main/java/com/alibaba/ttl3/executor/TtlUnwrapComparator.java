package com.alibaba.ttl3.executor;

import com.alibaba.ttl3.TtlWrappers;
import com.alibaba.ttl3.spi.TtlWrapper;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Comparator;

/**
 * @see TtlExecutors#getTtlRunnableUnwrapComparator(Comparator)
 * @see TtlExecutors#isTtlRunnableUnwrapComparator(Comparator)
 * @see TtlExecutors#unwrapComparator(Comparator)
 */
final class TtlUnwrapComparator<T> implements Comparator<T>, TtlWrapper<Comparator<T>> {
    private final Comparator<T> comparator;

    public TtlUnwrapComparator(@NonNull Comparator<T> comparator) {
        this.comparator = comparator;
    }

    @Override
    public int compare(T o1, T o2) {
        return comparator.compare(TtlWrappers.unwrap(o1), TtlWrappers.unwrap(o2));
    }

    @NonNull
    @Override
    public Comparator<T> unwrap() {
        return comparator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TtlUnwrapComparator<?> that = (TtlUnwrapComparator<?>) o;

        return comparator.equals(that.comparator);
    }

    @Override
    public int hashCode() {
        return comparator.hashCode();
    }

    @Override
    public String toString() {
        return "TtlUnwrapComparator{comparator=" + comparator + '}';
    }
}
