# 🎓 Developer Guide

---------------------------

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->


- [📌 Framework/Middleware integration to `TTL` transmittance](#-frameworkmiddleware-integration-to-ttl-transmittance)
- [📚 Related material](#-related-material)
    - [`JDK` core classes](#jdk-core-classes)
    - [`Java` Agent](#java-agent)
    - [`Javassist`](#javassist)
    - [`Maven Shade plugin`](#maven-shade-plugin)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

---------------------------

# 📌 Framework/Middleware integration to `TTL` transmittance

[`TransmittableThreadLocal.Transmitter`](../src/main/java/com/alibaba/ttl/TransmittableThreadLocal.java#L362) to capture all `TTL` values of current thread and replay them in another thread.

There are following methods：

1. `capture`: capture all `TTL` values in current thread
2. `replay`: replay the captured `TTL` values in the current thread, and return the backup `TTL` values before replay
3. `restore`: restore `TTL` values before replay

Sample code：

```java
// ===========================================================================
// Thread A
// ===========================================================================

TransmittableThreadLocal<String> context = new TransmittableThreadLocal<String>();
context.set("value-set-in-parent");

// 1. capture all TTL values in current thread
final Object captured = TransmittableThreadLocal.Transmitter.capture();

// ===========================================================================
// Thread B
// ===========================================================================

// 2. replay the captured TTL values in current thread, and return the backup TTL values before replay
final Object backup = TransmittableThreadLocal.Transmitter.replay(captured);
try {
    // Your biz code, you can get the TTL value from here
    String value = context.get();
    ...
} finally {
    // 3. restore TTL values before replay
    TransmittableThreadLocal.Transmitter.restore(backup);
}
```


- For more info about `TransmittableThreadLocal.Transmitter`, see [its Javadoc](../src/main/java/com/alibaba/ttl/TransmittableThreadLocal.java#L266-L362).
- For more actual implementation code of `TTL` transmittance, see [`TtlRunnable.java`](../src/main/java/com/alibaba/ttl/TtlRunnable.java) and [`TtlCallable.java`](../src/main/java/com/alibaba/ttl/TtlCallable.java).

# 📚 Related material

## `JDK` core classes

- [WeakHashMap](https://docs.oracle.com/javase/10/docs/api/java/util/WeakHashMap.html)
- [InheritableThreadLocal](https://docs.oracle.com/javase/10/docs/api/java/lang/InheritableThreadLocal.html)

## `Java` Agent

- [Java Agent Specification](https://docs.oracle.com/javase/10/docs/api/java/lang/instrument/package-summary.html)

## `Javassist`

- [Getting Started with Javassist](https://www.javassist.org/tutorial/tutorial.html)

## `Maven Shade plugin`

- [`Maven Shade plugin` doc](https://maven.apache.org/plugins/maven-shade-plugin/)
