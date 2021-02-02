package com.alibaba.ttl.agent.extension_transformlet.vertx;

import com.alibaba.ttl.TransmittableThreadLocal;
import io.vertx.core.Vertx;
import org.junit.Test;

/**
 * @author: tk (soulmate.tangk at gmail dot com)
 * @date: 2021/2/2
 */
public class VertxTransformletTest {
    @Test
    public void test() {
        TransmittableThreadLocal<String> transmittableThreadLocal = new TransmittableThreadLocal<String>();
        ThreadLocal<String> threadLocal = new ThreadLocal<>();
        InheritableThreadLocal<String> inheritableThreadLocal = new InheritableThreadLocal<String>();

        Vertx vertx = Vertx.vertx();
        vertx.eventBus().consumer("consumer", message -> {
            System.out.println(threadLocal.get() + Thread.currentThread().getName());
            System.out.println(transmittableThreadLocal.get() + Thread.currentThread().getName());
            System.out.println(inheritableThreadLocal.get() + Thread.currentThread().getName());
        });

        transmittableThreadLocal.set("hahahahaha");
        threadLocal.set("lalalalala");
        inheritableThreadLocal.set("gagagagaga");

        vertx.eventBus().request("consumer", "asdfsd");
        vertx.eventBus().request("consumer", "asdfsd");
    }

    @Test
    public void testCallback() {

    }
}
