package com.alibaba.ttl.agent.extension_transformlet.vertx;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.threadpool.agent.TtlAgent;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
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
                Assert.assertEquals(transmittedData, transmittableThreadLocal.get());
            } else {
                System.out.println("Test WITHOUT TTL Agent");
                Assert.assertNull(transmittableThreadLocal.get());
            }

            //here will be null always
            Assert.assertNull(inheritableThreadLocal.get());

            System.out.println("========================================");
        });

        transmittableThreadLocal.set(transmittedData);
        inheritableThreadLocal.set("gagagagaga");

        //delivery message
        vertx.eventBus().request("consumer", "asdfsd");
    }

    /**
     * @see
     * @throws InterruptedException
     */
    @Test
    public void testCallback() throws InterruptedException {
        TransmittableThreadLocal<String> transmittableThreadLocal = new TransmittableThreadLocal<String>();
        InheritableThreadLocal<String> inheritableThreadLocal = new InheritableThreadLocal<String>();
        String transmittedData = "hahahahaha";

        Vertx vertx = Vertx.vertx();
        //here will bind eventLoop to client and create a new Thread for eventLoop
        WebClient client = WebClient.create(vertx);

        //set value after eventLoop thread was created
        transmittableThreadLocal.set(transmittedData);
        inheritableThreadLocal.set("gagagagaga");

        client
            .get(80, "baidu.com", "/")
            .send()
            .onSuccess(response -> {
                System.out.println("===================callback=====================");

                if (TtlAgent.isTtlAgentLoaded()) {
                    System.out.println("Test **WITH** TTL Agent");
                    Assert.assertEquals(transmittedData, transmittableThreadLocal.get());
                } else {
                    System.out.println("Test WITHOUT TTL Agent");
                    Assert.assertNull(transmittableThreadLocal.get());
                }

                System.out.println("===================callback=====================");
            });

        Thread.sleep(10000);
    }
}
