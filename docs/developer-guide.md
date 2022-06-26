# 🎓 Developer Guide

---------------------------

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->

- [📌 框架/中间件集成`TTL`传递](#-%E6%A1%86%E6%9E%B6%E4%B8%AD%E9%97%B4%E4%BB%B6%E9%9B%86%E6%88%90ttl%E4%BC%A0%E9%80%92)
- [📟 关于`Java Agent`](#-%E5%85%B3%E4%BA%8Ejava-agent)
    - [`Java Agent`方式对应用代码无侵入](#java-agent%E6%96%B9%E5%BC%8F%E5%AF%B9%E5%BA%94%E7%94%A8%E4%BB%A3%E7%A0%81%E6%97%A0%E4%BE%B5%E5%85%A5)
    - [已有`Java Agent`中嵌入`TTL Agent`](#%E5%B7%B2%E6%9C%89java-agent%E4%B8%AD%E5%B5%8C%E5%85%A5ttl-agent)
- [👢 `Bootstrap ClassPath`上添加通用库`Jar`的问题及其解决方法](#-bootstrap-classpath%E4%B8%8A%E6%B7%BB%E5%8A%A0%E9%80%9A%E7%94%A8%E5%BA%93jar%E7%9A%84%E9%97%AE%E9%A2%98%E5%8F%8A%E5%85%B6%E8%A7%A3%E5%86%B3%E6%96%B9%E6%B3%95)
- [🔨 如何编译构建](#-%E5%A6%82%E4%BD%95%E7%BC%96%E8%AF%91%E6%9E%84%E5%BB%BA)
- [发布操作列表](#%E5%8F%91%E5%B8%83%E6%93%8D%E4%BD%9C%E5%88%97%E8%A1%A8)
- [📚 相关资料](#-%E7%9B%B8%E5%85%B3%E8%B5%84%E6%96%99)
    - [`JDK` core classes](#jdk-core-classes)
    - [`Java Agent`](#java-agent)
    - [`Javassist`](#javassist)
    - [`Maven Shade`插件](#maven-shade%E6%8F%92%E4%BB%B6)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

---------------------------

# 📌 框架/中间件集成`TTL`传递

框架/中间件集成`TTL`传递，通过[`TransmittableThreadLocal.Transmitter`](../src/main/java/com/alibaba/ttl/TransmittableThreadLocal.java#L362)
抓取当前线程的所有`TTL`值并在其他线程进行回放；在回放线程执行完业务操作后，恢复为回放线程原来的`TTL`值。

`TransmittableThreadLocal.Transmitter`提供了所有`TTL`值的抓取、回放和恢复方法（即`CRR`操作）：

1. `capture`方法：抓取线程（线程A）的所有`TTL`值。
2. `replay`方法：在另一个线程（线程B）中，回放在`capture`方法中抓取的`TTL`值，并返回 回放前`TTL`值的备份
3. `restore`方法：恢复线程B执行`replay`方法之前的`TTL`值（即备份）

示例代码：

```java
// ===========================================================================
// 线程 A
// ===========================================================================

TransmittableThreadLocal<String> context = new TransmittableThreadLocal<>();
context.set("value-set-in-parent");

// (1) 抓取当前线程的所有TTL值
final Object captured = TransmittableThreadLocal.Transmitter.capture();

// ===========================================================================
// 线程 B（异步线程）
// ===========================================================================

// (2) 在线程 B中回放在capture方法中抓取的TTL值，并返回 回放前TTL值的备份
final Object backup = TransmittableThreadLocal.Transmitter.replay(captured);
try {
    // 你的业务逻辑，这里你可以获取到外面设置的TTL值
    String value = context.get();

    System.out.println("Hello: " + value);
    ...
    String result = "World: " + value;
} finally {
    // (3) 恢复线程 B执行replay方法之前的TTL值（即备份）
    TransmittableThreadLocal.Transmitter.restore(backup);
}
```

更多`TTL`传递的代码实现示例，参见 [`TtlRunnable.java`](../src/main/java/com/alibaba/ttl/TtlRunnable.java)、[`TtlCallable.java`](../src/main/java/com/alibaba/ttl/TtlCallable.java)。

当然可以使用`TransmittableThreadLocal.Transmitter`的工具方法`runSupplierWithCaptured`和`runCallableWithCaptured`和可爱的`Java 8 Lambda`语法
来简化`replay`和`restore`操作，示例代码：

```java
// ===========================================================================
// 线程 A
// ===========================================================================

TransmittableThreadLocal<String> context = new TransmittableThreadLocal<>();
context.set("value-set-in-parent");

// (1) 抓取当前线程的所有TTL值
final Object captured = TransmittableThreadLocal.Transmitter.capture();

// ===========================================================================
// 线程 B（异步线程）
// ===========================================================================

String result = runSupplierWithCaptured(captured, () -> {
    // 你的业务逻辑，这里你可以获取到外面设置的TTL值
    String value = context.get();
    System.out.println("Hello: " + value);
    ...
    return "World: " + value;
}); // (2) + (3)
```

- 更多`TTL`传递的说明，详见[`TransmittableThreadLocal.Transmitter`的`JavaDoc`](../src/main/java/com/alibaba/ttl/TransmittableThreadLocal.java#L266-L362)。
- 更多`TTL`传递的代码实现，参见[`TtlRunnable.java`](../src/main/java/com/alibaba/ttl/TtlRunnable.java)、[`TtlCallable.java`](../src/main/java/com/alibaba/ttl/TtlCallable.java)。

# 📟 关于`Java Agent`

## `Java Agent`方式对应用代码无侵入

[User Guide - 2.3 使用`Java Agent`来修饰`JDK`线程池实现类](../README.md#23-%E4%BD%BF%E7%94%A8java-agent%E6%9D%A5%E4%BF%AE%E9%A5%B0jdk%E7%BA%BF%E7%A8%8B%E6%B1%A0%E5%AE%9E%E7%8E%B0%E7%B1%BB) 说到了，相对修饰`Runnable`或是线程池的方式，`Java Agent`方式是对应用代码无侵入的。下面做一些展开说明。

<img src="scenario-framework-sdk-arch.png" alt="构架图" width="260" />

按框架图，把前面示例代码操作可以分成下面几部分：

1. 读取信息设置到`TTL`。  
    这部分在容器中完成，无需应用参与。
2. 提交`Runnable`到线程池。要有修饰操作`Runnable`（无论是直接修饰`Runnable`还是修饰线程池）。  
    这部分操作一定是在用户应用中触发。
3. 读取`TTL`，做业务检查。  
    在`SDK`中完成，无需应用参与。

只有第2部分的操作和应用代码相关。

如果不通过`Java Agent`修饰线程池，则修饰操作需要应用代码来完成。

使用`Java Agent`方式，应用无需修改代码，即做到 相对应用代码 透明地完成跨线程池的上下文传递。

更多关于应用场景的了解说明参见文档[需求场景](requirement-scenario.md)。

## 已有`Java Agent`中嵌入`TTL Agent`

这样可以减少`Java`启动命令行上的`Agent`的配置。

在自己的`Agent`中加上`TTL Agent`的逻辑，示例代码如下（[`YourXxxAgent.java`](../src/test/java/com/alibaba/demo/ttl/agent/YourXxxAgent.java)）：

```java
import com.alibaba.ttl.threadpool.agent.TtlAgent;
import com.alibaba.ttl.threadpool.agent.TtlTransformer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.logging.Logger;

public final class YourXxxAgent {
    private static final Logger logger = Logger.getLogger(YourXxxAgent.class.getName());

    public static void premain(String agentArgs, Instrumentation inst) {
        TtlAgent.premain(agentArgs, inst); // add TTL Transformer

        // add your Transformer
        ...
    }
}
```

关于`Java Agent`和`ClassFileTransformer`的如何实现可以参考：[`TtlAgent.java`](../src/main/java/com/alibaba/ttl/threadpool/agent/TtlAgent.java)、[`TtlTransformer.java`](../src/main/java/com/alibaba/ttl/threadpool/agent/TtlTransformer.java)。

注意，在`bootclasspath`上，还是要加上`TTL Jar`：

```bash
-Xbootclasspath/a:/path/to/transmittable-thread-local-2.x.y.jar:/path/to/your/agent/jar/files
```

# 👢 `Bootstrap ClassPath`上添加通用库`Jar`的问题及其解决方法

`TTL Agent`的使用方式，需要将`TTL Jar`加到`Bootstrap ClassPath`上（通过`Java`命令行参数`-Xbootclasspath`）；这样`TTL`的类与`JDK`的标准库的类（如`java.lang.String`）的`ClassLoader`是一样的，都在`Bootstrap ClassPath`上。

`Bootstrap ClassPath`上的类会优先于应用`ClassPath`的`Jar`被加载，并且加载`ClassLoader`不能被改。  
\# 当然技术上严格地说，通过`Bootstrap ClassPath`上的类（如标准库的类）是可以改`ClassLoader`的，但这样做一般只会带来各种麻烦的问题。关于`ClassLoader`及其使用注意的介绍说明 可以参见[ClassLoader委托关系的完备配置](https://github.com/oldratlee/land#1-classloader%E5%A7%94%E6%89%98%E5%85%B3%E7%B3%BB%E7%9A%84%E5%AE%8C%E5%A4%87%E9%85%8D%E7%BD%AE)。

`TTL Agent`自己内部实现使用了`Javassist`，即在`Bootstrap ClassPath`上也需要添加`Javassist`。如果应用中也使用了`Javassist`，由于运行时会优先使用`TTL Agent`配置`Bootstrap ClassPath`上的`Javassist`，应用逻辑运行时实际不能选择/指定应用自己的`Javassist`的版本，带来了 应用需要的`Javassist`与`TTL Agent`用的`Javassist`之间的兼容性风险。

可以通过 `repackage`依赖（即 重命名/改写 依赖类的包名）来解决这个问题。`Maven`提供了[`Shade`插件](https://maven.apache.org/plugins/maven-shade-plugin/)，可以完成下面的操作：

- `repackage` `Javassist`的类文件
- 添加`repackage`过的`Javassist`到`TTL Jar`中

这样操作后，`TTL Agent`不需要依赖外部的`Javassist`依赖，效果上这样的`shade`过的`TTL Jar`是自包含的、在使用上是编译/运行时0依赖的，自然也规避了依赖冲突的问题。

# 🔨 如何编译构建

编译构建的环境要求： **_`JDK 8+`_**；用`Maven`常规的方式执行编译构建即可：  
\# 在工程中已经包含了符合版本要求的`Maven`，直接运行 **_工程根目录下的`mvnw`_**；并不需要先手动自己安装好`Maven`。

```bash
# 运行测试Case
./mvnw test
# 编译打包
./mvnw package
# 运行测试Case、编译打包、安装TTL库到Maven本地
./mvnw install

#####################################################
# 如果使用你自己安装的 maven，版本要求：maven 3.3.9+
mvn install
```

# 发布操作列表

详见独立文档 [发布操作列表](release-action-list.md)。

# 📚 相关资料

## `JDK` core classes

- [WeakHashMap](https://docs.oracle.com/javase/10/docs/api/java/util/WeakHashMap.html)
- [InheritableThreadLocal](https://docs.oracle.com/javase/10/docs/api/java/lang/InheritableThreadLocal.html)

## `Java Agent`

- 官方文档
    - [`Java Agent`规范 - `JavaDoc`](https://docs.oracle.com/javase/10/docs/api/java/lang/instrument/package-summary.html#package.description)
    - [JAR File Specification - JAR Manifest](https://docs.oracle.com/javase/10/docs/specs/jar/jar.html#jar-manifest)
    - [Working with Manifest Files - The Java™ Tutorials](https://docs.oracle.com/javase/tutorial/deployment/jar/manifestindex.html)
- [Java SE 6 新特性: Instrumentation 新功能](https://www.ibm.com/developerworks/cn/java/j-lo-jse61/)
- [Creation, dynamic loading and instrumentation with javaagents](https://dhruba.name/2010/02/07/creation-dynamic-loading-and-instrumentation-with-javaagents/)
- [JavaAgent加载机制分析](https://www.iteye.com/blog/nijiaben-1847212/)

## `Javassist`

- [Getting Started with Javassist](https://www.javassist.org/tutorial/tutorial.html)

## `Maven Shade`插件

- [`Maven Shade`插件文档](https://maven.apache.org/plugins/maven-shade-plugin/)
