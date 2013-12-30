multi-thread context(MTC)
=====================================

<div align="right">
<a href="https://github.com/alibaba/multi-thread-context/blob/master/README-EN.md">English Documentation</a>
</div>

在使用线程池等会Cache线程的组件情况下，完成多线程的Context传递。

`JDK`的[`java.lang.InheritableThreadLocal`](http://docs.oracle.com/javase/6/docs/api/java/lang/InheritableThreadLocal.html)类可以完成父子线程的Context传递。

但对于使用线程池等会Cache线程的组件的情况，线程由线程池创建好，并且线程是Cache起来反复使用的。

这时父子线程关系的上下文传递已经没有意义，应用中要做上下文传递，实际上是在把 **任务提交给线程池时**的上下文传递到 **任务执行时**。

有问题可以[提交Issue](https://github.com/alibaba/multi-thread-context/issues) 或 [Mail](mailto:oldratlee@gmail.com)。

需求场景
----------------------------

下面列出需求场景，即是整理出`MTC`的实际需求。

### 应用容器或上层框架跨应用代码给下层SDK传递信息

举个场景，淘宝的App Engine（如JAE或是TAE，PAAS平台）跑了ISV（这里指卖家的应用提供商）的应用，这些应用给淘宝卖家使用。

要防止ISV的应用拿到所有卖家家数据，这是数据安全的问题。解决的问题其中一环是：一个处理过程关联一个卖家的上下文，在这样的上下文中只能处理（读&写）这个卖家的数据。

请求由卖家发起（如从Web请求时进入App Engine），App Engine可以知道是从哪个卖家，在Web请求时在上下文中设置好卖家ID。

应用处理数据（DB、Cache、消息 etc.）是通过App Engine提供的服务SDK。当应用处理数据时，SDK检查数据所属的卖家是否和上下文中的卖家ID一致，不一致则拒绝数据的读写。

应用代码会使用线程池，并且这样的使用是正常的业务需求，卖家ID的传递从要App Engine传递到下层SDK要支持这样的用法。

\# 当然，仅仅通过这样一个手段是不能解决数据安全的，每次处理数据时，应用可能把数据汇聚到内存中，再批量导出或是自动Post出去。这个要通过其它的手段来解决，如代码白盒检查，内存分析，禁止应用自动对外请求。

### 日志记录系统上下文

App Engine的日志（如，SDK会记录日志）要记录系统上下文。由于不限制用户应用使用线程池，系统的上下文需要能跨线程的传递，且不影响应用代码。

使用说明
=====================================

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

但对于使用了异步执行（往往使用线程池完成）的情况，线程由线程池创建好，并且线程是Cache起来反复使用的。

这时父子线程关系的上下文传递已经没有意义，应用中要做上下文传递，实际上是在把 **任务提交给线程池时**的上下文传递到 **任务执行时**。
解决方法参见下面的这几种用法。

2. 保证线程池中传递Context
----------------------------

### 2.1 修饰`Runnable`和`Callable`

使用[`com.alibaba.mtc.MtContextRunnable`](https://github.com/alibaba/multi-thread-context/blob/master/src/main/java/com/alibaba/mtc/MtContextRunnable.java)和[`com.alibaba.mtc.MtContextCallable`](https://github.com/alibaba/multi-thread-context/blob/master/src/main/java/com/alibaba/mtc/MtContextCallable.java)来修饰传入线程池的`Runnable`和`Callable`。

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

![时序图](https://raw.github.com/wiki/alibaba/multi-thread-context/SequenceDiagram.png "时序图")

### 2.2 修饰线程池

省去`Runnable`和`Callable`的修饰，每次传入线程池时修饰`Runnable`和`Callable`，这个逻辑可以在线程池中完成。

通过工具类[`com.alibaba.mtc.threadpool.MtContextExecutors`](https://github.com/alibaba/multi-thread-context/blob/master/src/main/java/com/alibaba/mtc/threadpool/MtContextExecutors.java)完成，有下面的方法：

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

### 2.3. 使用Java Agent来修饰JDK线程池实现类

这种方式，实现线程池的`MtContext`传递是透明的（不需要修饰操作）。Demo参见[`AgentDemo.java`](https://github.com/alibaba/multi-thread-context/blob/master/src/test/java/com/alibaba/mtc/threadpool/agent/AgentDemo.java)。

目前Agent中，修饰了两个线程池实现类（实现代码在[`MtContextTransformer.java`](https://github.com/alibaba/multi-thread-context/blob/master/src/main/java/com/alibaba/mtc/threadpool/agent/MtContextTransformer.java)）：

- `java.util.concurrent.ThreadPoolExecutor`
- `java.util.concurrent.ScheduledThreadPoolExecutor`

在Java的启动参数加上：

- `-Xbootclasspath/a:/path/to/multithread.context-x.y.z.jar:/path/to/javassist-3.12.1.GA.jar`
- `-javaagent:/path/to/multithread.context-x.y.z.jar`

**注意**： 

* Agent修改是JDK的类，类中加入了引用`MTC`的代码，所以`MTC Agent`的Jar要加到`bootclasspath`上。
* `MTC Agent`使用`javassist`来修改类的实现，所以`bootclasspath`还在加上`javassist`的Jar。

Java命令行示例如下：

```bash
java -Xbootclasspath/a:dependency/javassist-3.12.1.GA.jar:multithread.context-1.0.0.jar \
    -javaagent:multithread.context-0.9.0-SNAPSHOT.jar \
    -cp classes \
    com.alibaba.mtc.threadpool.agent.AgentDemo
```

有Demo演示『使用Java Agent来修饰线程池实现类』，执行工程下的脚本[`run-agent-demo.sh`](https://github.com/alibaba/multi-thread-context/blob/master/run-agent-demo.sh)即可运行Demo。

#### 什么情况下，`Java Agent`的使用方式`MtContext`会失效

由于`Runnable`和`Callable`的修饰代码，是在线程池类中插入的。下面的情况会让插入的代码被绕过，`MtContext`会失效。

- 用户代码中继承`java.util.concurrent.ThreadPoolExecutor`和`java.util.concurrent.ScheduledThreadPoolExecutor`，
覆盖了`execute`、`submit`、`schedule`等提交任务的方法，并且没有调用父类的方法。   
修改线程池类的实现，`execute`、`submit`、`schedule`等提交任务的方法禁止这些被覆盖，可以规避这个问题。
- 目前，没有修饰`java.util.Timer`类，使用`Timer`时，`MtContext`会有问题。

##### 如何权衡这些失效情况

把这些失效情况都解决了是最好的，但复杂化了实现。下面是一些权衡：

- 不推荐使用`Timer`类，推荐用`ScheduledThreadPoolExecutor`。
`ScheduledThreadPoolExecutor`实现更强壮，并且功能更丰富。
如支持配置线程池的大小（`Timer`只有一个线程）；`Timer`在`Runnable`中抛出异常会中止定时执行。
- 覆盖了`execute`、`submit`、`schedule`的问题的权衡是：
业务上没有修改这些方法的需求。并且线程池类提供了`beforeExecute`方法用于插入扩展的逻辑。

#### 已有Java Agent中嵌入`MtContext Agent`

这样可以减少Java命令上Agent的配置。

在自己的`ClassFileTransformer`中调用`MtContextTransformer`，示例代码如下：

```java
public class TransformerAdaptor implements ClassFileTransformer {
    final MtContextTransformer mtContextTransformer = new MtContextTransformer();

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {
        final byte[] transform = mtContextTransformer.transform(
            loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
        if (transform != null) {
            return transform;
        }

        // Your transform code ...

        return null;
    }
}
```

注意还是要在`bootclasspath`上，加上`MtContext`依赖的2个Jar。

Maven依赖
=====================================

示例：

```xml
<dependency>
	<groupId>com.alibaba</groupId>
	<artifactId>multithread.context</artifactId>
	<version>1.0.1</version>
</dependency>
```

可以在 [search.maven.org](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22multithread.context%22%20g%3A%22com.alibaba%22) 查看可用的版本。

性能测试
=====================================

内存泄漏
----------------------------

对比测试[`MtContextThreadLocal`](https://github.com/alibaba/multi-thread-context/blob/master/src/main/java/com/alibaba/mtc/MtContextThreadLocal.java)和[`ThreadLocal`](http://docs.oracle.com/javase/6/docs/api/java/lang/ThreadLocal.html)，测试Case是：

简单一个线程一直循环`new` `MtContextThreadLocal`、`ThreadLocal`实例，不主动做任何清理操作，即不调用`ThreadLocal`的`remove`方法主动清空。

### 验证结果

都可以持续运行，不会出内存溢出`OutOfMemoryError`。

### 执行方式

可以通过执行工程下的脚本来运行Case验证：

* 脚本[`run-memoryleak-ThreadLocal.sh`](https://github.com/alibaba/multi-thread-context/blob/master/run-memoryleak-ThreadLocal.sh)运行`ThreadLocal`的测试。  
测试类是[`NoMemoryLeak_ThreadLocal_NoRemove`](https://github.com/alibaba/multi-thread-context/blob/master/src/test/java/com/alibaba/mtc/perf/memoryleak/NoMemoryLeak_ThreadLocal_NoRemove.java)。
* 脚本[`run-memoryleak-MtContextThreadLocal.sh`](https://github.com/alibaba/multi-thread-context/blob/master/run-memoryleak-MtContextThreadLocal.sh)运行`MtContextThreadLocal`的测试。
测试类是[`NoMemoryLeak_MtContextThreadLocal_NoRemove`](https://github.com/alibaba/multi-thread-context/blob/master/src/test/java/com/alibaba/mtc/perf/memoryleak/NoMemoryLeak_MtContextThreadLocal_NoRemove.java)。

TPS & 压力测试
----------------------------

对比测试[`MtContextThreadLocal`](https://github.com/alibaba/multi-thread-context/blob/master/src/main/java/com/alibaba/mtc/MtContextThreadLocal.java)和[`ThreadLocal`](http://docs.oracle.com/javase/6/docs/api/java/lang/ThreadLocal.html)，测试Case是：

2个线程并发一直循环`new` `MtContextThreadLocal`、`ThreadLocal`实例，不主动做任何清理操作，即不调用`ThreadLocal`的`remove`方法主动清空。

### 验证结果

在我的4核开发机上运行了24小时，稳定正常。

TPS结果如下：

`ThreadLocal`的TPS稳定在～29K：

```bash
......
tps: 29189
tps: 29587
tps: 28261
tps: 28919
```

`MtContextThreadLocal`的TPS稳定在～25K：

```bash
......
tps: 25200
tps: 25263
tps: 24876
tps: 24926
tps: 25005
```

GC情况如下：

`ThreadLocal`：

```bash
   S0     S1      E      O      P    YGC      YGCT     FGC     FGCT   GCT
......
 97.66   0.00   1.91  31.88  37.58 5550583 15453.930   271    2.798 15456.728
  0.00  97.66   0.00  50.44  37.58 5553030 15459.746   271    2.798 15462.543
  0.00  97.66   0.00  69.65  37.58 5555456 15466.223   271    2.798 15469.021
  0.00  96.88   0.00  89.43  37.58 5557844 15473.175   271    2.798 15475.973
 98.44   0.00   0.00  12.64  37.58 5560263 15479.334   272    2.818 15482.152
 96.09   0.00  28.21  32.65  37.58 5562739 15484.480   272    2.818 15487.298
 97.66   0.00  42.06  53.02  37.58 5565181 15490.219   272    2.818 15493.037
 96.88   0.00  61.95  72.61  37.58 5567593 15496.748   272    2.818 15499.566
  0.00  97.66   0.00  89.94  37.58 5570166 15503.949   272    2.818 15506.767
 97.66   0.00   0.00   4.71  37.58 5573247 15511.135   273    2.829 15513.964
```

`MtContextThreadLocal`：

```bash
   S0     S1      E      O      P    YGC      YGCT     FGC     FGCT   GCT
......
 97.66   0.00  66.99  34.22  38.76 4306321 14418.315  2840   38.798 14457.113
 98.44   0.00  30.83  41.82  38.76 4306423 14418.676  2840   38.798 14457.474
 97.66   0.00  91.83  49.23  38.76 4306521 14419.040  2840   38.798 14457.838
  0.00  99.22  30.41  56.47  38.76 4306618 14419.412  2840   38.798 14458.210
  0.00  97.66  68.25  63.40  38.76 4306710 14419.812  2840   38.798 14458.610
 97.66   0.00   2.01  70.35  38.76 4306801 14420.217  2840   38.798 14459.015
 98.44   0.00  46.48  76.98  38.76 4306889 14420.648  2840   38.798 14459.446
 98.44   0.00  96.90  83.53  38.81 4306976 14421.029  2840   38.798 14459.827
 98.44   0.00  96.39  89.17  51.01 4307049 14421.406  2840   38.798 14460.204
 98.44   0.00  96.44  89.17  51.02 4307049 14421.406  2840   38.798 14460.204
```

#### TPS下降的原因分析

使用`jvisualvm` Profile方法耗时，`MtContextThreadLocal`的Case多了热点方法`MtContextThreadLocal.addMtContextThreadLocal`。

目前在这个方法中，判定是否在`MtContextThreadLocal.holder`已经记录了当前`MtContextThreadLocal`实例的方法是循环遍历`MtContextThreadLocal.holder`。  
方法`MtContextThreadLocal.addMtContextThreadLocal`是时间复杂度是`O(n)`，`n`是`MtContextThreadLocal.holder`中实例个数。  
当`MtContextThreadLocal`实例个数比较多时（TPS测试Case符合这个条件，不停在创建实例直到被垃圾回收），该方法成为热点。

实际使用场景中，`MtContextThreadLocal`实例个数非常有限，不会有性能问题。

#### FGC次数增多的原因分析

在`MtContextThreadLocal.holder`中，持有`MtContextThreadLocal`实例的弱引用，减慢实例的回收。

实际使用场景中，`MtContextThreadLocal`实例个数非常有限，不会有性能问题。

### 执行方式

可以通过执行工程下的脚本来运行Case验证：

* 脚本[`run-tps-ThreadLocal.sh`](https://github.com/alibaba/multi-thread-context/blob/master/run-tps-ThreadLocal.sh)运行`ThreadLocal`的测试。  
测试类是[`CreateThreadLocalInstanceTps`](https://github.com/alibaba/multi-thread-context/blob/master/src/test/java/com/alibaba/mtc/perf/tps/CreateThreadLocalInstanceTps.java)。
* [`run-tps-MtContextThreadLocal.sh`](https://github.com/alibaba/multi-thread-context/blob/master/run-tps-MtContextThreadLocal.sh)运行`MtContextThreadLocal`的测试。
测试类是[`CreateMtContextThreadLocalInstanceTps`](https://github.com/alibaba/multi-thread-context/blob/master/src/test/java/com/alibaba/mtc/perf/tps/CreateMtContextThreadLocalInstanceTps.java)。

FAQ
=====================================

* Mac OS X下，使用javaagent，可能会报`JavaLaunchHelper`的出错信息。  
JDK Bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=8021205  
可以换一个版本的JDK。我的开发机上`1.7.0_40`有这个问题，`1.6.0_51`、`1.7.0_45`可以运行。   
\# `1.7.0_45`还是有`JavaLaunchHelper`的出错信息，但不影响运行。

相关资源
=====================================

* [Java Agent规范](http://docs.oracle.com/javase/6/docs/api/java/lang/instrument/package-summary.html)
* [Java SE 6 新特性: Instrumentation 新功能](http://www.ibm.com/developerworks/cn/java/j-lo-jse61/)
* [Creation, dynamic loading and instrumentation with javaagents](http://dhruba.name/2010/02/07/creation-dynamic-loading-and-instrumentation-with-javaagents/)
* [JavaAgent加载机制分析](http://alipaymiddleware.com/jvm/javaagent%E5%8A%A0%E8%BD%BD%E6%9C%BA%E5%88%B6%E5%88%86%E6%9E%90/)
* [Getting Started with Javassist](http://www.csg.ci.i.u-tokyo.ac.jp/~chiba/javassist/tutorial/tutorial.html)
