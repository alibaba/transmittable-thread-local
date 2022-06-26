# <div align="center"><a href="#dummy"><img src="docs/logo-blue.png" alt="📌 TransmittableThreadLocal(TTL)"></a></div>

<p align="center">
<a href="https://github.com/alibaba/transmittable-thread-local/actions/workflows/ci.yaml"><img src="https://img.shields.io/github/workflow/status/alibaba/transmittable-thread-local/CI/master?logo=github&logoColor=white" alt="Github Workflow Build Status"></a>
<a href="https://ci.appveyor.com/project/oldratlee/transmittable-thread-local"><img src="https://img.shields.io/appveyor/ci/oldratlee/transmittable-thread-local/master?logo=appveyor&logoColor=white" alt="Appveyor Build Status"></a>
<a href="https://codecov.io/gh/alibaba/transmittable-thread-local/branch/master"><img src="https://img.shields.io/codecov/c/github/alibaba/transmittable-thread-local/master?logo=codecov&logoColor=white" alt="Coverage Status"></a>
<a href="https://codeclimate.com/github/alibaba/transmittable-thread-local/maintainability"><img src="https://img.shields.io/codeclimate/maintainability/alibaba/transmittable-thread-local?logo=codeclimate&logoColor=white" alt="Maintainability"></a>
<a href="https://openjdk.java.net/"><img src="https://img.shields.io/badge/Java-6+-green?logo=java&logoColor=white" alt="JDK support"></a>
<a href="https://www.apache.org/licenses/LICENSE-2.0.html"><img src="https://img.shields.io/github/license/alibaba/transmittable-thread-local?color=4D7A97&logo=apache" alt="License"></a>
<a href="https://alibaba.github.io/transmittable-thread-local/apidocs/"><img src="https://img.shields.io/github/release/alibaba/transmittable-thread-local?label=javadoc&color=3d7c47&logo=microsoft-academic&logoColor=white" alt="Javadocs"></a>
<a href="https://search.maven.org/artifact/com.alibaba/transmittable-thread-local"><img src="https://img.shields.io/maven-central/v/com.alibaba/transmittable-thread-local?color=2d545e&logo=apache-maven&logoColor=white" alt="Maven Central"></a>
<a href="https://github.com/alibaba/transmittable-thread-local/releases"><img src="https://img.shields.io/github/release/alibaba/transmittable-thread-local" alt="GitHub release"></a>
<a href="https://github.com/alibaba/transmittable-thread-local/stargazers"><img src="https://img.shields.io/github/stars/alibaba/transmittable-thread-local" alt="GitHub Stars"></a>
<a href="https://github.com/alibaba/transmittable-thread-local/fork"><img src="https://img.shields.io/github/forks/alibaba/transmittable-thread-local" alt="GitHub Forks"></a>
<a href="https://github.com/alibaba/transmittable-thread-local/network/dependents"><img src="https://badgen.net/github/dependents-repo/alibaba/transmittable-thread-local?label=user%20repos" alt="user repos"></a>
<a href="https://github.com/alibaba/transmittable-thread-local/issues"><img src="https://img.shields.io/github/issues/alibaba/transmittable-thread-local" alt="GitHub issues"></a>
<a href="https://github.com/alibaba/transmittable-thread-local/graphs/contributors"><img src="https://img.shields.io/github/contributors/alibaba/transmittable-thread-local" alt="GitHub Contributors"></a>
<a href="https://github.com/alibaba/transmittable-thread-local"><img src="https://img.shields.io/github/repo-size/alibaba/transmittable-thread-local" alt="GitHub repo size"></a>
<a href="https://gitpod.io/#https://github.com/alibaba/transmittable-thread-local"><img src="https://img.shields.io/badge/Gitpod-ready--to--code-green?label=gitpod&logo=gitpod&logoColor=white" alt="gitpod: Ready to Code"></a>
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
- [💝 Who used](#-who-used)
- [👷 Contributors](#-contributors)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

----------------------------------------

# 🔧 Functions

👉 `TransmittableThreadLocal`(`TTL`): The missing Java™ std lib(simple & 0-dependency) for framework/middleware,
provide an enhanced `InheritableThreadLocal` that transmits values between threads even using thread pooling components. Support `Java 6~19`.

Class [`InheritableThreadLocal`](https://docs.oracle.com/javase/10/docs/api/java/lang/InheritableThreadLocal.html) in `JDK`
can transmit value to child thread from parent thread.

But when use thread pool, thread is cached up and used repeatedly. Transmitting value from parent thread to child thread has no meaning.
Application need transmit value from the time task is created to the time task is executed.

If you have problem or question, please [submit Issue](https://github.com/alibaba/transmittable-thread-local/issues) or play [fork](https://github.com/alibaba/transmittable-thread-local/fork) and pull request dance.

> From `TTL v2.13+` upgrade to `Java 8`.  
> If you need `Java 6` support, use version `2.12.x` <a href="https://search.maven.org/artifact/com.alibaba/transmittable-thread-local"><img src="https://img.shields.io/maven-central/v/com.alibaba/transmittable-thread-local?versionPrefix=2.12.&color=lightgrey&logo=apache-maven&logoColor=white" alt="Maven Central"></a>

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

\# See the executable demo [`SimpleDemo.kt`](src/test/java/com/alibaba/demo/ttl/SimpleDemo.kt) with full source code.

This is the function of class `InheritableThreadLocal`, should use class `InheritableThreadLocal` instead.

But when use thread pool, thread is cached up and used repeatedly. Transmitting value from parent thread to child thread has no meaning.
Application need transmit value from the time task is created to the point task is executed.

The solution is below usage.

## 2. Transmit value even using thread pool

### 2.1 Decorate `Runnable` and `Callable`

Decorate input `Runnable` and `Callable` by [`TtlRunnable`](/src/main/java/com/alibaba/ttl/TtlRunnable.java)
and [`TtlCallable`](src/main/java/com/alibaba/ttl/TtlCallable.java).

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
Even when the same `Runnable` task is submitted to the thread pool multiple times, the decoration operation (ie： `TtlRunnable.get(task)`) is required for each submission to capture the value of the `TransmittableThreadLocal` context at submission time; That is, if the same task is submitted next time without reperforming decoration and still using the last `TtlRunnable`, the submitted task will run in the context of the last captured context. The sample code is as follows:


```java
// first submission
Runnable task = new RunnableTask();
executorService.submit(TtlRunnable.get(task));

// ... some biz logic,
// and modified TransmittableThreadLocal context ...
// context.set("value-modified-in-parent");

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

\# See the executable demo [`TtlWrapperDemo.kt`](src/test/java/com/alibaba/demo/ttl/TtlWrapperDemo.kt) with full source code.

### 2.2 Decorate thread pool

Eliminating the work of `Runnable` and `Callable` Decoration every time it is submitted to thread pool. This work can be completed in the thread pool.

Use util class
[`com.alibaba.ttl.threadpool.TtlExecutors`](src/main/java/com/alibaba/ttl/threadpool/TtlExecutors.java)
to decorate thread pool.

Util class `com.alibaba.ttl.threadpool.TtlExecutors` has below methods:

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

\# See the executable demo [`TtlExecutorWrapperDemo.kt`](src/test/java/com/alibaba/demo/ttl/TtlExecutorWrapperDemo.kt) with full source code.

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

\# See the executable demo [`AgentDemo.kt`](src/test/java/com/alibaba/demo/ttl/agent/AgentDemo.kt) with full source code, run demo by the script [`scripts/run-agent-demo.sh`](scripts/run-agent-demo.sh).

At present, `TTL` agent has decorated below `JDK` execution components(aka. thread pool) implementation:

- `java.util.concurrent.ThreadPoolExecutor` and `java.util.concurrent.ScheduledThreadPoolExecutor`
    - decoration implementation code is in [`JdkExecutorTtlTransformlet.java`](src/main/java/com/alibaba/ttl/threadpool/agent/transformlet/internal/JdkExecutorTtlTransformlet.java).
- `java.util.concurrent.ForkJoinTask`（corresponding execution component is `java.util.concurrent.ForkJoinPool`）
    - decoration implementation code is in [`ForkJoinTtlTransformlet.java`](src/main/java/com/alibaba/ttl/threadpool/agent/transformlet/internal/ForkJoinTtlTransformlet.java), supports since version **_`2.5.1`_**.
    - **_NOTE_**: [**_`CompletableFuture`_**](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/CompletableFuture.html) and (parallel) [**_`Stream`_**](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/stream/package-summary.html) introduced in Java 8 is executed through `ForkJoinPool` underneath, so after supporting `ForkJoinPool`, `TTL` also supports `CompletableFuture` and `Stream` transparently. 🎉
- `java.util.TimerTask`（corresponding execution component is `java.util.Timer`）
    - decoration implementation code is in [`TimerTaskTtlTransformlet.java`](src/main/java/com/alibaba/ttl/threadpool/agent/transformlet/internal/TimerTaskTtlTransformlet.java), supports since version **_`2.7.0`_**.
    - **_NOTE_**: Since version `2.11.2` decoration for `TimerTask` default is enable (because correctness is first concern, not the best practice like "It is not recommended to use `TimerTask`" :); before version `2.11.1` default is disable.
    - enabled/disable by agent argument `ttl.agent.enable.timer.task`:
        - `-javaagent:path/to/transmittable-thread-local-2.x.y.jar=ttl.agent.enable.timer.task:true`
        - `-javaagent:path/to/transmittable-thread-local-2.x.y.jar=ttl.agent.enable.timer.task:false`
    - more info about `TTL` agent arguments, see [the javadoc of `TtlAgent.java`](src/main/java/com/alibaba/ttl/threadpool/agent/TtlAgent.java).

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

> `Boot-Class-Path`
>
> A list of paths to be searched by the bootstrap class loader. Paths represent directories or libraries (commonly referred to as JAR or zip libraries on many platforms).
> These paths are searched by the bootstrap class loader after the platform specific mechanisms of locating a class have failed. Paths are searched in the order listed.

More info:

- [`Java Agent Specification` - `JavaDoc`文档](https://docs.oracle.com/javase/10/docs/api/java/lang/instrument/package-summary.html#package.description)
- [JAR File Specification - JAR Manifest](https://docs.oracle.com/javase/10/docs/specs/jar/jar.html#jar-manifest)
- [Working with Manifest Files - The Java™ Tutorials](https://docs.oracle.com/javase/tutorial/deployment/jar/manifestindex.html)

# 🔌 Java API Docs

The current version Java API documentation: <https://alibaba.github.io/transmittable-thread-local/apidocs/>

# 🍪 Maven Dependency

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>transmittable-thread-local</artifactId>
    <version>2.13.0</version>
</dependency>
```

Check available version at [search.maven.org](https://search.maven.org/artifact/com.alibaba/transmittable-thread-local).

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

- [WeakHashMap](https://docs.oracle.com/javase/10/docs/api/java/util/WeakHashMap.html)
- [InheritableThreadLocal](https://docs.oracle.com/javase/10/docs/api/java/lang/InheritableThreadLocal.html)

# 💝 Who used

Some open-source projects used `TTL`:

- **Middleware**
    - [`sofastack/sofa-rpc` ![](https://img.shields.io/github/stars/sofastack/sofa-rpc.svg?style=social&label=Star)](https://github.com/sofastack/sofa-rpc) [![star](https://gitee.com/sofastack/sofa-rpc/badge/star.svg?theme=gray)](https://gitee.com/sofastack/sofa-rpc)  
      SOFARPC is a high-performance, high-extensibility, production-level Java RPC framework
    - [`dromara/hmily` ![](https://img.shields.io/github/stars/dromara/hmily.svg?style=social&label=Star)](https://github.com/dromara/hmily) [![star](https://gitee.com/dromara/hmily/badge/star.svg?theme=gray)](https://gitee.com/dromara/hmily)  
      Distributed transaction solutions
    - [`dromara/dynamic-tp` ![](https://img.shields.io/github/stars/dromara/dynamic-tp.svg?style=social&label=Star)](https://github.com/dromara/dynamic-tp) [![star](https://gitee.com/dromara/dynamic-tp/badge/star.svg?theme=gray)](https://gitee.com/dromara/dynamic-tp)  
      Lightweight dynamic threadpool, with monitoring and alarming functions, base on popular config centers (already support Nacos、Apollo、Zookeeper, can be customized through SPI)
    - [`mabaiwan/hippo4j` ![](https://img.shields.io/github/stars/mabaiwan/hippo4j.svg?style=social&label=Star)](https://github.com/mabaiwan/hippo4j) [![star](https://gitee.com/mabaiwancn/hippo4j/badge/star.svg?theme=gray)](https://gitee.com/mabaiwancn/hippo4j)  
      Powerful dynamic thread pool, does not rely on any middleware, with monitoring and alarm function
    - [`siaorg/sia-gateway` ![](https://img.shields.io/github/stars/siaorg/sia-gateway.svg?style=social&label=Star)](https://github.com/siaorg/sia-gateway)  
      microservice route gateway(zuul-plus)
    - [`huaweicloud/Sermant` ![](https://img.shields.io/github/stars/huaweicloud/Sermant.svg?style=social&label=Star)](https://github.com/huaweicloud/Sermant)  
      Sermant, a proxyless service mesh solution based on Javaagent.
    - [`ZTO-Express/zms` ![](https://img.shields.io/github/stars/ZTO-Express/zms.svg?style=social&label=Star)](https://github.com/ZTO-Express/zms) [![star](https://gitee.com/zto_express/zms/badge/star.svg?theme=gray)](https://gitee.com/zto_express/zms)  
      ZTO Message Service
    - [`tuya/connector` ![](https://img.shields.io/github/stars/tuya/connector.svg?style=social&label=Star)](https://github.com/tuya/connector)  
      The connector framework maps cloud APIs to local APIs based on simple configurations and flexible extension mechanisms.
- **Middleware/Data**
    - [`ppdaicorp/das` ![](https://img.shields.io/github/stars/ppdaicorp/das.svg?style=social&label=Star)](https://github.com/ppdaicorp/das)  
      数据库访问框架(data access service)，包括数据库控制台das console，数据库客户端das client和数据库服务端das server三部分
    - [`SimonAlong/Neo` ![](https://img.shields.io/github/stars/SimonAlong/Neo.svg?style=social&label=Star)](https://github.com/SimonAlong/Neo)  
      Orm框架：基于ActiveRecord思想开发的至简化且功能很全的Orm框架
    - [`didi/ALITA` ![](https://img.shields.io/github/stars/didi/ALITA.svg?style=social&label=Star)](https://github.com/didi/ALITA)  
      a layer-based data analysis tool
    - [`didi/daedalus` ![](https://img.shields.io/github/stars/didi/daedalus.svg?style=social&label=Star)](https://github.com/didi/daedalus)  
      实现快速创建数据构造流程，数据构造流程的可视化、线上化、持久化、标准化
    - [`aiwenmo/DataLink` ![](https://img.shields.io/github/stars/aiwenmo/DataLink.svg?style=social&label=Star)](https://github.com/aiwenmo/DataLink)  
      a new open source solution to bring Flink development to data center
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
    - [`Joolun/JooLun-wx` ![](https://img.shields.io/github/stars/Joolun/JooLun-wx.svg?style=social&label=Star)](https://github.com/Joolun/JooLun-wx) [![star](https://gitee.com/joolun/JooLun-wx/badge/star.svg?theme=gray)](https://gitee.com/joolun/JooLun-wx)  
      JooLun微信商城
    - [`gz-yami/mall4j` ![](https://img.shields.io/github/stars/gz-yami/mall4j.svg?style=social&label=Star)](https://github.com/gz-yami/mall4j) [![star](https://gitee.com/gz-yami/mall4j/badge/star.svg?theme=gray)](https://gitee.com/gz-yami/mall4j)  
      电商商城 java电商商城系统 uniapp商城 多用户商城
    - [`yangzongzhuan/RuoYi-Cloud` ![](https://img.shields.io/github/stars/yangzongzhuan/RuoYi-Cloud.svg?style=social&label=Star)](https://github.com/yangzongzhuan/RuoYi-Cloud) [![star](https://gitee.com/y_project/RuoYi-Cloud/badge/star.svg?theme=gray)](https://gitee.com/y_project/RuoYi-Cloud)  
      基于Spring Boot、Spring Cloud & Alibaba的分布式微服务架构权限管理系统
    - [`somowhere/albedo` ![](https://img.shields.io/github/stars/somowhere/albedo.svg?style=social&label=Star)](https://github.com/somowhere/albedo) [![star](https://gitee.com/somowhere/albedo/badge/star.svg?theme=gray)](https://gitee.com/somowhere/albedo)  
      基于 Spring Boot 、Spring Security、Mybatis 的RBAC权限管理系统
    - [`hiparker/opsli-boot` ![](https://img.shields.io/github/stars/hiparker/opsli-boot.svg?style=social&label=Star)](https://github.com/hiparker/opsli-boot)  
      一款的低代码快速平台，零代码开发，致力于做更简洁的后台管理系统
    - [`tengshe789/SpringCloud-miaosha` ![](https://img.shields.io/github/stars/tengshe789/SpringCloud-miaosha.svg?style=social&label=Star)](https://github.com/tengshe789/SpringCloud-miaosha)  
      一个基于spring cloud Greenwich的简单秒杀电子商城项目
- **Tool product**
    - [`ssssssss-team/spider-flow` ![](https://img.shields.io/github/stars/ssssssss-team/spider-flow.svg?style=social&label=Star)](https://github.com/ssssssss-team/spider-flow) [![star](https://gitee.com/ssssssss-team/spider-flow/badge/star.svg?theme=gray)](https://gitee.com/ssssssss-team/spider-flow)  
      新一代爬虫平台，以图形化方式定义爬虫流程，不写代码即可完成爬虫
    - [`nekolr/slime` ![](https://img.shields.io/github/stars/nekolr/slime.svg?style=social&label=Star)](https://github.com/nekolr/slime)  
      🍰 一个可视化的爬虫平台
    - [`zjcscut/octopus` ![](https://img.shields.io/github/stars/zjcscut/octopus.svg?style=social&label=Star)](https://github.com/zjcscut/octopus)  
      长链接压缩为短链接的服务
    - [`xggz/mqr` ![](https://img.shields.io/github/stars/xggz/mqr.svg?style=social&label=Star)](https://github.com/xggz/mqr) [![star](https://gitee.com/molicloud/mqr/badge/star.svg?theme=gray)](https://gitee.com/molicloud/mqr)  
      茉莉QQ机器人（简称MQR），采用mirai的Android协议实现的QQ机器人服务，通过web控制机器人的启停和配置
- **Test solution or tool**
    - [`alibaba/jvm-sandbox-repeater` ![](https://img.shields.io/github/stars/alibaba/jvm-sandbox-repeater.svg?style=social&label=Star)](https://github.com/alibaba/jvm-sandbox-repeater)  
      A Java server-side recording and playback solution based on JVM-Sandbox, 录制/回放通用解决方案
    - [`alibaba/testable-mock` ![](https://img.shields.io/github/stars/alibaba/testable-mock.svg?style=social&label=Star)](https://github.com/alibaba/testable-mock)  
      换种思路写Mock，让单元测试更简单
    - [`shulieTech/Takin` ![](https://img.shields.io/github/stars/shulieTech/Takin.svg?style=social&label=Star)](https://github.com/shulieTech/Takin)  
      measure online environmental performance test for full-links, Especially for microservices
        - [`shulieTech/LinkAgent` ![](https://img.shields.io/github/stars/shulieTech/LinkAgent.svg?style=social&label=Star)](https://github.com/shulieTech/LinkAgent)  
          a Java-based open-source agent designed to collect data and control Functions for Java applications through JVM bytecode, without modifying applications codes
    - [`alibaba/virtual-environment` ![](https://img.shields.io/github/stars/alibaba/virtual-environment.svg?style=social&label=Star)](https://github.com/alibaba/virtual-environment)  
      Route isolation with service sharing, 阿里测试环境服务隔离和联调机制的`Kubernetes`版实现
- **`Spring Cloud`/`Spring Boot` microservices framework solution or scaffold**
    - [`zlt2000/microservices-platform` ![](https://img.shields.io/github/stars/zlt2000/microservices-platform.svg?style=social&label=Star)](https://github.com/zlt2000/microservices-platform) [![star](https://gitee.com/zlt2000/microservices-platform/badge/star.svg?theme=gray)](https://gitee.com/zlt2000/microservices-platform)  
      基于SpringBoot2.x、SpringCloud和SpringCloudAlibaba并采用前后端分离的企业级微服务多租户系统架构
    - [`zuihou/lamp-cloud` ![](https://img.shields.io/github/stars/zuihou/lamp-cloud.svg?style=social&label=Star)](https://github.com/zuihou/lamp-cloud) [![star](https://gitee.com/zuihou111/lamp-cloud/badge/star.svg?theme=gray)](https://gitee.com/zuihou111/lamp-cloud)  
      基于Jdk11 + SpringCloud + SpringBoot 的微服务快速开发平台，其中的可配置的SaaS功能尤其闪耀， 具备RBAC功能、网关统一鉴权、Xss防跨站攻击、自动代码生成、多种存储系统、分布式事务、分布式定时任务等多个模块，支持多业务系统并行开发， 支持多服务并行开发，可以作为后端服务的开发脚手架
        - [`zuihou/lamp-util` ![](https://img.shields.io/github/stars/zuihou/lamp-util.svg?style=social&label=Star)](https://github.com/zuihou/lamp-util) [![star](https://gitee.com/zuihou111/lamp-util/badge/star.svg?theme=gray)](https://gitee.com/zuihou111/lamp-util)  
          打造一套兼顾 SpringBoot 和 SpringCloud 项目的公共工具类
    - [`YunaiV/ruoyi-vue-pro` ![](https://img.shields.io/github/stars/YunaiV/ruoyi-vue-pro.svg?style=social&label=Star)](https://github.com/YunaiV/ruoyi-vue-pro)  [![star](https://gitee.com/zhijiantianya/ruoyi-vue-pro/badge/star.svg?theme=gray)](https://gitee.com/zhijiantianya/ruoyi-vue-pro)  
      一套全部开源的企业级的快速开发平台。基于 Spring Boot + MyBatis Plus + Vue & Element 实现的后台管理系统 + 微信小程序，支持 RBAC 动态权限、数据权限、SaaS 多租户、Activiti + Flowable 工作流、三方登录、支付、短信、商城等功能。
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
    - [`yzcheng90/ms` ![](https://img.shields.io/github/stars/yzcheng90/ms.svg?style=social&label=Star)](https://github.com/yzcheng90/ms)  
      一个前后分离的分布式 spring cloud 框架(全家桶)，这里有统一认证，统一网关等等功能，是一个非常简洁的微服务脚手架
    - [`fafeidou/fast-cloud-nacos` ![](https://img.shields.io/github/stars/fafeidou/fast-cloud-nacos.svg?style=social&label=Star)](https://github.com/fafeidou/fast-cloud-nacos)  
      致力于打造一个基于nacos为注册中心，结合企业开发习惯，总结的一些基本的实现方式
    - [`HongZhaoHua/jstarcraft-core` ![](https://img.shields.io/github/stars/HongZhaoHua/jstarcraft-core.svg?style=social&label=Star)](https://github.com/HongZhaoHua/jstarcraft-core)  
      目标是提供一个通用的Java核心编程框架,作为搭建其它框架或者项目的基础. 让相关领域的研发人员能够专注高层设计而不用关注底层实现. 涵盖了缓存,编解码,通讯,事件,输入/输出,监控,存储,配置,脚本和事务10个方面
    - [`budwk/budwk` ![](https://img.shields.io/github/stars/budwk/budwk.svg?style=social&label=Star)](https://github.com/budwk/budwk) [![star](https://gitee.com/budwk/budwk/badge/star.svg?theme=gray)](https://gitee.com/budwk/budwk)  
      `BudWk` 原名 [`NutzWk` ![](https://img.shields.io/github/stars/Wizzercn/NutzWk.svg?style=social&label=Star)](https://github.com/Wizzercn/NutzWk) [![star](https://gitee.com/wizzer/NutzWk/badge/star.svg?theme=gray)](https://gitee.com/wizzer/NutzWk)，基于国产框架 nutz 及 nutzboot 开发的开源Web基础项目，集权限体系、系统参数、数据字典、站内消息、定时任务、CMS、微信等最常用功能，不庞杂、不面面俱到，使其具有上手容易、开发便捷、扩展灵活等特性，特别适合各类大中小型定制化项目需求
    - [`yinjihuan/spring-cloud` ![](https://img.shields.io/github/stars/yinjihuan/spring-cloud.svg?style=social&label=Star)](https://github.com/yinjihuan/spring-cloud)  
      《Spring Cloud微服务-全栈技术与案例解析》和《Spring Cloud微服务 入门 实战与进阶》配套源码
    - [`louyanfeng25/ddd-demo` ![](https://img.shields.io/github/stars/louyanfeng25/ddd-demo.svg?style=social&label=Star)](https://github.com/louyanfeng25/ddd-demo)  
      《深入浅出DDD》讲解的演示项目，为了能够更好的理解Demo中的分层与逻辑处理，我强烈建议你配合小册来深入了解DDD。

more open-source projects used `TTL`, see [![user repos](https://badgen.net/github/dependents-repo/alibaba/transmittable-thread-local?label=user%20repos)](https://github.com/alibaba/transmittable-thread-local/network/dependents)

# 👷 Contributors

- Jerry Lee \<oldratlee at gmail dot com> [@oldratlee](https://github.com/oldratlee)
- Yang Fang \<snoop.fy at gmail dot com> [@driventokill](https://github.com/driventokill)
- Zava Xu \<zava.kid at gmail dot com> [@zavakid](https://github.com/zavakid)
- wuwen \<wuwen.55 at aliyun dot com> [@wuwen5](https://github.com/wuwen5)
- Xiaowei Shi \<179969622 at qq dot com> [@xwshiustc](https://github.com/xwshiustc)
- David Dai \<351450944 at qq dot com> [@LNAmp](https://github.com/LNAmp)
- Your name here :-)

[![GitHub Contributors](https://contrib.rocks/image?repo=alibaba/transmittable-thread-local)](https://github.com/alibaba/transmittable-thread-local/graphs/contributors)
