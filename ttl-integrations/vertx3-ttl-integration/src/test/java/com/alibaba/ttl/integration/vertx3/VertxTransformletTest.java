package com.alibaba.ttl.integration.vertx4;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.threadpool.agent.TtlAgent;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author tk (soulmate.tangk at gmail dot com)
 */
public class VertxTransformletTest {
    @Test
    public void testTransmitThreadLocal_InEventbus() throws Exception {
        final TransmittableThreadLocal<String> transmittableThreadLocal = new TransmittableThreadLocal<>();
        final InheritableThreadLocal<String> inheritableThreadLocal = new InheritableThreadLocal<>();

        final String transmittedData = "transmitted_data";
        final String inheritedData = "inherited_data_ttl";
        final String message = "message_42";

        final Vertx vertx = Vertx.vertx();
        final String address = "consumer";

        vertx.eventBus().consumer(address, msg -> {
            // be executed in netty event loop thread
            System.out.println("========================================");
            assertEquals(message, msg.body());

            if (TtlAgent.isTtlAgentLoaded()) {
                System.out.println("Test **WITH** TTL Agent");
                assertEquals(transmittedData, transmittableThreadLocal.get());
            } else {
                System.out.println("Test WITHOUT TTL Agent");
                assertNull(transmittableThreadLocal.get());
            }

            // InheritableThreadLocal is always null
            assertNull(inheritableThreadLocal.get());

            System.out.println("========================================");

            msg.reply(message);
        });

        transmittableThreadLocal.set(transmittedData);
        inheritableThreadLocal.set(inheritedData);

        // delivery message
        final DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.setSendTimeout(1000);
        vertx.eventBus().send(address, message, deliveryOptions, event -> {
            System.out.println("receive reply message:" + event.result().body());
        });
    }

    @Test
    public void testCallbackInHttpClient() throws Exception {
        final TransmittableThreadLocal<String> transmittableThreadLocal = new TransmittableThreadLocal<>();
        final InheritableThreadLocal<String> inheritableThreadLocal = new InheritableThreadLocal<>();
        final String transmittedData = "transmitted_data_ttl";
        final String inheritedData = "inherited_data_ttl";

        final Vertx vertx = Vertx.vertx();

        //set value after eventLoop thread was created
        transmittableThreadLocal.set(transmittedData);
        inheritableThreadLocal.set(inheritedData);

        vertx.createHttpClient().get(80, "baidu.com", "/", event -> {
            System.out.println("receive msg from baidu:" + event.statusCode());
            System.out.println("===================callback=====================");
            event.bodyHandler(body -> {
                System.out.println("receive response body from www.baidu.com" + body.toString("utf8"));
            });

            if (TtlAgent.isTtlAgentLoaded()) {
                System.out.println("Test **WITH** TTL Agent");
                assertEquals(transmittedData, transmittableThreadLocal.get());
                assertNull(inheritableThreadLocal.get());
            } else {
                System.out.println("Test WITHOUT TTL Agent");
                assertNull(transmittableThreadLocal.get());
                assertNull(inheritableThreadLocal.get());
            }

            System.out.println("===================callback=====================");
        }).end();

        //wait for callback
        Thread.sleep(2000);
    }

    @Test
    public void testCallbackInVertxImpl() throws Exception {
        final TransmittableThreadLocal<String> transmittableThreadLocal = new TransmittableThreadLocal<>();
        final InheritableThreadLocal<String> inheritableThreadLocal = new InheritableThreadLocal<>();
        final String transmittedData = "transmitted_data_ttl";
        final String inheritedData = "inherited_data_ttl";
        final String messageReplyInBlockingCode = "messageReplyInBlockingCode___";

        final Vertx vertx = Vertx.vertx();

        //set value after eventLoop thread was created
        transmittableThreadLocal.set(transmittedData);
        inheritableThreadLocal.set(inheritedData);

        vertx.executeBlocking(a -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            a.complete(messageReplyInBlockingCode);
        }, event -> {
            System.out.println("receive msg from blocking code:" + event.result());
            System.out.println("===================callback=====================");

            if (TtlAgent.isTtlAgentLoaded()) {
                System.out.println("Test **WITH** TTL Agent");
                assertEquals(transmittedData, transmittableThreadLocal.get());
            } else {
                System.out.println("Test WITHOUT TTL Agent");
                assertNull(transmittableThreadLocal.get());
            }
            assertNull(inheritableThreadLocal.get());

            System.out.println("===================callback=====================");
        });

        //wait for callback
        Thread.sleep(2000);
    }
}
