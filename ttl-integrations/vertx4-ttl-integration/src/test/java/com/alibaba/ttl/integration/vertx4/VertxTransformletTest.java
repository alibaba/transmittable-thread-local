package com.alibaba.ttl.integration.vertx4;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.threadpool.agent.TtlAgent;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.junit.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
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

            //reply message can be get by {messageFuture.toCompletionStage().toCompletableFuture().get().body()} or {listener}
            msg.reply(message);
        });

        transmittableThreadLocal.set(transmittedData);
        inheritableThreadLocal.set(inheritedData);

        // delivery message
        final DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.setSendTimeout(1000);
        final Future<Message<Object>> messageFuture = vertx.eventBus().request(address, message, deliveryOptions);

        messageFuture.toCompletionStage().toCompletableFuture().get();
        assertEquals(message, messageFuture.toCompletionStage().toCompletableFuture().get().body());
    }

    @Test
    public void testCallback() throws Exception {
        final TransmittableThreadLocal<String> transmittableThreadLocal = new TransmittableThreadLocal<>();
        final InheritableThreadLocal<String> inheritableThreadLocal = new InheritableThreadLocal<>();
        final String transmittedData = "transmitted_data_ttl";
        final String inheritedData = "inherited_data_ttl";

        final Vertx vertx = Vertx.vertx();
        //here will bind eventLoop to client and create a new Thread for eventLoop
        final WebClient client = WebClient.create(vertx);

        //set value after eventLoop thread was created
        transmittableThreadLocal.set(transmittedData);
        inheritableThreadLocal.set(inheritedData);

        final Future<HttpResponse<Buffer>> future = client.get(80, "baidu.com", "/")
            .send()
            .onSuccess(response -> {
                System.out.println("===================callback=====================");
                System.out.println(response.body().toString(UTF_8));

                if (TtlAgent.isTtlAgentLoaded()) {
                    System.out.println("Test **WITH** TTL Agent");
                    assertEquals(transmittedData, transmittableThreadLocal.get());
                } else {
                    System.out.println("Test WITHOUT TTL Agent");
                    assertNull(transmittableThreadLocal.get());
                }

                System.out.println("===================callback=====================");
            });

        // block and wait to finish
        future.toCompletionStage().toCompletableFuture().get();
    }
}
