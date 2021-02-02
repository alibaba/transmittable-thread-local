#  Vertx 4 integration of TTL

## 1. assure TTL context transmit in callback 

## 1.1 Decorate `io.vertx.core.Handler`

Use [`TtlVertxHandler`](src/main/java/com/alibaba/ttl/agent/extension_transformlet/vertx/TtlVertxHandler.java) to decorate `Handler`。

## 1.2 Decorate `io.vertx.core.Future`

At present, `TTL` agent has decorated below `Vertx` callback components(`io.vertx.core.Future`) implementation:

- `io.vertx.core.Future`
- `io.vertx.core.impl.future.FutureImpl`
- decoration implementation code is in [`VertxFutureTtlTransformlet.java`](src/main/java/com/alibaba/ttl/agent/extension_transformlet/vertx/transformlet/VertxFutureTtlTransformlet.java)。

Sample code：

```java
Vertx vertx = Vertx.vertx();

//build channel
ManagedChannel channel = VertxChannelBuilder
  .forAddress(vertx, "localhost", 8080)
  .usePlaintext()
  .build();

// set in parent thread
TransmittableThreadLocal<String> context = new TransmittableThreadLocal<>();
context.set("value-set-in-parent");

//init stub
io.grpc.stub.XXX stub = XXX.newVertxStub(channel);
HelloRequest request = HelloRequest.newBuilder().setName("Julien").build();

//init handler
Handler<AsyncResult<String>> handler = event -> {
  // read in callback, value is "value-set-in-parent"
  context.get();
  if (event.succeeded()) {
    //do something
  } else {
    // find exception
  }
};
// extra work, create decorated TtlVertxHandler object
TtlVertxHandler<AsyncResult<String>> ttlVertxHandler = TtlVertxHandler.get(handler);

//send request
stub.sayHello(request).onComplete(ttlVertxHandler);
```
## 2. assure TTL context transmit in eventbus

### 2. decorate`java.lang.Runnable`
Use [`TtlRunnable`](../../src/main/java/com/alibaba/ttl/TtlRunnable.java) to decorate`Runnable`。

### 2.2 Decorate`io.netty.util.concurrent.SingleThreadEventExecutor`

- decoration implementation code is in[`NettySingleThreadEventExecutorTtlTransformlet.java`](src/main/java/com/alibaba/ttl/agent/extension_transformlet/vertx/transformlet/NettySingleThreadEventExecutorTtlTransformlet.java)。

