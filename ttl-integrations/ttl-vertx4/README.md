# Vertx 4的TTL集成

## 1. 保证异步io回调中传递值

### 1.1 vert.x内的回调处理

#### 修饰`io.vertx.core.Handler`

使用[`TtlVertxHandler`](src/main/java/com/alibaba/ttl/integration/vertx4/TtlVertxHandler.java)来修饰传入的`Handler`。

示例代码：

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

### 1.2 修饰`io.vertx.core.Future`

修饰了的Vert.x执行器组件如下:
- `io.vertx.core.Future`
    - 修饰实现代码在[`TtlVertxFutureTransformlet.java`](src/main/java/com/alibaba/ttl/threadpool/agent/internal/transformlet/impl/TtlVertxFutureTransformlet.java)。
    
