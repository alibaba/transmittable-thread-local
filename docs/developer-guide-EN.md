<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [Capture, replay and restore](#capture-replay-and-restore)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# Capture, replay and restore

By using [`com.alibaba.ttl.TransmittableThreadLocal.Transmitter`](src/main/java/com/alibaba/ttl/TransmittableThreadLocal.java)to capture all `TransmittableThreadLocal` of current thread and replay them in any other thread，there are following methods：

- `capture`: captures all `TransmittableThreadLocal` values in current thread
- `replay`: replays the captured TTL values after backup the current ones
- `restore`: restores current thread to state before replay

Sample code：

```java
ExecutorService executorService = ...

TransmittableThreadLocal<String> parent = new TransmittableThreadLocal<String>();
parent.set("value-set-in-parent");

//capture in current thread
final Object captured = TransmittableThreadLocal.Transmitter.capture();
executorService.submit(() -> {
    final Object backup = TransmittableThreadLocal.Transmitter.replay(captured);
    try {
        // Your biz code, you can get the TransmittableThreadLocal value from here
        String value = parent.get();
    } finally {
        TransmittableThreadLocal.Transmitter.restore(backup);
    }
});
```