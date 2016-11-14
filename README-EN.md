Transmittable ThreadLocal(TTL)
=====================================

[![Build Status](https://travis-ci.org/alibaba/transmittable-thread-local.svg?branch=master)](https://travis-ci.org/alibaba/transmittable-thread-local)
[![Coverage Status](https://img.shields.io/codecov/c/github/alibaba/transmittable-thread-local/master.svg)](https://codecov.io/gh/alibaba/transmittable-thread-local/branch/master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.alibaba/transmittable-thread-local/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.alibaba/transmittable-thread-local/)
[![GitHub release](https://img.shields.io/github/release/alibaba/transmittable-thread-local.svg)](https://github.com/alibaba/transmittable-thread-local/releases)  
[![Join the chat at https://gitter.im/alibaba/transmittable-thread-local](https://badges.gitter.im/alibaba/transmittable-thread-local.svg)](https://gitter.im/alibaba/transmittable-thread-local?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![GitHub issues](https://img.shields.io/github/issues/alibaba/transmittable-thread-local.svg)](https://github.com/alibaba/transmittable-thread-local/issues)
[![Dependency Status](https://www.versioneye.com/user/projects/56c0a36218b271002c699dca/badge.svg)](https://www.versioneye.com/user/projects/56c0a36218b271002c699dca)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

<div align="right">
<a href="README.md">中文文档</a>
</div>

:wrench: Functions
----------------------------

:point_right: Transmit `ThreadLocal` value between threads, even using thread cached components like thread pool.

Class [`InheritableThreadLocal`](http://docs.oracle.com/javase/7/docs/api/java/lang/InheritableThreadLocal.html) in `JDK`
can transmit value to child thread from parent thread.

But when use thread pool, thread is cached up and used repeatedly. Transmitting value from parent thread to child thread has no meaning.
Application need transmit value from the time task is created to the time task is executed.

If you have problem or question, please [submit Issue](https://github.com/alibaba/transmittable-thread-local/issues) or play [fork](https://github.com/alibaba/transmittable-thread-local/fork) and pull request dance.

:art: Requirements
----------------------------

The Requirements listed below is also why I sort out `TTL` in my work. 

* Application container or high layer framework transmit information to low layer sdk.
* Transmit context to logging without application code aware.

:busts_in_silhouette: User Guide
=====================================

1. simple usage
----------------------------

```java
// set in parent thread
TransmittableThreadLocal<String> parent = new TransmittableThreadLocal<String>();
parent.set("value-set-in-parent");

// =====================================================

// read in child thread, value is "value-set-in-parent"
String value = parent.get(); 
```

This is the function of class [`InheritableThreadLocal`](http://docs.oracle.com/javase/7/docs/api/java/lang/InheritableThreadLocal.html), should use class [`InheritableThreadLocal`](http://docs.oracle.com/javase/7/docs/api/java/lang/InheritableThreadLocal.html) instead.

But when use thread pool, thread is cached up and used repeatedly. Transmitting value from parent thread to child thread has no meaning.
Application need transmit value from the time task is created to the point task is executed.

The solution is below usage.

2. Transmit value even using thread pool
----------------------------

### 2.1 Decorate `Runnable` and `Callable`

Decorate input `Runnable` and `Callable` by [`com.alibaba.ttl.TtlRunnable`](/src/main/java/com/alibaba/ttl/TtlRunnable.java)
and [`com.alibaba.ttl.TtlCallable`](src/main/java/com/alibaba/ttl/TtlCallable.java).

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

* `getTtlExecutor`: decorate interface `Executor`
* `getTtlExecutorService`: decorate interface `ExecutorService`
* `ScheduledExecutorService`: decorate interface `ScheduledExecutorService`

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

### 2.3. Use Java Agent to decorate thread pool implementation class

In this usage, transmission is transparent\(no decoration operation\).

Sample code:

```java
ExecutorService executorService = Executors.newFixedThreadPool(3);

Runnable task = new Task("1");
Callable call = new Call("2");
executorService.submit(task);
executorService.submit(call);

// =====================================================

// Task或是Call中可以读取, 值是"value-set-in-parent"
String value = parent.get();
```

See demo [`AgentDemo.java`](src/test/java/com/alibaba/ttl/threadpool/agent/AgentDemo.java).

Agent decorate 2 thread pool implementation classes
\(implementation code [`TtlTransformer.java`](src/main/java/com/alibaba/ttl/threadpool/agent/TtlTransformer.java)\):

- `java.util.concurrent.ThreadPoolExecutor`
- `java.util.concurrent.ScheduledThreadPoolExecutor`

Add start options on Java command: 

- `-Xbootclasspath/a:/path/to/transmittable-thread-local-2.x.x.jar`
- `-javaagent:/path/to/transmittable-thread-local-2.x.x.jar`

**NOTE**： 

* Agent modify the jdk classes, add code refer to the class of `TTL`, so the jar of `TTL Agent` should add to `bootclasspath`.
* `TTL Agent` modify the class by `javassist`, so the Jar of `javassist` should add to `bootclasspath` too.

Java command example:

```bash
java -Xbootclasspath/a:transmittable-thread-local-2.0.0.jar \
    -javaagent:transmittable-thread-local-2.0.0.jar \
    -cp classes \
    com.alibaba.ttl.threadpool.agent.demo.AgentDemo
```

Run the script [`run-agent-demo.sh`](run-agent-demo.sh)
to start demo of "Use Java Agent to decorate thread pool implementation class".

:electric_plug: Java API Docs
======================

The current version Java API documentation: <http://alibaba.github.io/transmittable-thread-local/apidocs/>

:cookie: Maven dependency
=====================================

```xml
<dependency>
	<groupId>com.alibaba</groupId>
	<artifactId>transmittable-thread-local</artifactId>
	<version>2.1.0</version>
</dependency>
```

Check available version at [search.maven.org](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.alibaba%22%20AND%20a%3A%22transmittable-thread-local%22).

:books: Related resources
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

Javassist
----------------------------

* [Getting Started with Javassist](http://www.csg.ci.i.u-tokyo.ac.jp/~chiba/javassist/tutorial/tutorial.html)
