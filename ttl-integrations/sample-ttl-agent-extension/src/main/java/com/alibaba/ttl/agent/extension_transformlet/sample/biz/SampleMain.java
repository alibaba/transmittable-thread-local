package com.alibaba.ttl.agent.extension_transformlet.sample.biz;

import com.alibaba.ttl.threadpool.agent.TtlAgent;
import com.alibaba.ttl.threadpool.agent.transformlet.ClassInfo;

public class SampleMain {
    /**
     * @see ToBeTransformedClass#toBeTransformedMethod(int)
     * @see com.alibaba.ttl.agent.extension_transformlet.sample.transformlet.SampleExtensionTransformlet#doTransform(ClassInfo)
     */
    public static void main(String[] args) throws Exception {
        final ToBeTransformedClass instance = new ToBeTransformedClass();

        System.out.println("========================================");
        if (TtlAgent.isTtlAgentLoaded()) {
            System.out.println("Run WITH TTL Agent");
        } else {
            System.out.println("Run Without TTL Agent");
        }
        System.out.println(instance.toBeTransformedMethod(21));
        System.out.println("========================================");
    }
}
