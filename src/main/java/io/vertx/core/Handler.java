package io.vertx.core;

/**
 * @author: tk
 * @since: 2021/1/19
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
