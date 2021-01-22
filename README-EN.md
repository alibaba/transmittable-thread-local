# 📌 TransmittableThreadLocal(TTL) 📌

[![Build Status](https://img.shields.io/travis/alibaba/transmittable-thread-local/master?logo=travis-ci&logoColor=white)](https://travis-ci.org/alibaba/transmittable-thread-local)
[![Windows Build Status](https://img.shields.io/appveyor/ci/oldratlee/transmittable-thread-local/master?label=windows%20build&logo=appveyor&logoColor=white)](https://ci.appveyor.com/project/oldratlee/transmittable-thread-local)
[![Coverage Status](https://img.shields.io/codecov/c/github/alibaba/transmittable-thread-local/master?logo=codecov&logoColor=white)](https://codecov.io/gh/alibaba/transmittable-thread-local/branch/master)
[![Maintainability](https://badgen.net/codeclimate/maintainability/codeclimate/codeclimate?icon=codeclimate)](https://codeclimate.com/github/alibaba/transmittable-thread-local)  
[![License](https://img.shields.io/github/license/alibaba/transmittable-thread-local?color=4EB1BA)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Javadocs](https://img.shields.io/github/release/alibaba/transmittable-thread-local?label=javadoc&color=3d7c47&logo=microsoft-academic&logoColor=white)](https://alibaba.github.io/transmittable-thread-local/apidocs/)
[![Maven Central](https://img.shields.io/maven-central/v/com.alibaba/transmittable-thread-local?color=2d545e&logo=apache-maven&logoColor=white)](https://search.maven.org/search?q=g:com.alibaba%20AND%20a:transmittable-thread-local&core=gav)
[![GitHub release](https://img.shields.io/github/release/alibaba/transmittable-thread-local) ![JDK support](https://img.shields.io/badge/JDK-6+-green?logo=java&logoColor=white)](https://github.com/alibaba/transmittable-thread-local/releases)  
[![Chat at gitter.im](https://img.shields.io/gitter/room/alibaba/transmittable-thread-local?color=46BC99&logo=gitter&logoColor=white)](https://gitter.im/alibaba/transmittable-thread-local?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![GitHub Stars](https://img.shields.io/github/stars/alibaba/transmittable-thread-local)](https://github.com/alibaba/transmittable-thread-local/stargazers)
[![GitHub Forks](https://img.shields.io/github/forks/alibaba/transmittable-thread-local)](https://github.com/alibaba/transmittable-thread-local/fork)
[![GitHub repo dependents](https://badgen.net/github/dependents-repo/alibaba/transmittable-thread-local)](https://github.com/alibaba/transmittable-thread-local/network/dependents)
[![GitHub issues](https://img.shields.io/github/issues/alibaba/transmittable-thread-local)](https://github.com/alibaba/transmittable-thread-local/issues)
[![GitHub Contributors](https://img.shields.io/github/contributors/alibaba/transmittable-thread-local)](https://github.com/alibaba/transmittable-thread-local/graphs/contributors)

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
    - [3. Transmit value even in async io](#3-Transmit-value-even-in-async-io)
        - [3.1 callback in vert.x framework](#31-callback-in-vertx-framework)
            - [Decorate `io.vertx.core.Handler`](#Decorate-iovertxcoreHandler)
    - [4. Use Java Agent](#4-Use-Java-Agent)
        - [4.1 Use Java Agent to decorate thread pool implementation class](#41-use-java-agent-to-decorate-thread-pool-implementation-class)
        - [4.2 decorate `io.vertx.core.Future`](#42-decorate-iovertxcoreFuture)
        - [4.3 about java BootClassPath](#43-about-java-BootClassPath)
        - [4.4 Java command example](#44-Java-command-example)
- [🔌 Java API Docs](#-java-api-docs)
- [🍪 Maven Dependency](#-maven-dependency)
- [🔨 About compilation, build and dev](#-about-compilation-build-and-dev)
    - [How to compile and build](#how-to-compile-and-build)
    - [How to development by `IDE`](#how-to-development-by-ide)
- [🗿 More Documentation](#-more-documentation)
- [📚 Related Resources](#-related-resources)
    - [JDK Core Classes](#jdk-core-classes)
- [👷 Contributors](#-contributors)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

----------------------------------------

# 🔧 Functions

👉 The missing Java™ std lib(simple & 0-dependency) for framework/middleware,
provide an enhanced `InheritableThreadLocal` that transmits `ThreadLocal` value between threads even using thread pooling components.
Support `Java` 17/16/15/14/13/12/11/10/9/8/7/6.

Class [`InheritableThreadLocal`](https://docs.oracle.com/javase/10/docs/api/java/lang/InheritableThreadLocal.html) in `JDK`
can transmit value to child thread from parent thread.

But when use thread pool, thread is cached up and used repeatedly. Transmitting value from parent thread to child thread has no meaning.
Application need transmit value from the time task is created to the time task is executed.

If you have problem or question, please [submit Issue](https://github.com/alibaba/transmittable-thread-local/issues) or play [fork](https://github.com/alibaba/transmittable-thread-local/fork) and pull request dance.

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
context("value-set-in-parent");

Runnable task = new RunnableTask();
// extra work, create decorated ttlRunnable object
Runnable ttlRunnable = TtlRunnable.get(task);
executorService.submit(ttlRunnable);

// =====================================================

// read in task, value is "value-set-in-parent"
String value = context.get();
```

above code show how to dealing with `Runnable`, `Callable` is similar:

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

Eliminating the work of `Runnable` and `Callable` Decoration every time it is submitted to thread pool. This work can completed in the thread pool.

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

## 3. Transmit value even in async io

### 3.1 callback in vert.x framework

#### Decorate `io.vertx.core.Handler`

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

## 4 Use Java Agent

### 4.1 Use Java Agent to decorate thread pool implementation class

In this usage, transmission is transparent\(no decoration operation\).

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
    - decoration implementation code is in [`TtlExecutorTransformlet.java`](src/main/java/com/alibaba/ttl/threadpool/agent/internal/transformlet/impl/TtlExecutorTransformlet.java).
- `java.util.concurrent.ForkJoinTask`（corresponding execution component is `java.util.concurrent.ForkJoinPool`）
    - decoration implementation code is in [`TtlForkJoinTransformlet.java`](src/main/java/com/alibaba/ttl/threadpool/agent/internal/transformlet/impl/TtlForkJoinTransformlet.java), supports since version **_`2.5.1`_**.
    - **_NOTE_**: [**_`CompletableFuture`_**](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/CompletableFuture.html) and (parallel) [**_`Stream`_**](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/stream/package-summary.html) introduced in Java 8 is executed through `ForkJoinPool` underneath, so after supporting `ForkJoinPool`, `TTL` also supports `CompletableFuture` and `Stream` transparently. 🎉
- `java.util.TimerTask`（corresponding execution component is `java.util.Timer`）
    - decoration implementation code is in [`TtlTimerTaskTransformlet.java`](src/main/java/com/alibaba/ttl/threadpool/agent/internal/transformlet/impl/TtlTimerTaskTransformlet.java), supports since version **_`2.7.0`_**.
    - **_NOTE_**: Since version `2.11.2` decoration for `TimerTask` default is enable (because correctness is first concern, not the best practice like "It is not recommended to use `TimerTask`" :); before version `2.11.1` default is disable.
    - enabled/disable by agent argument `ttl.agent.enable.timer.task`:
        - `-javaagent:path/to/transmittable-thread-local-2.x.y.jar=ttl.agent.enable.timer.task:true`
        - `-javaagent:path/to/transmittable-thread-local-2.x.y.jar=ttl.agent.enable.timer.task:false`
    - more info about `TTL` agent arguments, see [the javadoc of `TtlAgent.java`](src/main/java/com/alibaba/ttl/threadpool/agent/TtlAgent.java).

Add start options on Java command:

- `-javaagent:path/to/transmittable-thread-local-2.x.x.jar`

### 4.2 decorate `io.vertx.core.Future`

At present, `TTL` agent has decorated below `Vertx` callback components(`io.vertx.core.Future`) implementation:

- `io.vertx.core.Future` 
    - decoration implementation code is in [`TtlVertxFutureTransformlet.java`](src/main/java/com/alibaba/ttl/threadpool/agent/internal/transformlet/impl/TtlVertxFutureTransformlet.java)。

### 4.3 about java BootClassPath
**NOTE**：

- Because TTL agent modified the `JDK` std lib classes, make code refer from std lib class to the TTL classes, so the TTL Agent jar must be added to `boot classpath`.
- Since `v2.6.0`, TTL agent jar will auto add self to `boot classpath`. But you **should _NOT_** modify the downloaded TTL jar file name in the maven repo(eg: `transmittable-thread-local-2.x.x.jar`).
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
- [Working with Manifest Files - The Java™ TutorialsHide](https://docs.oracle.com/javase/tutorial/deployment/jar/manifestindex.html)

### 4.4 Java command example

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
    <version>2.12.0</version>
</dependency>
```

Check available version at [search.maven.org](https://search.maven.org/search?q=g:com.alibaba%20AND%20a:transmittable-thread-local&core=gav).

# 🔨 About compilation, build and dev

## How to compile and build

Compilation/build environment require **_`JDK 8~11`_**; Compilation can be performed in the normal way of `Maven`.

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

## How to development by `IDE`

If you use `IDE` to develop (such as `IntelliJ IDEA`), note that:
open **_the `pom4ide.xml` file in the root directory of the project_** instead of `pom.xml` via `IDE`;
To avoid `IDE` complain using `JDK 8` standard library classes not found.

The reason that `IDE` support is not good / have to change a `POM` file, is:  
The code implementation of `TTL` uses the `JDK 8` standard library class, but it is compiled into a `Java 6` version class files.

# 🗿 More Documentation

- [🎓 Developer Guide](docs/developer-guide-en.md)

# 📚 Related Resources

## JDK Core Classes

- [WeakHashMap](https://docs.oracle.com/javase/10/docs/api/java/util/WeakHashMap.html)
- [InheritableThreadLocal](https://docs.oracle.com/javase/10/docs/api/java/lang/InheritableThreadLocal.html)

# 👷 Contributors

- Jerry Lee \<oldratlee at gmail dot com> [@oldratlee](https://github.com/oldratlee)
- Yang Fang \<snoop.fy at gmail dot com> [@driventokill](https://github.com/driventokill)
- Zava Xu \<zava.kid at gmail dot com> [@zavakid](https://github.com/zavakid)
- wuwen \<wuwen.55 at aliyun dot com> [@wuwen5](https://github.com/wuwen5)
- Xiaowei Shi \<179969622 at qq dot com>  [@xwshiustc](https://github.com/xwshiustc)
- David Dai \<351450944 at qq dot com> [@LNAmp](https://github.com/LNAmp)
- Your name here :-)
