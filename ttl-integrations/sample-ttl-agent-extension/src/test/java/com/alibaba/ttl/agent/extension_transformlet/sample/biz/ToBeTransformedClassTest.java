package com.alibaba.ttl.agent.extension_transformlet.sample.biz;

import com.alibaba.ttl.threadpool.agent.TtlAgent;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ToBeTransformedClassTest {
    @Test
    public void test_method1() {
        final ToBeTransformedClass instance = new ToBeTransformedClass();

        System.out.println("========================================");
        if (TtlAgent.isTtlAgentLoaded()) {
            System.out.println("Test WITH TTL Agent");
            assertEquals(42, instance.toBeTransformedMethod(21));
        } else {
            System.out.println("Test Without TTL Agent");
            assertEquals(21, instance.toBeTransformedMethod(21));
        }
        System.out.println("========================================");
    }
}
