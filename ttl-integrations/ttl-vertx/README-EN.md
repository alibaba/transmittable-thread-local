#  Vertx 4 integration of TTL

## 1. callback in vert.x framework

## 1.1 Decorate `io.vertx.core.Handler`

Use [`TtlVertxHandler`](src/main/java/com/alibaba/ttl/TtlVertxHandler.java) to decorate `Handler`。
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

## 1.2 decorate `io.vertx.core.Future`

At present, `TTL` agent has decorated below `Vertx` callback components(`io.vertx.core.Future`) implementation:

- `io.vertx.core.Future`
    - decoration implementation code is in [`TtlVertxFutureTransformlet.java`](src/main/java/com/alibaba/ttl/threadpool/agent/internal/transformlet/impl/TtlVertxFutureTransformlet.java)。
