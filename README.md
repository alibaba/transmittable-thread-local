Transmittable ThreadLocal(TTL)
=====================================

[![Build Status](https://travis-ci.org/alibaba/transmittable-thread-local.svg?branch=master)](https://travis-ci.org/alibaba/transmittable-thread-local)
[![Coverage Status](https://img.shields.io/codecov/c/github/alibaba/transmittable-thread-local/master.svg)](https://codecov.io/gh/alibaba/transmittable-thread-local/branch/master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.alibaba/transmittable-thread-local/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.alibaba/transmittable-thread-local/)
[![GitHub release](https://img.shields.io/github/release/alibaba/transmittable-thread-local.svg)](https://github.com/alibaba/transmittable-thread-local/releases)
[![Dependency Status](https://www.versioneye.com/user/projects/56c0a36218b271002c699dca/badge.svg)](https://www.versioneye.com/user/projects/56c0a36218b271002c699dca)  
[![Join the chat at https://gitter.im/alibaba/transmittable-thread-local](https://badges.gitter.im/alibaba/transmittable-thread-local.svg)](https://gitter.im/alibaba/transmittable-thread-local?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![GitHub issues](https://img.shields.io/github/issues/alibaba/transmittable-thread-local.svg)](https://github.com/alibaba/transmittable-thread-local/issues)
[![Percentage of issues still open](http://isitmaintained.com/badge/open/alibaba/transmittable-thread-local.svg)](http://isitmaintained.com/project/alibaba/transmittable-thread-local "Percentage of issues still open")
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/alibaba/transmittable-thread-local.svg)](http://isitmaintained.com/project/alibaba/transmittable-thread-local "Average time to resolve an issue")
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

<div align="right">
<a href="README-EN.md">English Documentation</a>
</div>

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->


- [:wrench: 功能](#wrench-%E5%8A%9F%E8%83%BD)
- [:art: 需求场景](#art-%E9%9C%80%E6%B1%82%E5%9C%BA%E6%99%AF)
- [:busts_in_silhouette: User Guide](#busts_in_silhouette-user-guide)
    - [1. 简单使用](#1-%E7%AE%80%E5%8D%95%E4%BD%BF%E7%94%A8)
    - [2. 保证线程池中传递值](#2-%E4%BF%9D%E8%AF%81%E7%BA%BF%E7%A8%8B%E6%B1%A0%E4%B8%AD%E4%BC%A0%E9%80%92%E5%80%BC)
        - [2.1 修饰`Runnable`和`Callable`](#21-%E4%BF%AE%E9%A5%B0runnable%E5%92%8Ccallable)
            - [整个过程的完整时序图](#%E6%95%B4%E4%B8%AA%E8%BF%87%E7%A8%8B%E7%9A%84%E5%AE%8C%E6%95%B4%E6%97%B6%E5%BA%8F%E5%9B%BE)
        - [2.2 修饰线程池](#22-%E4%BF%AE%E9%A5%B0%E7%BA%BF%E7%A8%8B%E6%B1%A0)
        - [2.3 使用Java Agent来修饰JDK线程池实现类](#23-%E4%BD%BF%E7%94%A8java-agent%E6%9D%A5%E4%BF%AE%E9%A5%B0jdk%E7%BA%BF%E7%A8%8B%E6%B1%A0%E5%AE%9E%E7%8E%B0%E7%B1%BB)
            - [`Java Agent`的使用方式在什么情况下`TTL`会失效](#java-agent%E7%9A%84%E4%BD%BF%E7%94%A8%E6%96%B9%E5%BC%8F%E5%9C%A8%E4%BB%80%E4%B9%88%E6%83%85%E5%86%B5%E4%B8%8Bttl%E4%BC%9A%E5%A4%B1%E6%95%88)
- [:electric_plug: Java API Docs](#electric_plug-java-api-docs)
- [:cookie: Maven依赖](#cookie-maven%E4%BE%9D%E8%B5%96)
- [:question: FAQ](#question-faq)
- [:moyai: 更多文档](#moyai-%E6%9B%B4%E5%A4%9A%E6%96%87%E6%A1%A3)
- [:books: 相关资料](#books-%E7%9B%B8%E5%85%B3%E8%B5%84%E6%96%99)
    - [Jdk core classes](#jdk-core-classes)
    - [Java Agent](#java-agent)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

:wrench: 功能
============================

:point_right: 在使用线程池等会缓存线程的组件情况下，提供`ThreadLocal`值的传递功能，解决异步执行时上下文传递的问题。

`JDK`的[`InheritableThreadLocal`](http://docs.oracle.com/javase/7/docs/api/java/lang/InheritableThreadLocal.html)类可以完成父线程到子线程的值传递。但对于使用线程池等会缓存线程的组件的情况，线程由线程池创建好，并且线程是缓存起来反复使用的；这时父子线程关系的`ThreadLocal`值传递已经没有意义，应用需要的实际上是把 **任务提交给线程池时**的`ThreadLocal`值传递到 **任务执行时**。

本库提供的[`TransmittableThreadLocal`](src/main/java/com/alibaba/ttl/TransmittableThreadLocal.java)类继承并加强[`InheritableThreadLocal`](http://docs.oracle.com/javase/7/docs/api/java/lang/InheritableThreadLocal.html)类，解决上述的问题，使用详见[User Guide](#busts_in_silhouette-user-guide)。

欢迎 :clap:

- 建议和提问，[提交`Issue`](https://github.com/oldratlee/translations/issues/new)
- 贡献和改进，[`Fork`后提通过`Pull Request`贡献代码](https://github.com/oldratlee/translations/fork)

:art: 需求场景
============================

在`ThreadLocal`的需求场景即是`TTL`的潜在需求场景，如果你的业务需要『在使用线程池等会缓存线程的组件情况下传递`ThreadLocal`』则是`TTL`目标场景。

下面是几个典型场景例子。

1. 分布式跟踪系统
2. 应用容器或上层框架跨应用代码给下层`SDK`传递信息
3. 日志收集记录系统上下文

各个场景的展开说明参见子文档 [需求场景](docs/requirement-scenario.md)。

:busts_in_silhouette: User Guide
=====================================

使用类[`TransmittableThreadLocal`](src/main/java/com/alibaba/ttl/TransmittableThreadLocal.java)来保存值，并跨线程池传递。

[`TransmittableThreadLocal`](src/main/java/com/alibaba/ttl/TransmittableThreadLocal.java)继承[`InheritableThreadLocal`](http://docs.oracle.com/javase/7/docs/api/java/lang/InheritableThreadLocal.html)，使用方式也类似。

相比[`InheritableThreadLocal`](http://docs.oracle.com/javase/7/docs/api/java/lang/InheritableThreadLocal.html)，添加了

1. `protected`方法`copy`  
    用于定制 **任务提交给线程池时**的`ThreadLocal`值传递到 **任务执行时**时的拷贝行为，缺省传递的是引用。
1. `protected`方法`beforeExecute`/`afterExecute`  
    执行任务(`Runnable`/`Callable`)的前/后的生命周期回调，缺省是空操作。

具体使用方式见下面的说明。

## 1. 简单使用

父线程给子线程传递值。

示例代码：

```java
// 在父线程中设置
TransmittableThreadLocal<String> parent = new TransmittableThreadLocal<String>();
parent.set("value-set-in-parent");

// =====================================================

// 在子线程中可以读取, 值是"value-set-in-parent"
String value = parent.get();
```

这是其实是[`InheritableThreadLocal`](http://docs.oracle.com/javase/7/docs/api/java/lang/InheritableThreadLocal.html)的功能，应该使用[`InheritableThreadLocal`](http://docs.oracle.com/javase/7/docs/api/java/lang/InheritableThreadLocal.html)来完成。

但对于使用线程池等会缓存线程的组件的情况，线程由线程池创建好，并且线程是缓存起来反复使用的；这时父子线程关系的`ThreadLocal`值传递已经没有意义，应用需要的实际上是把 **任务提交给线程池时**的`ThreadLocal`值传递到 **任务执行时**。

解决方法参见下面的这几种用法。

## 2. 保证线程池中传递值

### 2.1 修饰`Runnable`和`Callable`

使用[`com.alibaba.ttl.TtlRunnable`](src/main/java/com/alibaba/ttl/TtlRunnable.java)和[`com.alibaba.ttl.TtlCallable`](src/main/java/com/alibaba/ttl/TtlCallable.java)来修饰传入线程池的`Runnable`和`Callable`。

示例代码：

```java
TransmittableThreadLocal<String> parent = new TransmittableThreadLocal<String>();
parent.set("value-set-in-parent");

Runnable task = new Task("1");
// 额外的处理，生成修饰了的对象ttlRunnable
Runnable ttlRunnable = TtlRunnable.get(task);
executorService.submit(ttlRunnable);

// =====================================================

// Task中可以读取, 值是"value-set-in-parent"
String value = parent.get();
```

上面演示了`Runnable`，`Callable`的处理类似

```java
TransmittableThreadLocal<String> parent = new TransmittableThreadLocal<String>();
parent.set("value-set-in-parent");

Callable call = new Call("1");
// 额外的处理，生成修饰了的对象ttlCallable
Callable ttlCallable = TtlCallable.get(call);
executorService.submit(ttlCallable);

// =====================================================

// Call中可以读取, 值是"value-set-in-parent"
String value = parent.get();
```

#### 整个过程的完整时序图

![时序图](docs/TransmittableThreadLocal-sequence-diagram.png)

### 2.2 修饰线程池

省去每次`Runnable`和`Callable`传入线程池时的修饰，这个逻辑可以在线程池中完成。

通过工具类[`com.alibaba.ttl.threadpool.TtlExecutors`](src/main/java/com/alibaba/ttl/threadpool/TtlExecutors.java)完成，有下面的方法：

* `getTtlExecutor`：修饰接口`Executor`
* `getTtlExecutorService`：修饰接口`ExecutorService`
* `ScheduledExecutorService`：修饰接口`ScheduledExecutorService`

示例代码：

```java
ExecutorService executorService = ...
// 额外的处理，生成修饰了的对象executorService
executorService = TtlExecutors.getTtlExecutorService(executorService);

TransmittableThreadLocal<String> parent = new TransmittableThreadLocal<String>();
parent.set("value-set-in-parent");

Runnable task = new Task("1");
Callable call = new Call("2");
executorService.submit(task);
executorService.submit(call);

// =====================================================

// Task或是Call中可以读取, 值是"value-set-in-parent"
String value = parent.get();
```

### 2.3 使用Java Agent来修饰JDK线程池实现类

这种方式，实现线程池的传递是透明的，代码中没有修饰`Runnable`或是线程池的代码。即可以做到应用代码 **无侵入**，后面文档有结合实际场景的架构对这一点的说明。

示例代码：

```java
// 框架代码
TransmittableThreadLocal<String> parent = new TransmittableThreadLocal<String>();
parent.set("value-set-in-parent");

// 应用代码
ExecutorService executorService = Executors.newFixedThreadPool(3);

Runnable task = new Task("1");
Callable call = new Call("2");
executorService.submit(task);
executorService.submit(call);

// =====================================================

// Task或是Call中可以读取, 值是"value-set-in-parent"
String value = parent.get();
```

Demo参见[`AgentDemo.java`](src/test/java/com/alibaba/ttl/threadpool/agent/demo/AgentDemo.java)。

目前Agent中，修饰了`jdk`中的两个线程池实现类（实现代码在[`TtlTransformer.java`](src/main/java/com/alibaba/ttl/threadpool/agent/TtlTransformer.java)）：

- `java.util.concurrent.ThreadPoolExecutor`
- `java.util.concurrent.ScheduledThreadPoolExecutor`

在`Java`的启动参数加上：

- `-Xbootclasspath/a:/path/to/transmittable-thread-local-2.x.x.jar`
- `-javaagent:/path/to/transmittable-thread-local-2.x.x.jar`

**注意**：

* Agent修改是JDK的类，类中加入了引用`TTL`的代码，所以`TTL Agent`的`Jar`要加到`bootclasspath`上。

Java命令行示例如下：

```bash
java -Xbootclasspath/a:transmittable-thread-local-2.0.0.jar \
    -javaagent:transmittable-thread-local-2.0.0.jar \
    -cp classes \
    com.alibaba.ttl.threadpool.agent.demo.AgentDemo
```

有Demo演示『使用Java Agent来修饰线程池实现类』，执行工程下的脚本[`run-agent-demo.sh`](run-agent-demo.sh)即可运行Demo。

#### `Java Agent`的使用方式在什么情况下`TTL`会失效

由于`Runnable`和`Callable`的修饰代码，是在线程池类中插入的。下面的情况会让插入的代码被绕过，传递会失效。

- 用户代码中继承`java.util.concurrent.ThreadPoolExecutor`和`java.util.concurrent.ScheduledThreadPoolExecutor`，
覆盖了`execute`、`submit`、`schedule`等提交任务的方法，并且没有调用父类的方法。   
修改线程池类的实现，`execute`、`submit`、`schedule`等提交任务的方法禁止这些被覆盖，可以规避这个问题。
- 目前，没有修饰`java.util.Timer`类，使用`Timer`时，`TTL`会有问题。

:electric_plug: Java API Docs
======================

当前版本的Java API文档地址： <http://alibaba.github.io/transmittable-thread-local/apidocs/>

:cookie: Maven依赖
=====================================

示例：

```xml
<dependency>
	<groupId>com.alibaba</groupId>
	<artifactId>transmittable-thread-local</artifactId>
	<version>2.1.0</version>
</dependency>
```

可以在 [search.maven.org](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.alibaba%22%20AND%20a%3A%22transmittable-thread-local%22) 查看可用的版本。

:question: FAQ
=====================================

* Mac OS X下，使用javaagent，可能会报`JavaLaunchHelper`的出错信息。  
JDK Bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=8021205  
可以换一个版本的JDK。我的开发机上`1.7.0_40`有这个问题，`1.6.0_51`、`1.7.0_45`可以运行。   
\# `1.7.0_45`还是有`JavaLaunchHelper`的出错信息，但不影响运行。

:moyai: 更多文档
=====================================

- [需求场景说明](docs/requirement-scenario.md)
- [Developer Guide](docs/developer-guide.md)
- [性能测试](docs/performance-test.md)

:books: 相关资料
=====================================

Jdk core classes
----------------------------

* [WeakHashMap](http://docs.oracle.com/javase/7/docs/api/java/util/WeakHashMap.html)
* [InheritableThreadLocal](http://docs.oracle.com/javase/7/docs/api/java/lang/InheritableThreadLocal.html)

Java Agent
----------------------------

* [Java Agent规范](http://docs.oracle.com/javase/7/docs/api/java/lang/instrument/package-summary.html)
* [Java SE 6 新特性: Instrumentation 新功能](http://www.ibm.com/developerworks/cn/java/j-lo-jse61/)
* [Creation, dynamic loading and instrumentation with javaagents](http://dhruba.name/2010/02/07/creation-dynamic-loading-and-instrumentation-with-javaagents/)
* [JavaAgent加载机制分析](http://alipaymiddleware.com/jvm/javaagent%E5%8A%A0%E8%BD%BD%E6%9C%BA%E5%88%B6%E5%88%86%E6%9E%90/)
