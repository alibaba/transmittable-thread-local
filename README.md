multi-thread context(MTC)
=====================================

解决多线程传递Context的需求。

需求场景
----------------------------


功能
----------------------------

1. 父线程创建子线程时，Context传递。
1. 使用线程池时，执行任务Context能传递。

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

### 3. 修饰线程池，简化`Runnable`和`Callable`的修饰操作

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

### 4. 使用Java Agent来完成线程池的修饰操作

这种方式，实现线程池的`MtContext`传递，代码是透明的。  
\# 目前Agent中，修饰了`java.util.concurrent.ThreadPoolExecutor`和`java.util.concurrent.ScheduledThreadPoolExecutor`两个实现类。

在Java的启动参数加上`-javaagent:path/to/multithread.context-x.y.z.jar`。

Java命令行示例如下：

```bash
java -Xbootclasspath/a:multithread.context-0.9.0-SNAPSHOT.jar:javassist-3.18.1-GA.jar \
    -javaagent:multithread.context-0.9.0-SNAPSHOT.jar \
    -cp dependency/log4j-1.2.17.jar:dependency/slf4j-api-1.5.6.jar:dependency/slf4j-log4j12-1.5.6.jar:classes \
    com.alibaba.mtc.threadpool.agent.AgentDemo
```

代码代码中提供了Demo演示『使用Java Agent来完成线程池的修饰操作』，执行工程下的脚本[`run-agent-demo.sh`](https://github.com/oldratlee/multi-thread-context/blob/master/run-agent-demo.sh)即可运行Demo。

FAQ
=====================================

* Mac OS X下，使用javaagent，报JavaLaunchHelper的出错信息  
JDK Bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=8021205
