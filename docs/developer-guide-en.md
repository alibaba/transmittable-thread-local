# 🎓 Developer Guide

---------------------------

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->


- [📌 Framework/Middleware integration to `TTL` transmittance](#-frameworkmiddleware-integration-to-ttl-transmittance)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

---------------------------

# 📌 Framework/Middleware integration to `TTL` transmittance

[`TransmittableThreadLocal.Transmitter`](../src/main/java/com/alibaba/ttl/TransmittableThreadLocal.java#L201) to capture all `TTL` values of current thread and replay them in other thread.

There are following methods：

- `capture`: capture all `TTL` values in current thread
- `replay`: replay the captured `TTL` values in current thread, and return the backup `TTL` values before replay
- `restore`: restore `TTL` values before replay

Sample code：

```java
// ===========================================================================
// Thread A
// ===========================================================================

TransmittableThreadLocal<String> parent = new TransmittableThreadLocal<String>();
parent.set("value-set-in-parent");

// 1. capture all TTL values in current thread
final Object captured = TransmittableThreadLocal.Transmitter.capture();

// ===========================================================================
// Thread B
// ===========================================================================

// 2. replay the captured TTL values in current thread, and return the backup TTL values before replay
final Object backup = TransmittableThreadLocal.Transmitter.replay(captured);
try {
    // Your biz code, you can get the TTL value from here
    String value = parent.get();
    ...
} finally {
    // 3. restore TTL values before replay
    TransmittableThreadLocal.Transmitter.restore(backup);
}
```

For more actual implementation code of `TTL` transmittance, see [`TtlRunnable.java`](../src/main/java/com/alibaba/ttl/TtlRunnable.java#L43) and [`TtlCallable.java`](../src/main/java/com/alibaba/ttl/TtlCallable.java#L46)。