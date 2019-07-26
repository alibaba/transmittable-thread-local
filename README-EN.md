# 📌 Transmittable ThreadLocal(TTL) 📌

[![Build Status](https://travis-ci.org/alibaba/transmittable-thread-local.svg?branch=master)](https://travis-ci.org/alibaba/transmittable-thread-local)
[![Windows Build Status](https://img.shields.io/appveyor/ci/oldratlee/transmittable-thread-local/master.svg?label=windows%20build)](https://ci.appveyor.com/project/oldratlee/transmittable-thread-local)
[![Coverage Status](https://img.shields.io/codecov/c/github/alibaba/transmittable-thread-local/master.svg)](https://codecov.io/gh/alibaba/transmittable-thread-local/branch/master)
[![Maintainability](https://api.codeclimate.com/v1/badges/de6af6136e538cf1557c/maintainability)](https://codeclimate.com/github/alibaba/transmittable-thread-local/maintainability)  
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Javadocs](https://www.javadoc.io/badge/com.alibaba/transmittable-thread-local.svg)](https://alibaba.github.io/transmittable-thread-local/apidocs/)
[![Maven Central](https://img.shields.io/maven-central/v/com.alibaba/transmittable-thread-local.svg)](https://search.maven.org/search?q=g:com.alibaba%20AND%20a:transmittable-thread-local&core=gav)
[![GitHub release](https://img.shields.io/github/release/alibaba/transmittable-thread-local.svg)](https://github.com/alibaba/transmittable-thread-local/releases)  
[![Join the chat at https://gitter.im/alibaba/transmittable-thread-local](https://badges.gitter.im/alibaba/transmittable-thread-local.svg)](https://gitter.im/alibaba/transmittable-thread-local?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![GitHub issues](https://img.shields.io/github/issues/alibaba/transmittable-thread-local.svg)](https://github.com/alibaba/transmittable-thread-local/issues)
[![Percentage of issues still open](http://isitmaintained.com/badge/open/alibaba/transmittable-thread-local.svg)](http://isitmaintained.com/project/alibaba/transmittable-thread-local "Percentage of issues still open")
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/alibaba/transmittable-thread-local.svg)](http://isitmaintained.com/project/alibaba/transmittable-thread-local "Average time to resolve an issue")

📖 English Documentation | [📖 中文文档](README.md)

----------------------------------------

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->


- [🔧 Functions](#-functions)
- [🎨 Requirements](#-requirements)
- [👥 User Guide](#-user-guide)
    - [1. simple usage](#1-simple-usage)
    - [2. Transmit value even using thread pool](#2-transmit-value-even-using-thread-pool)
        - [2.1 Decorate `Runnable` and `Callable`](#21-decorate-runnable-and-callable)
        - [2.2 Decorate thread pool](#22-decorate-thread-pool)
        - [2.3 Use Java Agent to decorate thread pool implementation class](#23-use-java-agent-to-decorate-thread-pool-implementation-class)
- [🔌 Java API Docs](#-java-api-docs)
- [🍪 Maven dependency](#-maven-dependency)
- [🗿 More documentation](#-more-documentation)
- [📚 Related resources](#-related-resources)
    - [JDK core classes](#jdk-core-classes)
- [👷 Contributors](#-contributors)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

----------------------------------------

# 🔧 Functions

👉 The missing std Java™ lib(simple &amp; 0-dependency) for framework/middleware,
provide an enhanced `InheritableThreadLocal` that transmits `ThreadLocal` value between threads even using thread pooling components.
Support `Java` 12/11/10/9/8/7/6.

Class [`InheritableThreadLocal`](https://docs.oracle.com/javase/10/docs/api/java/lang/InheritableThreadLocal.html) in `JDK`
can transmit value to child thread from parent thread.

But when use thread pool, thread is cached up and used repeatedly. Transmitting value from parent thread to child thread has no meaning.
Application need transmit value from the time task is created to the time task is executed.

If you have problem or question, please [submit Issue](https://github.com/alibaba/transmittable-thread-local/issues) or play [fork](https://github.com/alibaba/transmittable-thread-local/fork) and pull request dance.

# 🎨 Requirements

The Requirements listed below is also why I sort out `TTL` in my work.

- Application container or high layer framework transmit information to low layer sdk.
- Transmit context to logging without application code aware.

# 👥 User Guide

## 1. simple usage

```java
// set in parent thread
TransmittableThreadLocal<String> parent = new TransmittableThreadLocal<String>();
parent.set("value-set-in-parent");

// =====================================================

// read in child thread, value is "value-set-in-parent"
String value = parent.get();
```

This is the function of class [`InheritableThreadLocal`](https://docs.oracle.com/javase/10/docs/api/java/lang/InheritableThreadLocal.html), should use class [`InheritableThreadLocal`](https://docs.oracle.com/javase/10/docs/api/java/lang/InheritableThreadLocal.html) instead.

But when use thread pool, thread is cached up and used repeatedly. Transmitting value from parent thread to child thread has no meaning.
Application need transmit value from the time task is created to the point task is executed.

The solution is below usage.

## 2. Transmit value even using thread pool

### 2.1 Decorate `Runnable` and `Callable`

Decorate input `Runnable` and `Callable` by [`TtlRunnable`](/src/main/java/com/alibaba/ttl/TtlRunnable.java)
and [`TtlCallable`](src/main/java/com/alibaba/ttl/TtlCallable.java).

Sample code:

```java
TransmittableThreadLocal<String> parent = new TransmittableThreadLocal<String>();
parent.set("value-set-in-parent");

Runnable task = new Task("1");
// extra work, create decorated ttlRunnable object
Runnable ttlRunnable = TtlRunnable.get(task);
executorService.submit(ttlRunnable);

// =====================================================

// read in task, value is "value-set-in-parent"
String value = parent.get();
```

above code show how to dealing with `Runnable`, `Callable` is similar:

```java
TransmittableThreadLocal<String> parent = new TransmittableThreadLocal<String>();
parent.set("value-set-in-parent");

Callable call = new Call("1");
// extra work, create decorated ttlCallable object
Callable ttlCallable = TtlCallable.get(call);
executorService.submit(ttlCallable);

// =====================================================

// read in call, value is "value-set-in-parent"
String value = parent.get();
```

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

TransmittableThreadLocal<String> parent = new TransmittableThreadLocal<String>();
parent.set("value-set-in-parent");

Runnable task = new Task("1");
Callable call = new Call("2");
executorService.submit(task);
executorService.submit(call);

// =====================================================

// read in Task or Callable, value is "value-set-in-parent"
String value = parent.get();
```

### 2.3 Use Java Agent to decorate thread pool implementation class

In this usage, transmission is transparent\(no decoration operation\).

Sample code:

```java
ExecutorService executorService = Executors.newFixedThreadPool(3);

Runnable task = new Task("1");
Callable call = new Call("2");
executorService.submit(task);
executorService.submit(call);

// =====================================================

// read in Task or Callable, value is "value-set-in-parent"
String value = parent.get();
```

See demo [`AgentDemo.kt`](src/test/java/com/alibaba/demo/agent/AgentDemo.kt).

At present, `TTL` agent has decorated below `JDK` thread pool implementation:

- `java.util.concurrent.ThreadPoolExecutor` and `java.util.concurrent.ScheduledThreadPoolExecutor`  
    decoration implemetation code is in [`TtlExecutorTransformlet.java`](src/main/java/com/alibaba/ttl/threadpool/agent/internal/transformlet/impl/TtlExecutorTransformlet.java)
- `java.util.concurrent.ForkJoinTask`（corresponding thread pool is `java.util.concurrent.ForkJoinPool`）  
    decoration implemetation code is in [`TtlForkJoinTransformlet.java`](src/main/java/com/alibaba/ttl/threadpool/agent/internal/transformlet/impl/TtlForkJoinTransformlet.java)
- `java.util.TimerTask`（corresponding thread pool is `java.util.Timer`）  
    decoration implemetation code is in [`TtlTimerTaskTransformlet.java`](src/main/java/com/alibaba/ttl/threadpool/agent/internal/transformlet/impl/TtlTimerTaskTransformlet.java)  
    **_NOTE_**: decoration for `TimerTask` default is disable, enabled by agent argument `ttl.agent.enable.timer.task`: `-javaagent:path/to/transmittable-thread-local-2.x.x.jar=ttl.agent.enable.timer.task:true`.  
    more info about `TTL` agent arguments, see [the javadoc of `TtlAgent.java`](src/main/java/com/alibaba/ttl/threadpool/agent/TtlAgent.java).

Add start options on Java command:

- `-javaagent:path/to/transmittable-thread-local-2.x.x.jar`

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

Java command example:

```bash
java -javaagent:transmittable-thread-local-2.x.x.jar \
    -cp classes \
    com.alibaba.ttl.threadpool.agent.demo.AgentDemo
```

or

```bash
java -javaagent:path/to/ttl-foo-name-changed.jar \
    -Xbootclasspath/a:path/to/ttl-foo-name-changed.jar \
    -cp classes \
    com.alibaba.ttl.threadpool.agent.demo.AgentDemo
```

Run the script [`scripts/run-agent-demo.sh`](scripts/run-agent-demo.sh)
to start demo of "Use Java Agent to decorate thread pool implementation class".

# 🔌 Java API Docs

The current version Java API documentation: <https://alibaba.github.io/transmittable-thread-local/apidocs/>

# 🍪 Maven dependency

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>transmittable-thread-local</artifactId>
    <version>2.10.2</version>
</dependency>
```

Check available version at [search.maven.org](https://search.maven.org/search?q=g:com.alibaba%20AND%20a:transmittable-thread-local&core=gav).

# 🗿 More documentation

- [🎓 Developer Guide](docs/developer-guide-en.md)

# 📚 Related resources

## JDK core classes

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
