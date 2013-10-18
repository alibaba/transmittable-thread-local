multi-thread context(MTC)
=====================================

解决多线程传递Context的需求。

功能
----------------------------

1. 父线程创建子线程时，Context传递。
1. 使用线程池时，执行任务Context能传递。

需求场景
----------------------------

TODO

使用说明
=====================================

1. 简单使用MtContext
----------------------------

```java
// 在父线程中设置
MtContext.set("key", "value-set-in-parent");

// 在子线程中可以读取, 值是"value-set-in-parent"
String value = MtContext.get("key"); 
```

对于使用了线程池的情况，线程由线程池创建好，并且会把线程Cache起来，反复使用。

这时父子线程关系的上下文传递已经没有意义，应用中要做上下文传递，实际上是在把 **任务提交给线程池时**的上下文传递到 **任务执行时**。
解决方法参见下面的这几种用法。

2. 保证线程池中传递MtContext
----------------------------

使用[`com.alibaba.mtc.MtContextRunnable`](https://github.com/oldratlee/multi-thread-context/blob/master/src/main/java/com/alibaba/mtc/MtContextRunnable.java)和[`com.alibaba.mtc.MtContextCallable`](https://github.com/oldratlee/multi-thread-context/blob/master/src/main/java/com/alibaba/mtc/MtContextCallable.java)来修饰传入线程池的`Runnable`和`Callable`。

示例代码：

```java
MtContext.set("key", "value-set-in-parent");

Runnable task = new Task("1");
Runnable mtContextRunnable = MtContextRunnable.get(task); // 额外的处理，生成修饰了的对象mtContextRunnable
executorService.submit(mtContextRunnable);

// Task中可以读取, 值是"value-set-in-parent"
String value = MtContext.get("key");
```

上面演示了`Runnable`，`Callable`的处理类似

```java
MtContext.set("key", "value-set-in-parent");

Callable call = new Call("1");
Callable mtContextCallable = MtContextCallable.get(call); // 额外的处理，生成修饰了的对象mtContextCallable
executorService.submit(mtContextCallable);

// Call中可以读取, 值是"value-set-in-parent"
String value = MtContext.get("key");
```

3. 修饰线程池，省去`Runnable`和`Callable`的修饰
----------------------------

每次传入线程池时修饰`Runnable`和`Callable`，这个逻辑可以在线程池中完成。

通过工具类[`com.alibaba.mtc.threadpool.MtContextExecutors`](https://github.com/oldratlee/multi-thread-context/blob/master/src/main/java/com/alibaba/mtc/threadpool/MtContextExecutors.java)完成，有下面的方法：

* `getMtcExecutor`：修饰接口`Executor`
* `getMtcExecutorService`：修饰接口`ExecutorService`
* `ScheduledExecutorService`：修饰接口`ScheduledExecutorService`

示例代码：

```java
ExecutorService executorService = ...
executorService = MtContextExecutors.getMtcExecutorService(executorService); // 额外的处理，生成修饰了的对象executorService

MtContext.set("key", "value-set-in-parent");

Runnable task = new Task("1");
Callable call = new Call("2");
executorService.submit(task);
executorService.submit(call);

// Task或是Call中可以读取, 值是"value-set-in-parent"
String value = MtContext.get("key");
```

4. 使用Java Agent来修饰JDK线程池实现类
----------------------------

这种方式，实现线程池的`MtContext`传递，代码是透明的。  

目前Agent中，修饰了两个线程池实现类（实现代码在[MtContextTransformer.java](https://github.com/oldratlee/multi-thread-context/blob/master/src/main/java/com/alibaba/mtc/threadpool/agent/MtContextTransformer.java)）：

- `java.util.concurrent.ThreadPoolExecutor`
- `java.util.concurrent.ScheduledThreadPoolExecutor`

在Java的启动参数加上：

- `-Xbootclasspath/a:/path/to/multithread.context-x.y.z.jar:/path/to/javassist-3.18.1-GA.jar`
- `-javaagent:/path/to/multithread.context-x.y.z.jar`

**注意**： 

* Agent修改是JDK的类，类中加入了引用`MTC`的代码，所以`MTC Agent`的Jar要加到`bootclasspath`上。
* `MTC Agent`使用`javassist`来修改类的实现，所以`bootclasspath`还在加上`javassist`的Jar。

Java命令行示例如下：

```bash
java -Xbootclasspath/a:dependency/javassist-3.18.1-GA.jar:multithread.context-0.9.0-SNAPSHOT.jar \
    -javaagent:multithread.context-0.9.0-SNAPSHOT.jar \
    -cp classes \
    com.alibaba.mtc.threadpool.agent.AgentDemo
```

代码代码中提供了Demo演示『使用Java Agent来修饰线程池实现类』，执行工程下的脚本[`run-agent-demo.sh`](https://github.com/oldratlee/multi-thread-context/blob/master/run-agent-demo.sh)即可运行Demo。

### 什么情况下，`Java Agent`的使用方式`MtContext`会失效

由于`Runnable`和`Callable`的修饰代码，是在线程池类中插入的。下面的情况会让插入的代码被绕过，`MtContext`会失效。

- 用户代码中继承`java.util.concurrent.ThreadPoolExecutor`和`java.util.concurrent.ScheduledThreadPoolExecutor`，
覆盖了`execute`、`submit`、`schedule`等提交任务的方法，并且调用父类的方法。   
- 目前，没有修饰`java.util.Timer`类，使用`Timer`时，`MtContext`会有问题。

FAQ
=====================================

* Mac OS X下，使用javaagent，可能会报JavaLaunchHelper的出错信息。  
可以换一个版本的JDK。我的开发机上`1.7.0_40`有这个问题，`1.6.0_51`、`1.7.0_45`是OK的。   
JDK Bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=8021205

相关资源
=====================================

* [Java Agent规范](http://docs.oracle.com/javase/6/docs/api/java/lang/instrument/package-summary.html)
* [Java SE 6 新特性: Instrumentation 新功能](http://www.ibm.com/developerworks/cn/java/j-lo-jse61/)
* [Creation, dynamic loading and instrumentation with javaagents](http://dhruba.name/2010/02/07/creation-dynamic-loading-and-instrumentation-with-javaagents/)
* [JavaAgent加载机制分析](http://alipaymiddleware.com/jvm/javaagent%E5%8A%A0%E8%BD%BD%E6%9C%BA%E5%88%B6%E5%88%86%E6%9E%90/)
* [Getting Started with Javassist](http://www.csg.ci.i.u-tokyo.ac.jp/~chiba/javassist/tutorial/tutorial.html)
