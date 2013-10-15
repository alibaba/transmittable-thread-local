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

### 1. 简单使用MtContext

```java
// 在父线程中设置
MtContext.set("key", "value-set-in-parent");

// 在子线程中可以读取, 值是"value-set-in-parent"
String value = MtContext.get("key"); 
```

### 2. 保证线程池中传递MtContext

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

### 3. 修饰线程池，省去`Runnable`和`Callable`的修饰

```java

executorService = MtContextExecutors.getMtcExecutorService(executorService); // 额外的处理，生成修饰了的对象executorService

MtContext.set("key", "value-set-in-parent");

Runnable task = new Task("1");
Callable call = new Call("2");
executorService.submit(task);
executorService.submit(call);

// Task或是Call中可以读取, 值是"value-set-in-parent"
String value = MtContext.get("key");
```

### 4. 使用Java Agent来修饰线程池实现类

这种方式，实现线程池的`MtContext`传递，代码是透明的。  
\# 目前Agent中，修饰了`java.util.concurrent.ThreadPoolExecutor`和`java.util.concurrent.ScheduledThreadPoolExecutor`两个实现类。

在Java的启动参数加上`-Xbootclasspath/a:multithread.context-x.y.z.jar:javassist-3.18.1-GA.jar -javaagent:path/to/multithread.context-x.y.z.jar`。  

**注意**： 

* 因为Agent修改的是JDK的类，所以Agent的Jar要加到`bootclasspath`上。
* 使用`javassist`来修改类的实现，所以`bootclasspath`还在加上`javassist`的Jar。

Java命令行示例如下：

```bash
java -Xbootclasspath/a:dependency/javassist-3.18.1-GA.jar:multithread.context-0.9.0-SNAPSHOT.jar
    -javaagent:multithread.context-0.9.0-SNAPSHOT.jar 
    -cp classes
    com.alibaba.mtc.threadpool.agent.AgentDemo
```

代码代码中提供了Demo演示『使用Java Agent来修饰线程池实现类』，执行工程下的脚本[`run-agent-demo.sh`](https://github.com/oldratlee/multi-thread-context/blob/master/run-agent-demo.sh)即可运行Demo。

FAQ
=====================================

* Mac OS X下，使用javaagent，报JavaLaunchHelper的出错信息  
JDK Bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=8021205

相关资源
=====================================

* [Java Agent规范](http://docs.oracle.com/javase/6/docs/api/java/lang/instrument/package-summary.html)
* [Java SE 6 新特性: Instrumentation 新功能](http://www.ibm.com/developerworks/cn/java/j-lo-jse61/)
* [JavaAgent加载机制分析](http://alipaymiddleware.com/jvm/javaagent%E5%8A%A0%E8%BD%BD%E6%9C%BA%E5%88%B6%E5%88%86%E6%9E%90/)
* [Getting Started with Javassist](http://www.csg.ci.i.u-tokyo.ac.jp/~chiba/javassist/tutorial/tutorial.html)
