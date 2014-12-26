multi-thread context(MTC)
=====================================

[![Build Status](https://travis-ci.org/alibaba/multi-thread-context.svg?branch=master)](https://travis-ci.org/alibaba/multi-thread-context) [![Coverage Status](https://coveralls.io/repos/alibaba/multi-thread-context/badge.png?branch=master)](https://coveralls.io/r/alibaba/multi-thread-context?branch=master) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.alibaba/multithread.context/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.alibaba/multithread.context/)

<div align="right">
<a href="README.md">中文文档</a>
</div>

:wrench: Functions
----------------------------

:point_right: Transmit multi-thread context, even using thread cached components like thread pool.

Class [`java.lang.InheritableThreadLocal`](http://docs.oracle.com/javase/6/docs/api/java/lang/InheritableThreadLocal.html) in `JDK`
can transmit context to child thread from parent thread.

But when use thread pool, thread is cached up and used repeatedly. Transmitting context from parent thread to child thread has no meaning.
Application need transmit context from the time task is created to the time task is executed.

If you have problem or question, 

- [submit Issue](https://github.com/alibaba/multi-thread-context/issues) 
- [mail list](http://mtc.59504.x6.nabble.com/) (Powered by [nabble](http://www.nabble.com/))
	- Click "Options > Subscribe via email" to subscribe to this mailing list; 
	- Click "Options > Post by email..." to get the email address of this mailing list; 
	- You can post messages via email or through the forum interface.

:art: Requirements
----------------------------

The Requirements listed below is also why I sort out `MTC` in my work. 

* Application container or high layer framework transmit information to low layer sdk.
* Transmit context to logging without application code aware.

:notebook: User Guide
=====================================

1. simple usage
----------------------------

```java
// set in parent thread
MtContextThreadLocal<String> parent = new MtContextThreadLocal<String>();
parent.set("value-set-in-parent");

// =====================================================

// read in child thread, value is "value-set-in-parent"
String value = parent.get(); 
```

This is the function of class [`java.lang.InheritableThreadLocal`](http://docs.oracle.com/javase/6/docs/api/java/lang/InheritableThreadLocal.html), should use class [`java.lang.InheritableThreadLocal`](http://docs.oracle.com/javase/6/docs/api/java/lang/InheritableThreadLocal.html) instead.

But when use thread pool, thread is cached up and used repeatedly. Transmitting context from parent thread to child thread has no meaning.
Application need transmit context from the time task is created to the point task is executed.

The solution is below usage.

2. Transmit context even using thread pool
----------------------------

### 2.1 Decorate `Runnable` and `Callable`

Decorate input `Runnable` and `Callable` by [`com.alibaba.mtc.MtContextRunnable`](src/main/java/com/alibaba/mtc/MtContextRunnable.java)
and [`com.alibaba.mtc.MtContextCallable`](src/main/java/com/alibaba/mtc/MtContextCallable.java).

Sample code:

```java
MtContextThreadLocal<String> parent = new MtContextThreadLocal<String>();
parent.set("value-set-in-parent");

Runnable task = new Task("1");
// extra work, create decorated mtContextRunnable object
Runnable mtContextRunnable = MtContextRunnable.get(task); 
executorService.submit(mtContextRunnable);

// =====================================================

// read in task, value is "value-set-in-parent"
String value = parent.get(); 
```

above code show how to dealing with `Runnable`, `Callable` is similar:

```java
MtContextThreadLocal<String> parent = new MtContextThreadLocal<String>();
parent.set("value-set-in-parent");

Callable call = new Call("1");
// extra work, create decorated mtContextCallable object
Callable mtContextCallable = MtContextCallable.get(call); 
executorService.submit(mtContextCallable);

// =====================================================

// read in call, value is "value-set-in-parent"
String value = parent.get(); 
```

### 2.2 Decorate thread pool

Eliminating the work of `Runnable` and `Callable` Decoration every time it is submitted to thread pool. This work can completed in the thread pool.

Use util class
[`com.alibaba.mtc.threadpool.MtContextExecutors`](src/main/java/com/alibaba/mtc/threadpool/MtContextExecutors.java)
to decorate thread pool.

Util class `com.alibaba.mtc.threadpool.MtContextExecutors` has below methods:

* `getMtcExecutor`: decorate interface `Executor`
* `getMtcExecutorService`: decorate interface `ExecutorService`
* `ScheduledExecutorService`: decorate interface `ScheduledExecutorService`

Sample code:

```java
ExecutorService executorService = ...
// extra work, create decorated executorService object
executorService = MtContextExecutors.getMtcExecutorService(executorService); 

MtContextThreadLocal<String> parent = new MtContextThreadLocal<String>();
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

In this usage, `MtContext` transmission is transparent\(no decoration operation\).

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

See demo [`AgentDemo.java`](src/test/java/com/alibaba/mtc/threadpool/agent/AgentDemo.java).

Agent decorate 2 thread pool implementation classes
\(implementation code [`MtContextTransformer.java`](src/main/java/com/alibaba/mtc/threadpool/agent/MtContextTransformer.java)\):

- `java.util.concurrent.ThreadPoolExecutor`
- `java.util.concurrent.ScheduledThreadPoolExecutor`

Add start options on Java command: 

- `-Xbootclasspath/a:/path/to/multithread.context-x.y.z.jar:/path/to/javassist-3.12.1.GA.jar`
- `-javaagent:/path/to/multithread.context-x.y.z.jar`

**NOTE**： 

* Agent modify the jdk classes, add code refer to the class of `MTC`, so the jar of `MTC Agent` should add to `bootclasspath`.
* `MTC Agent` modify the class by `javassist`, so the Jar of `javassist` should add to `bootclasspath` too.

Java command example:

```bash
java -Xbootclasspath/a:dependency/javassist-3.12.1.GA.jar:multithread.context-1.0.0.jar \
    -javaagent:multithread.context-0.9.0-SNAPSHOT.jar \
    -cp classes \
    com.alibaba.mtc.threadpool.agent.AgentDemo
```

Run the script [`run-agent-demo.sh`](run-agent-demo.sh)
to start demo of "Use Java Agent to decorate thread pool implementation class".

:electric_plug: Java API Docs
======================

The current version Java API documentation: <http://alibaba.github.io/multi-thread-context/apidocs/>

:cookie: Maven dependency
=====================================

```xml
<dependency>
	<groupId>com.alibaba</groupId>
	<artifactId>multithread.context</artifactId>
	<version>1.0.3</version>
</dependency>
```

Check available version at [search.maven.org](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.alibaba%22%20AND%20a%3A%22multithread.context%22).

:books: Related resources
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

Javassist
----------------------------

* [Getting Started with Javassist](http://www.csg.ci.i.u-tokyo.ac.jp/~chiba/javassist/tutorial/tutorial.html)
