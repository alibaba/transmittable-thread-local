package io.vertx.core;

/**
 * @author: tk
 * @since: 2021/1/19
 * 这里是为了解决原io.vertx.core.Handler由AppClassLoader加载，
 * 而TtlVertxHandler由BootstrapClassLoader加载，
 * 导致其对TtlVertxHandler不可见的问题
 */
@FunctionalInterface
public interface Handler<E> {

    /**
     * Something has happened, so handle it.
     *
     * @param event the event to handle
     */
    void handle(E event);
}
