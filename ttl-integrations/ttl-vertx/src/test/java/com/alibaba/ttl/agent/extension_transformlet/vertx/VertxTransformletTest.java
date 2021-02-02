package com.alibaba.ttl.agent.extension_transformlet.vertx;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.threadpool.agent.TtlAgent;
import io.vertx.core.Vertx;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author: tk (soulmate.tangk at gmail dot com)
 * @date: 2021/2/2
 */
public class VertxTransformletTest {
    @Test
    public void testTransmitThreadLocalInEventbus() {
        TransmittableThreadLocal<String> transmittableThreadLocal = new TransmittableThreadLocal<String>();
        InheritableThreadLocal<String> inheritableThreadLocal = new InheritableThreadLocal<String>();
        String transmittedData = "hahahahaha";

        Vertx vertx = Vertx.vertx();
        vertx.eventBus().consumer("consumer", message -> {
            //there will be execute in netty event loop thread

            System.out.println("========================================");
            if (TtlAgent.isTtlAgentLoaded()) {
                System.out.println("Test **WITH** TTL Agent");
                Assert.assertEquals(transmittableThreadLocal.get(), transmittedData);
            } else {
                System.out.println("Test WITHOUT TTL Agent");
                Assert.assertNull(transmittableThreadLocal.get());
            }

            //here will be null always
            Assert.assertNull(inheritableThreadLocal.get());

            System.out.println(transmittableThreadLocal.get() + Thread.currentThread().getName());
            System.out.println(inheritableThreadLocal.get() + Thread.currentThread().getName());
            System.out.println("========================================");
        });

        transmittableThreadLocal.set(transmittedData);
        inheritableThreadLocal.set("gagagagaga");

        //delivery message
        vertx.eventBus().request("consumer", "asdfsd");
    }

    @Test
    public void testCallback() {

    }
}
