multi-thread context(MTC)
=====================================

[![Build Status](https://travis-ci.org/alibaba/multi-thread-context.svg?branch=master)](https://travis-ci.org/alibaba/multi-thread-context)
[![Coverage Status](https://coveralls.io/repos/alibaba/multi-thread-context/badge.svg?branch=master)](https://coveralls.io/r/alibaba/multi-thread-context?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.alibaba/multithread.context/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.alibaba/multithread.context/)
[![GitHub release](https://img.shields.io/github/release/alibaba/multi-thread-context.svg)](https://github.com/alibaba/multi-thread-context/releases)  
[![Dependency Status](https://www.versioneye.com/user/projects/553a308b1d2989bdd5000073/badge.svg?style=flat)](https://www.versioneye.com/user/projects/553a308b1d2989bdd5000073)
[![GitHub issues](https://img.shields.io/github/issues/alibaba/multi-thread-context.svg)](https://github.com/alibaba/multi-thread-context/issues)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

<div align="right">
<a href="README-EN.md">English Documentation</a>
</div>

:wrench: 功能
----------------------------

:point_right: 在使用线程池等会缓存线程的组件情况下，完成多线程的Context传递。

`JDK`的[`java.lang.InheritableThreadLocal`](http://docs.oracle.com/javase/6/docs/api/java/lang/InheritableThreadLocal.html)类可以完成父子线程的Context传递。

但对于使用线程池等会缓存线程的组件的情况，线程由线程池创建好，并且线程是缓存起来反复使用的。这时父子线程关系的上下文传递已经没有意义，应用中要做上下文传递，实际上是在把 **任务提交给线程池时**的上下文传递到 **任务执行时**。

欢迎提建议或问题（[提交Issue](https://github.com/alibaba/multi-thread-context/issues)）和[Fork后提交代码](https://github.com/alibaba/multi-thread-context/fork)！

:art: 需求场景
----------------------------

### 应用容器或上层框架跨应用代码给下层SDK传递信息

举个场景，`App Engine`（`PAAS`）上会运行由应用提供商提供的应用（`SAAS`模式）。多个`SAAS`用户购买并使用这个应用（即`SAAS`应用）。`SAAS`应用往往是一个实例为多个`SAAS`用户提供服务。    
\# 另一种模式是：`SAAS`用户使用完全独立一个`SAAS`应用，包含独立应用实例及其后的数据源（如`DB`、缓存，etc）。

需要避免的`SAAS`应用拿到多个`SAAS`用户的数据。

一个解决方法是处理过程关联一个`SAAS`用户的上下文，在上下文中应用只能处理（读&写）这个`SAAS`用户的数据。

请求由`SAAS`用户发起（如从`Web`请求进入`App Engine`），`App Engine`可以知道是从哪个`SAAS`用户，在`Web`请求时在上下文中设置好`SAAS`用户`ID`。

应用处理数据（`DB`、`Web`、消息 etc.）是通过`App Engine`提供的服务`SDK`来完成。当应用处理数据时，`SDK`检查数据所属的`SAAS`用户是否和上下文中的`SAAS`用户`ID`一致，如果不一致则拒绝数据的读写。

应用代码会使用线程池，并且这样的使用是正常的业务需求。`SAAS`用户`ID`的从要`App Engine`传递到下层`SDK`，要支持这样的用法。

### 日志记录系统上下文

`App Engine`的日志（如，`SDK`会记录日志）要记录系统上下文。由于不限制用户应用使用线程池，系统的上下文需要能跨线程的传递，且不影响应用代码。

### 上面场景使用`MTC`的整体构架

<img src="docs/mtc-arch.png" alt="构架图" width="260" />

构架涉及3个角色：容器、用户应用、`SDK`。

整体流程：

1. 请求进入`PAAS`容器，提取上下文信息并设置好上下文。
2. 进入用户应用处理业务，业务调用`SDK`（如`DB`、消息、etc）。    
用户应用会使用线程池，所以调用`SDK`的线程可能不是请求的线程。
3. 进入`SDK`处理。    
提取上下文的信息，决定是否符合拒绝处理。

整个过程中，上下文的传递 对于 **用户应用代码** 期望是透明的。

:notebook: User Guide
=====================================

使用类[`MtContextThreadLocal`](src/main/java/com/alibaba/mtc/MtContextThreadLocal.java)来保存上下文，并跨线程池传递。

[`MtContextThreadLocal`](src/main/java/com/alibaba/mtc/MtContextThreadLocal.java)继承[`java.lang.InheritableThreadLocal`](http://docs.oracle.com/javase/6/docs/api/java/lang/InheritableThreadLocal.html)，使用方式也类似。

比[`java.lang.InheritableThreadLocal`](http://docs.oracle.com/javase/6/docs/api/java/lang/InheritableThreadLocal.html)，添加了`protected`方法`copy`，用于定制 **任务提交给线程池时**的上下文传递到 **任务执行时**时的拷贝行为，缺省是传递的是引用。

具体使用方式见下面的说明。

1. 简单使用
----------------------------

父线程给子线程传递Context。

示例代码：

```java
// 在父线程中设置
MtContextThreadLocal<String> parent = new MtContextThreadLocal<String>();
parent.set("value-set-in-parent");

// =====================================================

// 在子线程中可以读取, 值是"value-set-in-parent"
String value = parent.get(); 
```

这是其实是[`java.lang.InheritableThreadLocal`](http://docs.oracle.com/javase/6/docs/api/java/lang/InheritableThreadLocal.html)的功能，应该使用[`java.lang.InheritableThreadLocal`](http://docs.oracle.com/javase/6/docs/api/java/lang/InheritableThreadLocal.html)来完成。

但对于使用了异步执行（往往使用线程池完成）的情况，线程由线程池创建好，并且线程是缓存起来反复使用的。

这时父子线程关系的上下文传递已经没有意义，应用中要做上下文传递，实际上是在把 **任务提交给线程池时**的上下文传递到 **任务执行时**。
解决方法参见下面的这几种用法。

2. 保证线程池中传递Context
----------------------------

### 2.1 修饰`Runnable`和`Callable`

使用[`com.alibaba.mtc.MtContextRunnable`](src/main/java/com/alibaba/mtc/MtContextRunnable.java)和[`com.alibaba.mtc.MtContextCallable`](src/main/java/com/alibaba/mtc/MtContextCallable.java)来修饰传入线程池的`Runnable`和`Callable`。

示例代码：

```java
MtContextThreadLocal<String> parent = new MtContextThreadLocal<String>();
parent.set("value-set-in-parent");

Runnable task = new Task("1");
// 额外的处理，生成修饰了的对象mtContextRunnable
Runnable mtContextRunnable = MtContextRunnable.get(task); 
executorService.submit(mtContextRunnable);

// =====================================================

// Task中可以读取, 值是"value-set-in-parent"
String value = parent.get(); 
```

上面演示了`Runnable`，`Callable`的处理类似

```java
MtContextThreadLocal<String> parent = new MtContextThreadLocal<String>();
parent.set("value-set-in-parent");

Callable call = new Call("1");
// 额外的处理，生成修饰了的对象mtContextCallable
Callable mtContextCallable = MtContextCallable.get(call); 
executorService.submit(mtContextCallable);

// =====================================================

// Call中可以读取, 值是"value-set-in-parent"
String value = parent.get(); 
```

#### 这种使用方式的时序图

<img src="docs/SequenceDiagram.png" alt="时序图" width="600" />

### 2.2 修饰线程池

省去每次`Runnable`和`Callable`传入线程池时的修饰，这个逻辑可以在线程池中完成。

通过工具类[`com.alibaba.mtc.threadpool.MtContextExecutors`](src/main/java/com/alibaba/mtc/threadpool/MtContextExecutors.java)完成，有下面的方法：

* `getMtcExecutor`：修饰接口`Executor`
* `getMtcExecutorService`：修饰接口`ExecutorService`
* `ScheduledExecutorService`：修饰接口`ScheduledExecutorService`

示例代码：

```java
ExecutorService executorService = ...
// 额外的处理，生成修饰了的对象executorService
executorService = MtContextExecutors.getMtcExecutorService(executorService); 

MtContextThreadLocal<String> parent = new MtContextThreadLocal<String>();
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

这种方式，实现线程池的`MtContext`传递过程中，代码中没有修饰`Runnble`或是线程池的代码。    
\# 即可以做到应用代码 **无侵入**，后面文档有结合实际场景的架构对这一点的说明。

示例代码：

```java
// 框架代码
MtContextThreadLocal<String> parent = new MtContextThreadLocal<String>();
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

Demo参见[`AgentDemo.java`](src/test/java/com/alibaba/mtc/threadpool/agent/demo/AgentDemo.java)。

目前Agent中，修饰了`jdk`中的两个线程池实现类（实现代码在[`MtContextTransformer.java`](src/main/java/com/alibaba/mtc/threadpool/agent/MtContextTransformer.java)）：

- `java.util.concurrent.ThreadPoolExecutor`
- `java.util.concurrent.ScheduledThreadPoolExecutor`

在`Java`的启动参数加上：

- `-Xbootclasspath/a:/path/to/multithread.context-1.1.0.jar`
- `-javaagent:/path/to/multithread.context-1.1.0.jar`

**注意**： 

* Agent修改是JDK的类，类中加入了引用`MTC`的代码，所以`MTC Agent`的`Jar`要加到`bootclasspath`上。

Java命令行示例如下：

```bash
java -Xbootclasspath/a:multithread.context-1.1.0.jar \
    -javaagent:multithread.context-1.1.0-SNAPSHOT.jar \
    -cp classes \
    com.alibaba.mtc.threadpool.agent.demo.AgentDemo
```

有Demo演示『使用Java Agent来修饰线程池实现类』，执行工程下的脚本[`run-agent-demo.sh`](run-agent-demo.sh)即可运行Demo。

#### 什么情况下，`Java Agent`的使用方式`MtContext`会失效？

由于`Runnable`和`Callable`的修饰代码，是在线程池类中插入的。下面的情况会让插入的代码被绕过，`MtContext`会失效。

- 用户代码中继承`java.util.concurrent.ThreadPoolExecutor`和`java.util.concurrent.ScheduledThreadPoolExecutor`，
覆盖了`execute`、`submit`、`schedule`等提交任务的方法，并且没有调用父类的方法。   
修改线程池类的实现，`execute`、`submit`、`schedule`等提交任务的方法禁止这些被覆盖，可以规避这个问题。
- 目前，没有修饰`java.util.Timer`类，使用`Timer`时，`MtContext`会有问题。

:electric_plug: Java API Docs
======================

当前版本的Java API文档地址： <http://alibaba.github.io/multi-thread-context/apidocs/>

:cookie: Maven依赖
=====================================

示例：

```xml
<dependency>
	<groupId>com.alibaba</groupId>
	<artifactId>multithread.context</artifactId>
	<version>1.1.0</version>
</dependency>
```

可以在 [search.maven.org](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.alibaba%22%20AND%20a%3A%22multithread.context%22) 查看可用的版本。

:question: FAQ
=====================================

* Mac OS X下，使用javaagent，可能会报`JavaLaunchHelper`的出错信息。  
JDK Bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=8021205  
可以换一个版本的JDK。我的开发机上`1.7.0_40`有这个问题，`1.6.0_51`、`1.7.0_45`可以运行。   
\# `1.7.0_45`还是有`JavaLaunchHelper`的出错信息，但不影响运行。

:moyai: 更多文档
=====================================

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

* [Java Agent规范](http://docs.oracle.com/javase/6/docs/api/java/lang/instrument/package-summary.html)
* [Java SE 6 新特性: Instrumentation 新功能](http://www.ibm.com/developerworks/cn/java/j-lo-jse61/)
* [Creation, dynamic loading and instrumentation with javaagents](http://dhruba.name/2010/02/07/creation-dynamic-loading-and-instrumentation-with-javaagents/)
* [JavaAgent加载机制分析](http://alipaymiddleware.com/jvm/javaagent%E5%8A%A0%E8%BD%BD%E6%9C%BA%E5%88%B6%E5%88%86%E6%9E%90/)
