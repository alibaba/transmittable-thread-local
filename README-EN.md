# <div align="center"><a href="#dummy"><img src="https://user-images.githubusercontent.com/1063891/233595946-4493119e-4e0c-4081-a382-0a20731c578e.png" alt="📌 TransmittableThreadLocal(TTL)"></a></div>

> [!IMPORTANT]
> 🚧 This branch is `TransmittableThreadLocal(TTL) v3`, which is in development and has not been released yet.  
> See [issue 432](https://github.com/alibaba/transmittable-thread-local/issues/432) for the `v3` notes, work item list and its progress.
>
> 👉 The stable version `v2.x` currently in use is on [**branch `2.x`**](https://github.com/alibaba/transmittable-thread-local/tree/2.x).

<p align="center">
<a href="https://github.com/alibaba/transmittable-thread-local/actions/workflows/ci.yaml"><img src="https://img.shields.io/github/actions/workflow/status/alibaba/transmittable-thread-local/ci.yaml?branch=master&logo=github&logoColor=white&label=fast ci" alt="Fast CI"></a>
<a href="https://github.com/alibaba/transmittable-thread-local/actions/workflows/strong_ci.yaml"><img src="https://img.shields.io/github/actions/workflow/status/alibaba/transmittable-thread-local/strong_ci.yaml?branch=master&logo=github&logoColor=white&label=strong ci" alt="Strong CI"></a>
<a href="https://app.codecov.io/gh/alibaba/transmittable-thread-local/tree/master"><img src="https://badgen.net/codecov/c/github/alibaba/transmittable-thread-local/master?icon=codecov" alt="Coverage Status"></a>
<a href="https://openjdk.java.net/"><img src="https://img.shields.io/badge/Java-6+-339933?logo=openjdk&logoColor=white" alt="JDK support"></a>
<a href="https://www.apache.org/licenses/LICENSE-2.0.html"><img src="https://img.shields.io/github/license/alibaba/transmittable-thread-local?color=4D7A97&logo=apache" alt="License"></a>
<a href="https://alibaba.github.io/transmittable-thread-local/apidocs/"><img src="https://img.shields.io/github/release/alibaba/transmittable-thread-local?label=javadoc&color=339933&logo=microsoft-academic&logoColor=white" alt="Javadocs"></a>
<a href="https://repo1.maven.org/maven2/com/alibaba/transmittable-thread-local/maven-metadata.xml"><img src="https://img.shields.io/maven-central/v/com.alibaba/transmittable-thread-local?logo=apache-maven&logoColor=white" alt="Maven Central"></a>
<a href="https://github.com/alibaba/transmittable-thread-local/releases"><img src="https://img.shields.io/github/release/alibaba/transmittable-thread-local" alt="GitHub release"></a>
<a href="https://github.com/alibaba/transmittable-thread-local/stargazers"><img src="https://img.shields.io/github/stars/alibaba/transmittable-thread-local?style=flat" alt="GitHub Stars"></a>
<a href="https://github.com/alibaba/transmittable-thread-local/fork"><img src="https://img.shields.io/github/forks/alibaba/transmittable-thread-local?style=flat" alt="GitHub Forks"></a>
<a href="https://github.com/alibaba/transmittable-thread-local/network/dependents"><img src="https://badgen.net/github/dependents-repo/alibaba/transmittable-thread-local?label=user%20repos" alt="user repos"></a>
<a href="https://github.com/alibaba/transmittable-thread-local/issues"><img src="https://img.shields.io/github/issues/alibaba/transmittable-thread-local" alt="GitHub issues"></a>
<a href="https://github.com/alibaba/transmittable-thread-local/graphs/contributors"><img src="https://img.shields.io/github/contributors/alibaba/transmittable-thread-local" alt="GitHub Contributors"></a>
<a href="https://github.com/alibaba/transmittable-thread-local"><img src="https://img.shields.io/github/repo-size/alibaba/transmittable-thread-local" alt="GitHub repo size"></a>
<a href="https://gitpod.io/#https://github.com/alibaba/transmittable-thread-local"><img src="https://img.shields.io/badge/Gitpod-ready to code-339933?label=gitpod&logo=gitpod&logoColor=white" alt="gitpod: Ready to Code"></a>
</p>

📖 English Documentation | [📖 中文文档](README.md)

----------------------------------------

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->

- [🔧 Functions](#-functions)
- [🎨 Requirements](#-requirements)
- [👥 User Guide](#-user-guide)
    - [1. Simple usage](#1-simple-usage)
    - [2. Transmit value even using thread pool](#2-transmit-value-even-using-thread-pool)
        - [2.1 Decorate `Runnable` and `Callable`](#21-decorate-runnable-and-callable)
        - [2.2 Decorate thread pool](#22-decorate-thread-pool)
        - [2.3 Use Java Agent to decorate thread pool implementation class](#23-use-java-agent-to-decorate-thread-pool-implementation-class)
- [🔌 Java API Docs](#-java-api-docs)
- [🍪 Maven Dependency](#-maven-dependency)
- [🔨 How to compile and build](#-how-to-compile-and-build)
- [🗿 More Documentation](#-more-documentation)
- [📚 Related Resources](#-related-resources)
    - [JDK Core Classes](#jdk-core-classes)
- [💗 Who Used](#-who-used)
- [👷 Contributors](#-contributors)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

----------------------------------------

# 🔧 Functions

👉 `TransmittableThreadLocal`(`TTL`): The missing Java™ std lib(simple & 0-dependency) for framework/middleware,
provide an enhanced `InheritableThreadLocal` that transmits values between threads even using thread pooling components. Support `Java 6~21`.

Class [`InheritableThreadLocal`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/InheritableThreadLocal.html) in `JDK`
can transmit value to child thread from parent thread.

But when use thread pool, thread is cached up and used repeatedly. Transmitting value from parent thread to child thread has no meaning.
Application need transmit value from the time task is created to the time task is executed.

If you have problem or question, please [submit Issue](https://github.com/alibaba/transmittable-thread-local/issues) or play [fork](https://github.com/alibaba/transmittable-thread-local/fork) and pull request dance.

> [!NOTE]
> From `TTL v2.13+` upgrade to `Java 8`. 🚀  
> If you need `Java 6` support, use version `2.12.x` <a href="https://repo1.maven.org/maven2/com/alibaba/transmittable-thread-local/maven-metadata.xml"><img src="https://img.shields.io/maven-central/v/com.alibaba/transmittable-thread-local?versionPrefix=2.12.&color=lightgrey&logo=apache-maven&logoColor=white" alt="Maven Central"></a>

# 🎨 Requirements

The Requirements listed below is also why I sort out `TransmittableThreadLocal` in my work.

- Application container or high layer framework transmit information to low layer sdk.
- Transmit context to logging without application code aware.

# 👥 User Guide

## 1. Simple usage

```java
TransmittableThreadLocal<String> context = new TransmittableThreadLocal<>();

// =====================================================

// set in parent thread
context.set("value-set-in-parent");

// =====================================================

// read in child thread, value is "value-set-in-parent"
String value = context.get();
```

\# See the executable demo [`SimpleDemo.kt`](ttl-core/src/test/java/com/alibaba/demo/ttl3/SimpleDemo.kt) with full source code.

This is the function of class `InheritableThreadLocal`, should use class `InheritableThreadLocal` instead.

But when use thread pool, thread is cached up and used repeatedly. Transmitting value from parent thread to child thread has no meaning.
Application need transmit value from the time task is created to the point task is executed.

The solution is below usage.

## 2. Transmit value even using thread pool

### 2.1 Decorate `Runnable` and `Callable`

Decorate input `Runnable` and `Callable` by [`TtlRunnable`](ttl-core/src/main/java/com/alibaba/ttl3/TtlRunnable.java)
and [`TtlCallable`](ttl-core/src/main/java/com/alibaba/ttl3/TtlCallable.java).

Sample code:

```java
TransmittableThreadLocal<String> context = new TransmittableThreadLocal<>();

// =====================================================

// set in parent thread
context.set("value-set-in-parent");

Runnable task = new RunnableTask();
// extra work, create decorated ttlRunnable object
Runnable ttlRunnable = TtlRunnable.get(task);
executorService.submit(ttlRunnable);

// =====================================================

// read in task, value is "value-set-in-parent"
String value = context.get();
```

**_NOTE_**：  
Even when the same `Runnable` task is submitted to the thread pool multiple times, the decoration operations(ie： `TtlRunnable.get(task)`) is required for each submission to capture the value of the `TransmittableThreadLocal` context at submission time; That is, if the same task is submitted next time without reperforming decoration and still using the last `TtlRunnable`, the submitted task will run in the context of the last captured context. The sample code is as follows:


```java
// first submission
Runnable task = new RunnableTask();
executorService.submit(TtlRunnable.get(task));

// ... some biz logic,
// and modified TransmittableThreadLocal context ...
context.set("value-modified-in-parent");

// next submission
// reperform decoration to transmit the modified TransmittableThreadLocal context
executorService.submit(TtlRunnable.get(task));
```

Above code show how to dealing with `Runnable`, `Callable` is similar:

```java
TransmittableThreadLocal<String> context = new TransmittableThreadLocal<>();

// =====================================================

// set in parent thread
context.set("value-set-in-parent");

Callable call = new CallableTask();
// extra work, create decorated ttlCallable object
Callable ttlCallable = TtlCallable.get(call);
executorService.submit(ttlCallable);

// =====================================================

// read in call, value is "value-set-in-parent"
String value = context.get();
```

\# See the executable demo [`TtlWrapperDemo.kt`](ttl-core/src/test/java/com/alibaba/demo/ttl3/TtlWrapperDemo.kt) with full source code.

### 2.2 Decorate thread pool

Eliminating the work of `Runnable` and `Callable` Decoration every time it is submitted to thread pool. This work can be completed in the thread pool.

Use util class
[`TtlExecutors`](ttl-core/src/main/java/com/alibaba/ttl3/executor/TtlExecutors.java)
to decorate thread pool.

Util class `TtlExecutors` has below methods:

- `getTtlExecutor`: decorate interface `Executor`
- `getTtlExecutorService`: decorate interface `ExecutorService`
- `getTtlScheduledExecutorService`: decorate interface `ScheduledExecutorService`

Sample code:

```java
ExecutorService executorService = ...
// extra work, create decorated executorService object
executorService = TtlExecutors.getTtlExecutorService(executorService);

TransmittableThreadLocal<String> context = new TransmittableThreadLocal<>();

// =====================================================

// set in parent thread
context.set("value-set-in-parent");

Runnable task = new RunnableTask();
Callable call = new CallableTask();
executorService.submit(task);
executorService.submit(call);

// =====================================================

// read in Task or Callable, value is "value-set-in-parent"
String value = context.get();
```

\# See the executable demo [`TtlExecutorWrapperDemo.kt`](ttl-core/src/test/java/com/alibaba/demo/ttl3/TtlExecutorWrapperDemo.kt) with full source code.

### 2.3 Use Java Agent to decorate thread pool implementation class

In this usage, transmittance is transparent\(no decoration operation\).

Sample code:

```java
// ## 1. upper layer logic of framework ##
TransmittableThreadLocal<String> context = new TransmittableThreadLocal<>();
context.set("value-set-in-parent");

// ## 2. biz logic ##
ExecutorService executorService = Executors.newFixedThreadPool(3);

Runnable task = new RunnableTask();
Callable call = new CallableTask();
executorService.submit(task);
executorService.submit(call);

// ## 3. underlayer logic of framework ##
// read in Task or Callable, value is "value-set-in-parent"
String value = context.get();
```

\# See the executable demo [`AgentDemo.kt`](ttl2-compatible/src/test/java/com/alibaba/demo/ttl/agent/AgentDemo.kt) with full source code, run demo by the script [`scripts/run-agent-demo.sh`](scripts/run-agent-demo.sh).

At present, `TTL` agent has decorated below `JDK` execution components(aka. thread pool) implementation:

- `java.util.concurrent.ThreadPoolExecutor` and `java.util.concurrent.ScheduledThreadPoolExecutor`
    - decoration implementation code is in [`JdkExecutorTtlTransformlet.java`](ttl-agent/src/main/java/com/alibaba/ttl3/agent/transformlet/internal/JdkExecutorTtlTransformlet.java).
- `java.util.concurrent.ForkJoinTask`（corresponding execution component is `java.util.concurrent.ForkJoinPool`）
    - decoration implementation code is in [`ForkJoinTtlTransformlet.java`](ttl-agent/src/main/java/com/alibaba/ttl3/agent/transformlet/internal/ForkJoinTtlTransformlet.java), supports since version **_`2.5.1`_**.
    - **_NOTE_**: [**_`CompletableFuture`_**](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/CompletableFuture.html) and (parallel) [**_`Stream`_**](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/package-summary.html) introduced in Java 8 is executed through `ForkJoinPool` underneath, so after supporting `ForkJoinPool`, `TTL` also supports `CompletableFuture` and `Stream` transparently. 🎉
- `java.util.TimerTask`（corresponding execution component is `java.util.Timer`）
    - decoration implementation code is in [`TimerTaskTtlTransformlet.java`](ttl-agent/src/main/java/com/alibaba/ttl3/agent/transformlet/internal/TimerTaskTtlTransformlet.java), supports since version **_`2.7.0`_**.
    - **_NOTE_**: Since version `2.11.2` decoration for `TimerTask` default is enable (because correctness is first concern, not the best practice like "It is not recommended to use `TimerTask`" :); before version `2.11.1` default is disable.
    - enabled/disable by agent argument `ttl.agent.enable.timer.task`:
        - `-javaagent:path/to/transmittable-thread-local-2.x.y.jar=ttl.agent.enable.timer.task:true`
        - `-javaagent:path/to/transmittable-thread-local-2.x.y.jar=ttl.agent.enable.timer.task:false`
    - more info about `TTL` agent arguments, see [the javadoc of `TtlAgent.java`](ttl-agent/src/main/java/com/alibaba/ttl3/agent/TtlAgent.java).

Add start options on Java command:

- `-javaagent:path/to/transmittable-thread-local-2.x.y.jar`

Java command example:

```bash
java -javaagent:transmittable-thread-local-2.x.y.jar \
    -cp classes \
    com.alibaba.demo.ttl.agent.AgentDemo

# if changed the TTL jar file name or the TTL version is before 2.6.0,
# should set argument -Xbootclasspath explicitly.
java -javaagent:path/to/ttl-foo-name-changed.jar \
    -Xbootclasspath/a:path/to/ttl-foo-name-changed.jar \
    -cp classes \
    com.alibaba.demo.ttl.agent.AgentDemo

java -javaagent:path/to/transmittable-thread-local-2.5.1.jar \
    -Xbootclasspath/a:path/to/transmittable-thread-local-2.5.1.jar \
    -cp classes \
    com.alibaba.demo.ttl.agent.AgentDemo
```

Run the script [`scripts/run-agent-demo.sh`](scripts/run-agent-demo.sh)
to start demo of "Use Java Agent to decorate thread pool implementation class".



**NOTE**：

- Because TTL agent modified the `JDK` std lib classes, make code refer from std lib class to the TTL classes, so the TTL Agent jar must be added to `boot classpath`.
- Since `v2.6.0`, TTL agent jar will auto add self to `boot classpath`. But you **should _NOT_** modify the downloaded TTL jar file name in the maven repo(eg: `transmittable-thread-local-2.x.y.jar`).
    - if you modified the downloaded TTL jar file name(eg: `ttl-foo-name-changed.jar`),
      you must add TTL agent jar to `boot classpath` manually by java option `-Xbootclasspath/a:path/to/ttl-foo-name-changed.jar`.

The implementation of auto adding self agent jar to `boot classpath` use the `Boot-Class-Path` property of manifest file(`META-INF/MANIFEST.MF`) in the TTL Java Agent Jar:

> [!NOTE]
> `Boot-Class-Path`
>
> A list of paths to be searched by the bootstrap class loader. Paths represent directories or libraries (commonly referred to as JAR or zip libraries on many platforms).
> These paths are searched by the bootstrap class loader after the platform specific mechanisms of locating a class have failed. Paths are searched in the order listed.

More info:

- [`Java Agent Specification` - `JavaDoc`文档](https://docs.oracle.com/en/java/javase/21/docs/api/java.instrument/java/lang/instrument/package-summary.html#package.description)
- [JAR File Specification - JAR Manifest](https://docs.oracle.com/en/java/javase/21/docs/specs/jar/jar.html#jar-manifest)
- [Working with Manifest Files - The Java™ Tutorials](https://docs.oracle.com/javase/tutorial/deployment/jar/manifestindex.html)

# 🔌 Java API Docs

The current version Java API documentation: <https://alibaba.github.io/transmittable-thread-local/apidocs/>

# 🍪 Maven Dependency

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>transmittable-thread-local</artifactId>
    <version>2.14.4</version>
</dependency>
```

Check available version at [maven.org](https://repo1.maven.org/maven2/com/alibaba/transmittable-thread-local/maven-metadata.xml).

# 🔨 How to compile and build

Compilation/build environment require **_`JDK 8+`_**; Compilation can be performed in the normal way of `Maven`.

\# The project already contains `Maven` that satisfied the required version, directly run **_`mvnw` in the project root directory_**; there is no need to manually install `Maven` by yourself.

```bash
# Run test case
./mvnw test
# Compile and package
./mvnw package
# Run test case, compile and package, install TTL library to local Maven
./mvnw install

##################################################
# If you use maven installed by yourself, the version requirement: maven 3.3.9+

mvn install
```

# 🗿 More Documentation

- [🎓 Developer Guide](docs/developer-guide-en.md)

# 📚 Related Resources

## JDK Core Classes

- [WeakHashMap](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/WeakHashMap.html)
- [InheritableThreadLocal](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/InheritableThreadLocal.html)

# 💗 Who Used

Some open-source projects used `TTL`:

- **Middleware**
    - [`sofastack/sofa-rpc` ![](https://img.shields.io/github/stars/sofastack/sofa-rpc.svg?style=social&label=Star)](https://github.com/sofastack/sofa-rpc) [![star](https://gitee.com/sofastack/sofa-rpc/badge/star.svg?theme=gray)](https://gitee.com/sofastack/sofa-rpc)  
      SOFARPC is a high-performance, high-extensibility, production-level Java RPC framework
    - [`trpc-group/trpc-java` ![](https://img.shields.io/github/stars/trpc-group/trpc-java.svg?style=social&label=Star)](https://github.com/trpc-group/trpc-java)  
      A pluggable, high-performance RPC framework written in java
    - [`tencentmusic/supersonic` ![](https://img.shields.io/github/stars/tencentmusic/supersonic.svg?style=social&label=Star)](https://github.com/tencentmusic/supersonic)  
      SuperSonic is an out-of-the-box yet highly extensible framework for building ChatBI
    - [`dromara/hmily` ![](https://img.shields.io/github/stars/dromara/hmily.svg?style=social&label=Star)](https://github.com/dromara/hmily) [![star](https://gitee.com/dromara/hmily/badge/star.svg?theme=gray)](https://gitee.com/dromara/hmily)  
      Distributed transaction solutions
    - [`dromara/gobrs-async` ![](https://img.shields.io/github/stars/dromara/gobrs-async.svg?style=social&label=Star)](https://github.com/dromara/gobrs-async) [![star](https://gitee.com/dromara/gobrs-async/badge/star.svg?theme=gray)](https://gitee.com/dromara/gobrs-async)  
      一款功能强大、配置灵活、带有全链路异常回调、内存优化、异常状态管理于一身的高性能异步编排框架。为企业提供在复杂应用场景下动态任务编排的能力。 针对于复杂场景下，异步线程复杂性、任务依赖性、异常状态难控制性
    - [`dromara/dynamic-tp` ![](https://img.shields.io/github/stars/dromara/dynamic-tp.svg?style=social&label=Star)](https://github.com/dromara/dynamic-tp) [![star](https://gitee.com/dromara/dynamic-tp/badge/star.svg?theme=gray)](https://gitee.com/dromara/dynamic-tp)  
      Lightweight dynamic threadpool, with monitoring and alarming functions, base on popular config centers (already support Nacos、Apollo、Zookeeper, can be customized through SPI)
    - [`opengoofy/hippo4j` ![](https://img.shields.io/github/stars/opengoofy/hippo4j.svg?style=social&label=Star)](https://github.com/opengoofy/hippo4j) [![star](https://gitee.com/magestack/hippo4j/badge/star.svg?theme=gray)](https://gitee.com/magestack/hippo4j)  
      动态线程池框架，附带监控报警功能，支持 JDK、Tomcat、Jetty、Undertow 线程池；Apache RocketMQ、Dubbo、RabbitMQ、Hystrix 消费等线程池。内置两种使用模式：轻量级依赖配置中心以及无中间件依赖版本
    - [`siaorg/sia-gateway` ![](https://img.shields.io/github/stars/siaorg/sia-gateway.svg?style=social&label=Star)](https://github.com/siaorg/sia-gateway)  
      microservice route gateway(zuul-plus)
    - [`huaweicloud/Sermant` ![](https://img.shields.io/github/stars/huaweicloud/Sermant.svg?style=social&label=Star)](https://github.com/huaweicloud/Sermant)  
      Sermant, a proxyless service mesh solution based on Javaagent
    - [`ZTO-Express/zms` ![](https://img.shields.io/github/stars/ZTO-Express/zms.svg?style=social&label=Star)](https://github.com/ZTO-Express/zms) [![star](https://gitee.com/zto_express/zms/badge/star.svg?theme=gray)](https://gitee.com/zto_express/zms)  
      ZTO Message Service
    - [`lxchinesszz/tomato` ![](https://img.shields.io/github/stars/lxchinesszz/tomato.svg?style=social&label=Star)](https://github.com/lxchinesszz/tomato)  
      一款专门为SpringBoot项目设计的幂等组件
    - [`ytyht226/taskflow` ![](https://img.shields.io/github/stars/ytyht226/taskflow.svg?style=social&label=Star)](https://github.com/ytyht226/taskflow)  
      一款轻量、简单易用、可灵活扩展的通用任务编排框架，基于有向无环图(DAG)的方式实现，框架提供了组件复用、同步/异步编排、条件判断、分支选择等能力，可以根据不同的业务场景对任意的业务流程进行编排
    - [`foldright/cffu` ![](https://img.shields.io/github/stars/foldright/cffu.svg?style=social&label=star)](https://github.com/foldright/cffu)  
      🦝 Java CompletableFuture Fu, aka. CF-Fu, pronounced "Shifu"; include best practice/traps guide and a tiny sidekick library to improve user experience and reduce misuse.
    - [`tuya/connector` ![](https://img.shields.io/github/stars/tuya/connector.svg?style=social&label=Star)](https://github.com/tuya/connector)  
      The connector framework maps cloud APIs to local APIs based on simple configurations and flexible extension mechanisms
- **Middleware/Data Processing**
    - [`apache/shardingsphere` ![](https://img.shields.io/github/stars/apache/shardingsphere.svg?style=social&label=Star)](https://github.com/apache/shardingsphere) [![star](https://gitee.com/Sharding-Sphere/sharding-sphere/badge/star.svg?theme=gray)](https://gitee.com/Sharding-Sphere/sharding-sphere)  
      Ecosystem to transform any database into a distributed database system, and enhance it with sharding, elastic scaling, encryption features & more
    - [`apache/kylin` ![](https://img.shields.io/github/stars/apache/kylin.svg?style=social&label=Star)](https://github.com/apache/kylin)  
      A unified and powerful OLAP platform for Hadoop and Cloud.
    - [`mybatis-flex/mybatis-flex` ![](https://img.shields.io/github/stars/mybatis-flex/mybatis-flex.svg?style=social&label=Star)](https://github.com/mybatis-flex/mybatis-flex) [![star](https://gitee.com/mybatis-flex/mybatis-flex/badge/star.svg?theme=gray)](https://gitee.com/mybatis-flex/mybatis-flex)  
      mybatis-flex is an elegant Mybatis Enhancement Framework
    - [`basicai/xtreme1` ![](https://img.shields.io/github/stars/basicai/xtreme1.svg?style=social&label=Star)](https://github.com/basicai/xtreme1)  
      The Next GEN Platform for Multisensory Training Data. #3D annotation, lidar-camera annotation and image annotation tools are supported
    - [`oceanbase/odc` ![](https://img.shields.io/github/stars/oceanbase/odc.svg?style=social&label=Star)](https://github.com/oceanbase/odc)  
      An open-source, enterprise-grade database tool for collaborative development
    - [`sagframe/sagacity-sqltoy` ![](https://img.shields.io/github/stars/sagframe/sagacity-sqltoy.svg?style=social&label=Star)](https://github.com/sagframe/sagacity-sqltoy)  
      Java真正智慧的ORM框架
    - [`dromara/stream-query` ![](https://img.shields.io/github/stars/dromara/stream-query.svg?style=social&label=Star)](https://github.com/dromara/stream-query) [![star](https://gitee.com/dromara/stream-query/badge/star.svg?theme=gray)](https://gitee.com/dromara/stream-query)  
      允许完全摆脱Mapper的mybatis-plus体验；可以使用类似“工具类”这样的静态函数进行数据库操作
    - [`luo-zhan/Transformer` ![](https://img.shields.io/github/stars/luo-zhan/Transformer.svg?style=social&label=Star)](https://github.com/luo-zhan/Transformer)  
      Transformer可能是最简单，但最强大的字段转换插件，一个注解搞定任意转换，让开发变得更加丝滑
    - [`SimonAlong/Neo` ![](https://img.shields.io/github/stars/SimonAlong/Neo.svg?style=social&label=Star)](https://github.com/SimonAlong/Neo)  
      Orm框架：基于ActiveRecord思想开发的至简化且功能很全的Orm框架
    - [`ppdaicorp/das` ![](https://img.shields.io/github/stars/ppdaicorp/das.svg?style=social&label=Star)](https://github.com/ppdaicorp/das)  
      数据库访问框架(data access service)，包括数据库控制台das console，数据库客户端das client和数据库服务端das server三部分
    - [`didi/ALITA` ![](https://img.shields.io/github/stars/didi/ALITA.svg?style=social&label=Star)](https://github.com/didi/ALITA)  
      a layer-based data analysis tool
    - [`didi/daedalus` ![](https://img.shields.io/github/stars/didi/daedalus.svg?style=social&label=Star)](https://github.com/didi/daedalus)  
      实现快速创建数据构造流程，数据构造流程的可视化、线上化、持久化、标准化
- **Middleware/Flow engine**
    - [`dromara/liteflow` ![](https://img.shields.io/github/stars/dromara/liteflow.svg?style=social&label=Star)](https://github.com/dromara/liteflow) [![star](https://gitee.com/dromara/liteFlow/badge/star.svg?theme=gray)](https://gitee.com/dromara/liteFlow)  
      a lightweight and practical micro-process framework
    - [`alibaba/bulbasaur` ![](https://img.shields.io/github/stars/alibaba/bulbasaur.svg?style=social&label=Star)](https://github.com/alibaba/bulbasaur)  
      A pluggable, scalable process engine
- **Middleware/Log**
    - [`dromara/TLog` ![](https://img.shields.io/github/stars/dromara/TLog.svg?style=social&label=Star)](https://github.com/dromara/TLog) [![star](https://gitee.com/dromara/TLog/badge/star.svg?theme=gray)](https://gitee.com/dromara/TLog)  
      Lightweight distributed log label tracking framework
    - [`fayechenlong/plumelog` ![](https://img.shields.io/github/stars/fayechenlong/plumelog.svg?style=social&label=Star)](https://github.com/fayechenlong/plumelog) [![star](https://gitee.com/plumeorg/plumelog/badge/star.svg?theme=gray)](https://gitee.com/plumeorg/plumelog)  
      一个java分布式日志组件，支持百亿级别
    - [`minbox-projects/minbox-logging` ![](https://img.shields.io/github/stars/minbox-projects/minbox-logging.svg?style=social&label=Star)](https://github.com/minbox-projects/minbox-logging) [![star](https://gitee.com/minbox-projects/minbox-logging/badge/star.svg?theme=gray)](https://gitee.com/minbox-projects/minbox-logging)  
      分布式零侵入式、链路式请求日志分析框架。提供Admin端点进行采集日志、分析日志、日志告警通知、服务性能分析等。通过Admin Ui可查看实时链路日志信息、在线业务服务列表
        - [`minbox-projects/api-boot` ![](https://img.shields.io/github/stars/minbox-projects/api-boot.svg?style=social&label=Star)](https://github.com/minbox-projects/api-boot) [![star](https://gitee.com/minbox-projects/api-boot/badge/star.svg?theme=gray)](https://gitee.com/minbox-projects/api-boot)  
          为接口服务而生的，基于“ SpringBoot”完成扩展和自动配置，内部封装了一系列的开箱即用Starters
    - [`ofpay/logback-mdc-ttl` ![](https://img.shields.io/github/stars/ofpay/logback-mdc-ttl.svg?style=social&label=Star)](https://github.com/ofpay/logback-mdc-ttl)  
      logback扩展，集成transmittable-thread-local支持跨线程池的mdc跟踪
    - [`oldratlee/log4j2-ttl-thread-context-map` ![](https://img.shields.io/github/stars/oldratlee/log4j2-ttl-thread-context-map.svg?style=social&label=Star)](https://github.com/oldratlee/log4j2-ttl-thread-context-map)  
      Log4j2 TTL ThreadContextMap, Log4j2 extension integrated TransmittableThreadLocal to MDC
- **Middleware/Bytecode**
    - [`ymm-tech/easy-byte-coder` ![](https://img.shields.io/github/stars/ymm-tech/easy-byte-coder.svg?style=social&label=Star)](https://github.com/ymm-tech/easy-byte-coder)  
      Easy-byte-coder is a non-invasive bytecode injection framework based on JVM
- **Business service or platform application**
    - [`OpenBankProject/OBP-API` ![](https://img.shields.io/github/stars/OpenBankProject/OBP-API.svg?style=social&label=Star)](https://github.com/OpenBankProject/OBP-API)  
      An open source RESTful API platform for banks that supports Open Banking, XS2A and PSD2 through access to accounts, transactions, counterparties, payments, entitlements and metadata - plus a host of internal banking and management APIs
    - [`gz-yami/mall4j` ![](https://img.shields.io/github/stars/gz-yami/mall4j.svg?style=social&label=Star)](https://github.com/gz-yami/mall4j) [![star](https://gitee.com/gz-yami/mall4j/badge/star.svg?theme=gray)](https://gitee.com/gz-yami/mall4j)  
      电商商城 java电商商城系统 uniapp商城 多用户商城
    - [`Joolun/JooLun-wx` ![](https://img.shields.io/github/stars/Joolun/JooLun-wx.svg?style=social&label=Star)](https://github.com/Joolun/JooLun-wx) [![star](https://gitee.com/joolun/JooLun-wx/badge/star.svg?theme=gray)](https://gitee.com/joolun/JooLun-wx)  
      JooLun微信商城
    - [`HummerRisk/HummerRisk` ![](https://img.shields.io/github/stars/HummerRisk/HummerRisk.svg?style=social&label=Star)](https://github.com/HummerRisk/HummerRisk)  
      云原生安全平台，包括混合云安全治理和容器云安全检测
    - [`XiaoMi/mone` ![](https://img.shields.io/github/stars/XiaoMi/mone.svg?style=social&label=Star)](https://github.com/XiaoMi/mone)  
      `Mone`以微服务为核心的一站式企业协同研发平台。支持公共云、专有云和混合云多种部署形态；提供从“项目创建->开发->部署->治理->应用观测”端到端的研发全流程服务；通过云原生新技术和研发新模式，打造“双敏”，敏捷研发和敏捷组织，保障小米-中国区高复杂业务、大规模团队的敏捷研发协同，实现多倍效能提升。
    - [`yangzongzhuan/RuoYi-Cloud` ![](https://img.shields.io/github/stars/yangzongzhuan/RuoYi-Cloud.svg?style=social&label=Star)](https://github.com/yangzongzhuan/RuoYi-Cloud) [![star](https://gitee.com/y_project/RuoYi-Cloud/badge/star.svg?theme=gray)](https://gitee.com/y_project/RuoYi-Cloud)  
      基于Spring Boot、Spring Cloud & Alibaba的分布式微服务架构权限管理系统
    - [`somowhere/albedo` ![](https://img.shields.io/github/stars/somowhere/albedo.svg?style=social&label=Star)](https://github.com/somowhere/albedo) [![star](https://gitee.com/somowhere/albedo/badge/star.svg?theme=gray)](https://gitee.com/somowhere/albedo)  
      基于 Spring Boot 、Spring Security、Mybatis 的RBAC权限管理系统
    - [`qwdigital/LinkWechat` ![](https://img.shields.io/github/stars/qwdigital/LinkWechat.svg?style=social&label=Star)](https://github.com/qwdigital/LinkWechat) [![star](https://gitee.com/LinkWeChat/link-wechat/badge/star.svg?theme=gray)](https://gitee.com/LinkWeChat/link-wechat)  
      基于企业微信的开源 SCRM 系统，采用主流的 Java 微服务架构，是企业私域流量管理与营销的综合解决方案，助力企业提高客户运营效率，强化营销能力，拓展盈利空间
    - [`fushengqian/fuint` ![](https://img.shields.io/github/stars/fushengqian/fuint.svg?style=social&label=Star)](https://github.com/fushengqian/fuint) [![star](https://gitee.com/fuint/fuint-uniapp/badge/star.svg?theme=gray)](https://gitee.com/fuint/fuint-uniapp)  
      fuint会员营销系统是一套开源的实体店铺会员管理和营销系统
    - [`hiparker/opsli-boot` ![](https://img.shields.io/github/stars/hiparker/opsli-boot.svg?style=social&label=Star)](https://github.com/hiparker/opsli-boot) [![star](https://gitee.com/hiparker/opsli-boot/badge/star.svg?theme=gray)](https://gitee.com/hiparker/opsli-boot)  
      一款的低代码快速平台，零代码开发，致力于做更简洁的后台管理系统
    - [`topiam/eiam` ![](https://img.shields.io/github/stars/topiam/eiam.svg?style=social&label=Star)](https://github.com/topiam/eiam) [![star](https://gitee.com/topiam/eiam/badge/star.svg?theme=gray)](https://gitee.com/topiam/eiam)  
      EIAM（Employee Identity and Access Management Program）企业级开源IAM平台，实现用户全生命周期的管理、统一认证和单点登录、为数字身份安全赋能
    - [`Newspiral/newspiral-business` ![](https://img.shields.io/github/stars/Newspiral/newspiral-business.svg?style=social&label=Star)](https://github.com/Newspiral/newspiral-business)  
      联盟区块链底层平台
- **Tool product**
    - [`ssssssss-team/spider-flow` ![](https://img.shields.io/github/stars/ssssssss-team/spider-flow.svg?style=social&label=Star)](https://github.com/ssssssss-team/spider-flow) [![star](https://gitee.com/ssssssss-team/spider-flow/badge/star.svg?theme=gray)](https://gitee.com/ssssssss-team/spider-flow)  
      新一代爬虫平台，以图形化方式定义爬虫流程，不写代码即可完成爬虫
    - [`nekolr/slime` ![](https://img.shields.io/github/stars/nekolr/slime.svg?style=social&label=Star)](https://github.com/nekolr/slime)  
      🍰 一个可视化的爬虫平台
    - [`Jackson0714/PassJava-Platform` ![](https://img.shields.io/github/stars/Jackson0714/PassJava-Platform.svg?style=social&label=Star)](https://github.com/Jackson0714/PassJava-Platform)  
      一款面试刷题的 Spring Cloud 开源系统。零碎时间利用小程序查看常见面试题，夯实Java基础。 该项目可以教会你如何搭建SpringBoot项目，Spring Cloud项目。 采用流行的技术，如 SpringBoot、MyBatis、Redis、 MySql、 MongoDB、 RabbitMQ、Elasticsearch，采用Docker容器化部署
    - [`martin-chips/DimpleBlog` ![](https://img.shields.io/github/stars/martin-chips/DimpleBlog.svg?style=social&label=Star)](https://github.com/martin-chips/DimpleBlog)  
      基于`SpringBoot2`搭建的个人博客系统
    - [`zjcscut/octopus` ![](https://img.shields.io/github/stars/zjcscut/octopus.svg?style=social&label=Star)](https://github.com/zjcscut/octopus)  
      长链接压缩为短链接的服务
    - [`xggz/mqr` ![](https://img.shields.io/github/stars/xggz/mqr.svg?style=social&label=Star)](https://github.com/xggz/mqr) [![star](https://gitee.com/mlyai/mqr/badge/star.svg?theme=gray)](https://gitee.com/mlyai/mqr)  
      茉莉QQ机器人（简称MQR），采用mirai的Android协议实现的QQ机器人服务，通过web控制机器人的启停和配置
- **Test solution or tool**
    - [`alibaba/jvm-sandbox-repeater` ![](https://img.shields.io/github/stars/alibaba/jvm-sandbox-repeater.svg?style=social&label=Star)](https://github.com/alibaba/jvm-sandbox-repeater)  
      A Java server-side recording and playback solution based on JVM-Sandbox, 录制/回放通用解决方案
    - [`vivo/MoonBox` ![](https://img.shields.io/github/stars/vivo/MoonBox.svg?style=social&label=Star)](https://github.com/vivo/MoonBox)  
      Moonbox（月光宝盒）是JVM-Sandbox生态下的，基于jvm-sandbox-repeater重新开发的，一款流量回放平台产品。相较于jvm-sandbox-repeater，Moonbox功能更加丰富、数据可靠性更高，同时便于快速线上部署和使用
    - [`alibaba/testable-mock` ![](https://img.shields.io/github/stars/alibaba/testable-mock.svg?style=social&label=Star)](https://github.com/alibaba/testable-mock)  
      换种思路写Mock，让单元测试更简单
    - [`shulieTech/Takin` ![](https://img.shields.io/github/stars/shulieTech/Takin.svg?style=social&label=Star)](https://github.com/shulieTech/Takin)  
      measure online environmental performance test for full-links, Especially for microservices
        - [`shulieTech/LinkAgent` ![](https://img.shields.io/github/stars/shulieTech/LinkAgent.svg?style=social&label=Star)](https://github.com/shulieTech/LinkAgent)  
          a Java-based open-source agent designed to collect data and control Functions for Java applications through JVM bytecode, without modifying applications codes
    - [`alibaba/virtual-environment` ![](https://img.shields.io/github/stars/alibaba/virtual-environment.svg?style=social&label=Star)](https://github.com/alibaba/virtual-environment)  
      Route isolation with service sharing, 阿里测试环境服务隔离和联调机制的`Kubernetes`版实现
- **`Spring Cloud`/`Spring Boot` microservices framework solution or scaffold**
    - [`YunaiV/ruoyi-vue-pro` ![](https://img.shields.io/github/stars/YunaiV/ruoyi-vue-pro.svg?style=social&label=Star)](https://github.com/YunaiV/ruoyi-vue-pro)  [![star](https://gitee.com/zhijiantianya/ruoyi-vue-pro/badge/star.svg?theme=gray)](https://gitee.com/zhijiantianya/ruoyi-vue-pro)  
      一套全部开源的企业级的快速开发平台。基于 Spring Boot + MyBatis Plus + Vue & Element 实现的后台管理系统 + 微信小程序，支持 RBAC 动态权限、数据权限、SaaS 多租户、Activiti + Flowable 工作流、三方登录、支付、短信、商城等功能
    - [`YunaiV/yudao-cloud` ![](https://img.shields.io/github/stars/YunaiV/yudao-cloud.svg?style=social&label=Star)](https://github.com/YunaiV/yudao-cloud)  [![star](https://gitee.com/zhijiantianya/yudao-cloud/badge/star.svg?theme=gray)](https://gitee.com/zhijiantianya/yudao-cloud)  
      RuoYi-Vue 全新 Cloud 版本，优化重构所有功能。基于 Spring Cloud Alibaba + MyBatis Plus + Vue & Element 实现的后台管理系统 + 用户小程序，支持 RBAC 动态权限、多租户、数据权限、工作流、三方登录、支付、短信、商城等功能
    - [`zlt2000/microservices-platform` ![](https://img.shields.io/github/stars/zlt2000/microservices-platform.svg?style=social&label=Star)](https://github.com/zlt2000/microservices-platform) [![star](https://gitee.com/zlt2000/microservices-platform/badge/star.svg?theme=gray)](https://gitee.com/zlt2000/microservices-platform)  
      基于SpringBoot2.x、SpringCloud和SpringCloudAlibaba并采用前后端分离的企业级微服务多租户系统架构
    - [`dromara/lamp-cloud` ![](https://img.shields.io/github/stars/dromara/lamp-cloud.svg?style=social&label=Star)](https://github.com/zuihou/lamp-cloud) [![star](https://gitee.com/dromara/lamp-cloud/badge/star.svg?theme=gray)](https://gitee.com/dromara/lamp-cloud)  
      基于Jdk11 + SpringCloud + SpringBoot 的微服务快速开发平台，其中的可配置的SaaS功能尤其闪耀， 具备RBAC功能、网关统一鉴权、Xss防跨站攻击、自动代码生成、多种存储系统、分布式事务、分布式定时任务等多个模块，支持多业务系统并行开发， 支持多服务并行开发，可以作为后端服务的开发脚手架
        - [`zuihou/lamp-util` ![](https://img.shields.io/github/stars/zuihou/lamp-util.svg?style=social&label=Star)](https://github.com/zuihou/lamp-util) [![star](https://gitee.com/zuihou111/lamp-util/badge/star.svg?theme=gray)](https://gitee.com/zuihou111/lamp-util)  
          打造一套兼顾 SpringBoot 和 SpringCloud 项目的公共工具类
    - [`matevip/matecloud` ![](https://img.shields.io/github/stars/matevip/matecloud.svg?style=social&label=Star)](https://github.com/matevip/matecloud) [![star](https://gitee.com/matevip/matecloud/badge/star.svg?theme=gray)](https://gitee.com/matevip/matecloud)  
      一款基于Spring Cloud Alibaba的微服务架构
    - [`gavenwangcn/vole` ![](https://img.shields.io/github/stars/gavenwangcn/vole.svg?style=social&label=Star)](https://github.com/gavenwangcn/vole)  
      SpringCloud Micro service business framework
    - [`liuweijw/fw-cloud-framework` ![](https://img.shields.io/github/stars/liuweijw/fw-cloud-framework.svg?style=social&label=Star)](https://github.com/liuweijw/fw-cloud-framework) [![star](https://gitee.com/liuweijw/fw-cloud-framework/badge/star.svg?theme=gray)](https://gitee.com/liuweijw/fw-cloud-framework)  
      基于springcloud全家桶开发分布式框架（支持oauth2认证授权、SSO登录、统一下单、微信公众号服务、Shardingdbc分库分表、常见服务监控、链路监控、异步日志、redis缓存等功能），实现基于Vue全家桶等前后端分离项目工程
    - [`liuht777/Taroco` ![](https://img.shields.io/github/stars/liuht777/Taroco.svg?style=social&label=Star)](https://github.com/liuht777/Taroco)  
      整合Nacos、Spring Cloud Alibaba，提供了一系列starter组件， 同时提供服务治理、服务监控、OAuth2权限认证，支持服务降级/熔断、服务权重
    - [`mingyang66/spring-parent` ![](https://img.shields.io/github/stars/mingyang66/spring-parent.svg?style=social&label=Star)](https://github.com/mingyang66/spring-parent)  
      数据库多数据源、Redis多数据源、日志组件、全链路日志追踪、埋点扩展点、Netty、微服务、开发基础框架支持、异常统一处理、返回值、跨域、API路由、监控等
    - [`budwk/budwk` ![](https://img.shields.io/github/stars/budwk/budwk.svg?style=social&label=Star)](https://github.com/budwk/budwk) [![star](https://gitee.com/budwk/budwk/badge/star.svg?theme=gray)](https://gitee.com/budwk/budwk)  
      `BudWk` 原名 [`NutzWk` ![](https://img.shields.io/github/stars/Wizzercn/NutzWk.svg?style=social&label=Star)](https://github.com/Wizzercn/NutzWk) [![star](https://gitee.com/wizzer/NutzWk/badge/star.svg?theme=gray)](https://gitee.com/wizzer/NutzWk)，基于国产框架 nutz 及 nutzboot 开发的开源Web基础项目，集权限体系、系统参数、数据字典、站内消息、定时任务、CMS、微信等最常用功能，不庞杂、不面面俱到，使其具有上手容易、开发便捷、扩展灵活等特性，特别适合各类大中小型定制化项目需求
    - [`yinjihuan/spring-cloud` ![](https://img.shields.io/github/stars/yinjihuan/spring-cloud.svg?style=social&label=Star)](https://github.com/yinjihuan/spring-cloud)  
      《Spring Cloud微服务-全栈技术与案例解析》和《Spring Cloud微服务 入门 实战与进阶》配套源码
    - [`louyanfeng25/ddd-demo` ![](https://img.shields.io/github/stars/louyanfeng25/ddd-demo.svg?style=social&label=Star)](https://github.com/louyanfeng25/ddd-demo)  
      《深入浅出DDD》讲解的演示项目，为了能够更好的理解Demo中的分层与逻辑处理，我强烈建议你配合小册来深入了解DDD
    - [`nageoffer/12306` ![](https://img.shields.io/github/stars/nageoffer/12306.svg?style=social&label=Star)](https://github.com/nageoffer/12306)  
      12306 铁路购票服务是与大家生活和出行相关的关键系统，包括会员、购票、订单、支付和网关等服务。

more open-source projects used `TTL`, see [![user repos](https://badgen.net/github/dependents-repo/alibaba/transmittable-thread-local?label=user%20repos)](https://github.com/alibaba/transmittable-thread-local/network/dependents)

# 👷 Contributors

- Jerry Lee \<oldratlee at gmail dot com> [@oldratlee](https://github.com/oldratlee)
- Yang Fang \<snoop.fy at gmail dot com> [@driventokill](https://github.com/driventokill)
- Zava Xu \<zava.kid at gmail dot com> [@zavakid](https://github.com/zavakid)
- wuwen \<wuwen.55 at aliyun dot com> [@wuwen5](https://github.com/wuwen5)
- rybalkinsd \<yan.brikl at gmail dot com> [@rybalkinsd](https://github.com/rybalkinsd)
- David Dai \<351450944 at qq dot com> [@LNAmp](https://github.com/LNAmp)
- Your name here :-)

[![GitHub Contributors](https://contrib.rocks/image?repo=alibaba/transmittable-thread-local)](https://github.com/alibaba/transmittable-thread-local/graphs/contributors)
