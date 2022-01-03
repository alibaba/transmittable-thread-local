package com.alibaba.demo.ttl;

import com.alibaba.ttl.TtlWrappers;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
//import java.util.stream.Collectors;

/**
 * @author huangfei1101 (fei.hf at alibaba-inc dot com)
 * @date 2021/12/30
 */
public class TtlWrapperTypeInferenceProblemShowcase {
    @Test
    public void wrapFunction() {
        List<Integer> source = buildSourceList(10);

        /*
         * Try to call the function foo for each element in the list
         * Because it is compatible with Java 6, the following code is commented
         * The following line of code has a compilation error
         */
        //List<Integer> targetWithCompileError = source.parallelStream().map(TtlWrappers.wrap(i->foo(i))).collect(Collectors.toList());

        /*
         * The following line of code is correct
         */
        //List<Integer> target = source.parallelStream().map(TtlWrappers.wrapFunction(i->foo(i))).collect(Collectors.toList());
    }

    private int foo(int i) {
        return i*10;
    }

    private List<Integer> buildSourceList(int length) {
        List<Integer> l = new ArrayList<Integer>();
        for (int i = 0; i < length; ++i) {
            l.add(i);
        }
        return l;
    }
}
