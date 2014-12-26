multi-thread context(MTC)
=====================================

[![Build Status](https://travis-ci.org/alibaba/multi-thread-context.svg?branch=master)](https://travis-ci.org/alibaba/multi-thread-context) [![Coverage Status](https://coveralls.io/repos/alibaba/multi-thread-context/badge.png?branch=master)](https://coveralls.io/r/alibaba/multi-thread-context?branch=master) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.alibaba/multithread.context/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.alibaba/multithread.context/)

<div align="right">
<a href="README-EN.md">English Documentation</a>
</div>

:wrench: 功能
----------------------------

:point_right: 在使用线程池等会缓存线程的组件情况下，完成多线程的Context传递。

`JDK`的[`java.lang.InheritableThreadLocal`](http://docs.oracle.com/javase/6/docs/api/java/lang/InheritableThreadLocal.html)类可以完成父子线程的Context传递。

但对于使用线程池等会缓存线程的组件的情况，线程由线程池创建好，并且线程是缓存起来反复使用的。这时父子线程关系的上下文传递已经没有意义，应用中要做上下文传递，实际上是在把 **任务提交给线程池时**的上下文传递到 **任务执行时**。

如有问题欢迎：

- [提交Issue](https://github.com/alibaba/multi-thread-context/issues) 
- [发到邮件列表](http://mtc.59504.x6.nabble.com/) （[nabble](http://www.nabble.com/)提供）
  - 点击【选项】-【通过电子邮箱发布】（即[这个页面](http://mtc.59504.x6.nabble.com/template/NamlServlet.jtp?macro=subscribe&node=1)），来获得这个邮件列表的地址来发布信息。
  - 点击【选项】-【通过邮件订阅】（即[这个页面](http://mtc.59504.x6.nabble.com/template/NamlServlet.jtp?macro=subscribe&node=1)），来订阅这个邮件列表。
  - 可以通过邮件或是[论坛界面](http://mtc.59504.x6.nabble.com/)来发布和查看信息。

:art: 需求场景
----------------------------

### 应用容器或上层框架跨应用代码给下层SDK传递信息

举个场景，`App Engine`（`PAAS`）上会运行由应用提供商提供的应用（`SAAS`模式）。多个`SAAS`用户购买并使用这个应用（即`SAAS`应用）。`SAAS`应用往往是一个实例为多个`SAAS`用户提供服务。    
\# 另一种模式是：`SAAS`用户使用完全独立一个`SAAS`应用，包含独立应用实例及其后的数据源（如`DB`、缓存，etc）。

需要避免的`SAAS`应用拿到多个`SAAS`用户的数据。

一个解决方法是处理过程关联一个`SAAS`用户的上下文，在上下文中应用只能处理（读&写）这个`SAAS`用户的数据。

请求由`SAAS`用户发起（如从`Web`请求进入`App Engine`），`App Engine`可以知道是从哪个`SAAS`用户，在`Web`请求时在上下文中设置好`SAAS`用户`ID`。

应用处理数据（`DB`、`Web`、消息 etc.）是通过`App Engine`提供的服务`SDK`来完成。当应用处理数据时，`SDK`检查数据所属的`SAAS`用户是否和上下文中的`SAAS`用户`ID`一致，如果不一致则拒绝数据的读写。

应用代码会使用线程池，并且这样的使用是正常的业务需求。`SAAS`用户`ID`的从要`App Engine`传递到下层`SDK`，要支持这样的用法。

### 日志记录系统上下文

`App Engine`的日志（如，`SDK`会记录日志）要记录系统上下文。由于不限制用户应用使用线程池，系统的上下文需要能跨线程的传递，且不影响应用代码。

### 上面场景使用`MTC`的整体构架

<img src="https://raw.github.com/wiki/alibaba/multi-thread-context/mtc-arch.png" alt="构架图" width="260" />

构架涉及3个角色：容器、用户应用、`SDK`。

整体流程：

1. 请求进入`PAAS`容器，提取上下文信息并设置好上下文。
2. 进入用户应用处理业务，业务调用`SDK`（如`DB`、消息、etc）。    
用户应用会使用线程池，所以调用`SDK`的线程可能不是请求的线程。
3. 进入`SDK`处理。    
提取上下文的信息，决定是否符合拒绝处理。

整个过程中，上下文的传递 对于 **用户应用代码** 期望是透明的。

:notebook: User Guide
=====================================

使用类[`MtContextThreadLocal`](src/main/java/com/alibaba/mtc/MtContextThreadLocal.java)来保存上下文，并跨线程池传递。

[`MtContextThreadLocal`](src/main/java/com/alibaba/mtc/MtContextThreadLocal.java)继承[`java.lang.InheritableThreadLocal`](http://docs.oracle.com/javase/6/docs/api/java/lang/InheritableThreadLocal.html)，使用方式也类似。

比[`java.lang.InheritableThreadLocal`](http://docs.oracle.com/javase/6/docs/api/java/lang/InheritableThreadLocal.html)，添加了`protected`方法`copy`，用于定制 **任务提交给线程池时**的上下文传递到 **任务执行时**时的拷贝行为，缺省是传递的是引用。

具体使用方式见下面的说明。

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

但对于使用了异步执行（往往使用线程池完成）的情况，线程由线程池创建好，并且线程是缓存起来反复使用的。

这时父子线程关系的上下文传递已经没有意义，应用中要做上下文传递，实际上是在把 **任务提交给线程池时**的上下文传递到 **任务执行时**。
解决方法参见下面的这几种用法。

2. 保证线程池中传递Context
----------------------------

### 2.1 修饰`Runnable`和`Callable`

使用[`com.alibaba.mtc.MtContextRunnable`](src/main/java/com/alibaba/mtc/MtContextRunnable.java)和[`com.alibaba.mtc.MtContextCallable`](src/main/java/com/alibaba/mtc/MtContextCallable.java)来修饰传入线程池的`Runnable`和`Callable`。

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

<img src="https://raw.github.com/wiki/alibaba/multi-thread-context/SequenceDiagram.png" alt="时序图" width="600" />

### 2.2 修饰线程池

省去每次`Runnable`和`Callable`传入线程池时的修饰，这个逻辑可以在线程池中完成。

通过工具类[`com.alibaba.mtc.threadpool.MtContextExecutors`](src/main/java/com/alibaba/mtc/threadpool/MtContextExecutors.java)完成，有下面的方法：

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

### 2.3 使用Java Agent来修饰JDK线程池实现类

这种方式，实现线程池的`MtContext`传递过程中，代码中没有修饰`Runnble`或是线程池的代码。    
\# 即可以做到应用代码 **无侵入**，后面文档有结合实际场景的架构对这一点的说明。

示例代码：

```java
// 框架代码
MtContextThreadLocal<String> parent = new MtContextThreadLocal<String>();
parent.set("value-set-in-parent");

// 应用代码
ExecutorService executorService = Executors.newFixedThreadPool(3);

Runnable task = new Task("1");
Callable call = new Call("2");
executorService.submit(task);
executorService.submit(call);

// =====================================================

// Task或是Call中可以读取, 值是"value-set-in-parent"
String value = parent.get();
```

Demo参见[`AgentDemo.java`](src/test/java/com/alibaba/mtc/threadpool/agent/demo/AgentDemo.java)。

目前Agent中，修饰了`jdk`中的两个线程池实现类（实现代码在[`MtContextTransformer.java`](src/main/java/com/alibaba/mtc/threadpool/agent/MtContextTransformer.java)）：

- `java.util.concurrent.ThreadPoolExecutor`
- `java.util.concurrent.ScheduledThreadPoolExecutor`

在`Java`的启动参数加上：

- `-Xbootclasspath/a:/path/to/multithread.context-1.1.0.jar`
- `-javaagent:/path/to/multithread.context-1.1.0.jar`

**注意**： 

* Agent修改是JDK的类，类中加入了引用`MTC`的代码，所以`MTC Agent`的`Jar`要加到`bootclasspath`上。

Java命令行示例如下：

```bash
java -Xbootclasspath/a:multithread.context-1.1.0.jar \
    -javaagent:multithread.context-1.1.0-SNAPSHOT.jar \
    -cp classes \
    com.alibaba.mtc.threadpool.agent.demo.AgentDemo
```

有Demo演示『使用Java Agent来修饰线程池实现类』，执行工程下的脚本[`run-agent-demo.sh`](run-agent-demo.sh)即可运行Demo。

#### 什么情况下，`Java Agent`的使用方式`MtContext`会失效？

由于`Runnable`和`Callable`的修饰代码，是在线程池类中插入的。下面的情况会让插入的代码被绕过，`MtContext`会失效。

- 用户代码中继承`java.util.concurrent.ThreadPoolExecutor`和`java.util.concurrent.ScheduledThreadPoolExecutor`，
覆盖了`execute`、`submit`、`schedule`等提交任务的方法，并且没有调用父类的方法。   
修改线程池类的实现，`execute`、`submit`、`schedule`等提交任务的方法禁止这些被覆盖，可以规避这个问题。
- 目前，没有修饰`java.util.Timer`类，使用`Timer`时，`MtContext`会有问题。

:mortar_board: Developer Guide
=====================================

`Java Agent`方式对应用代码无侵入
----------------------------

相对修饰`Runnble`或是线程池的方式，`Java Agent`方式为什么是应用代码无侵入的？

<img src="https://raw.github.com/wiki/alibaba/multi-thread-context/mtc-arch.png" alt="构架图" width="260" />

按框架图，把前面示例代码操作可以分成下面几部分：

1. 读取信息设置到`MtContext`。    
这部分在容器中完成，无需应用参与。
2. 提交`Runnable`到线程池。要有修饰操作`Runnable`（无论是直接修饰`Runnble`还是修饰线程池）。    
这部分操作一定是在用户应用中触发。
3. 读取`MtContext`，做业务检查。    
在`SDK`中完成，无需应用参与。

只有第2部分的操作和应用代码相关。

如果不通过`Java Agent`修饰线程池，则修饰操作需要应用代码来完成。

使用`Java Agent`方式，应用无需修改代码，即做到 相对应用代码 透明地完成跨线程池的上下文传递。

如何权衡`Java Agent`方式的失效情况
----------------------------

把这些失效情况都解决了是最好的，但复杂化了实现。下面是一些权衡：

- 不推荐使用`Timer`类，推荐用`ScheduledThreadPoolExecutor`。
`ScheduledThreadPoolExecutor`实现更强壮，并且功能更丰富。
如支持配置线程池的大小（`Timer`只有一个线程）；`Timer`在`Runnable`中抛出异常会中止定时执行。
- 覆盖了`execute`、`submit`、`schedule`的问题的权衡是：
业务上没有修改这些方法的需求。并且线程池类提供了`beforeExecute`方法用于插入扩展的逻辑。

已有Java Agent中嵌入`MtContext Agent`
----------------------------

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

注意还是要在`bootclasspath`上，加上`MtContext`依赖的2个Jar：

```bash
-Xbootclasspath/a:/path/to/multithread.context-1.1.0.jar:/path/to/your/agent/jar/files
```

Bootstrap上添加通用库的`Jar`的问题及解决方法
----------------------------

通过`Java`命令参数`-Xbootclasspath`把库的`Jar`加`Bootstrap` `ClassPath`上。`Bootstrap` `ClassPath`上的`Jar`中类会优先于应用`ClassPath`的`Jar`被加载，并且不能被覆盖。

`MTC`在`Bootstrap` `ClassPath`上添加了`Javassist`的依赖，如果应用中如果使用了`Javassist`，实际上会优先使用`Bootstrap` `ClassPath`上的`Javassist`，即应用不能选择`Javassist`的版本，应用需要的`Javassist`和`MTC`的`Javassist`有兼容性的风险。

可以通过`repackage`（重新命名包名）来解决这个问题。

`Maven`提供了[Shade](http://maven.apache.org/plugins/maven-shade-plugin/)插件，可以完成`repackage`操作，并把`Javassist`的类加到`MTC`的`Jar`中。

这样就不需要依赖外部的`Javassist`依赖，也规避了依赖冲突的问题。

:electric_plug: Java API Docs
======================

当前版本的Java API文档地址： <http://alibaba.github.io/multi-thread-context/apidocs/>

:cookie: Maven依赖
=====================================

示例：

```xml
<dependency>
	<groupId>com.alibaba</groupId>
	<artifactId>multithread.context</artifactId>
	<version>1.1.0</version>
</dependency>
```

可以在 [search.maven.org](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.alibaba%22%20AND%20a%3A%22multithread.context%22) 查看可用的版本。

:umbrella: 性能测试
=====================================

内存泄漏
----------------------------

对比测试[`MtContextThreadLocal`](src/main/java/com/alibaba/mtc/MtContextThreadLocal.java)和[`ThreadLocal`](http://docs.oracle.com/javase/6/docs/api/java/lang/ThreadLocal.html)，测试Case是：

简单一个线程一直循环`new` `MtContextThreadLocal`、`ThreadLocal`实例，不主动做任何清理操作，即不调用`ThreadLocal`的`remove`方法主动清空。

### 验证结果

都可以持续运行，不会出内存溢出`OutOfMemoryError`。

### 执行方式

可以通过执行工程下的脚本来运行Case验证：

* 脚本[`run-memoryleak-ThreadLocal.sh`](run-memoryleak-ThreadLocal.sh)运行`ThreadLocal`的测试。  
测试类是[`NoMemoryLeak_ThreadLocal_NoRemove`](src/test/java/com/alibaba/mtc/perf/memoryleak/NoMemoryLeak_ThreadLocal_NoRemove.java)。
* 脚本[`run-memoryleak-MtContextThreadLocal.sh`](run-memoryleak-MtContextThreadLocal.sh)运行`MtContextThreadLocal`的测试。
测试类是[`NoMemoryLeak_MtContextThreadLocal_NoRemove`](src/test/java/com/alibaba/mtc/perf/memoryleak/NoMemoryLeak_MtContextThreadLocal_NoRemove.java)。

TPS & 压力测试
----------------------------

对比测试[`MtContextThreadLocal`](src/main/java/com/alibaba/mtc/MtContextThreadLocal.java)和[`ThreadLocal`](http://docs.oracle.com/javase/6/docs/api/java/lang/ThreadLocal.html)，测试Case是：

2个线程并发一直循环`new` `MtContextThreadLocal`、`ThreadLocal`实例，不主动做任何清理操作，即不调用`ThreadLocal`的`remove`方法主动清空。

### 验证结果

在我的4核开发机上运行了24小时，稳定正常。

TPS结果如下：

`ThreadLocal`的TPS稳定在～41K：

```bash
......
tps: 42470
tps: 40940
tps: 41041
tps: 40408
tps: 40610
```

`MtContextThreadLocal`的TPS稳定在～40K：

```bash
......
tps: 40461 
tps: 40101 
tps: 39989 
tps: 40684 
tps: 41174 
```

GC情况如下（1分钟输出一次）：

`ThreadLocal`的每分钟GC时间是`5.45s`，FGC次数是`0.09`：

```bash
   S0     S1      E      O      P    YGC      YGCT     FGC     FGCT   GCT
......
  0.00  97.66   0.00   8.33  12.70 1470935 2636.215    41    0.229 2636.444
 97.66   0.00   0.00  17.18  12.70 1473968 2640.597    41    0.229 2640.825
 98.44   0.00   0.00  25.47  12.70 1477020 2645.265    41    0.229 2645.493
 96.88   0.00  33.04  34.03  12.70 1480068 2650.149    41    0.229 2650.378
  0.00  97.66  14.01  41.82  12.70 1483113 2655.262    41    0.229 2655.490
  0.00  97.66  74.07  50.25  12.70 1486149 2660.596    41    0.229 2660.825
 96.88   0.00   0.00  58.32  12.70 1489170 2666.135    41    0.229 2666.364
 98.44   0.00  26.07  67.05  12.70 1492162 2671.841    41    0.229 2672.070
  0.00  97.66   0.00  76.50  12.70 1495139 2677.809    41    0.229 2678.038
  0.00  97.66   0.00  85.95  12.70 1498091 2683.994    41    0.229 2684.222
 96.88   0.00   0.00  96.50  12.70 1501038 2690.454    41    0.229 2690.683
 97.66   0.00   0.00   7.96  12.70 1504054 2695.583    42    0.233 2695.816
  0.00  97.66   0.00  17.46  12.70 1507099 2700.009    42    0.233 2700.241
  0.00  97.66   0.00  26.97  12.70 1510133 2704.652    42    0.233 2704.885
 97.66   0.00   0.00  36.57  12.70 1513158 2709.592    42    0.233 2709.825
  0.00  97.66   0.00  45.59  12.70 1516167 2714.738    42    0.233 2714.971
 98.44   0.00   0.00  54.49  12.70 1519166 2720.109    42    0.233 2720.342
  0.00  98.44   0.00  63.52  12.70 1522141 2725.688    42    0.233 2725.921
  0.00  97.66  84.18  72.00  12.70 1525139 2731.579    42    0.233 2731.812
  0.00  98.44  20.04  80.10  12.70 1528121 2737.680    42    0.233 2737.913
  0.00  97.66  28.06  87.70  12.70 1531093 2743.991    42    0.233 2744.224
  0.00  98.44   0.00  95.63  12.70 1534055 2750.508    42    0.233 2750.741
 97.66   0.00   0.00   4.75  12.70 1537062 2756.196    43    0.239 2756.435
```

`MtContextThreadLocal`的每分钟GC时间是`5.29s`，FGC次数是`3.27`：

```bash
   S0     S1      E      O      P    YGC      YGCT     FGC     FGCT   GCT
......
  0.00  98.44   8.01  57.38  12.80 1390879 2571.496  1572    9.820 2581.315
  0.00  97.66   0.00  78.87  12.80 1393725 2576.784  1575    9.839 2586.623
 98.44   0.00  14.04   5.83  12.80 1396559 2582.082  1579    9.866 2591.948
 98.44   0.00   0.00  26.41  12.80 1399394 2587.274  1582    9.885 2597.159
 98.44  98.44   0.00  50.75  12.80 1402230 2592.506  1585    9.904 2602.410
 98.44   0.00   0.00  84.37  12.80 1405077 2597.808  1588    9.925 2607.733
  0.00  98.44   0.00   5.19  12.80 1407926 2603.108  1592    9.952 2613.059
  0.00  98.44  58.17  29.80  12.80 1410770 2608.314  1595    9.973 2618.287
 99.22   0.00   0.00  54.14  12.80 1413606 2613.582  1598    9.992 2623.574
 98.44   0.00   0.00  78.18  12.80 1416444 2618.881  1601   10.012 2628.893
  0.00  97.66   0.00   7.36  12.80 1419275 2624.167  1605   10.038 2634.205
  0.00  99.22   0.00  31.04  12.80 1422125 2629.391  1608   10.057 2639.448
  0.00  98.44   0.00  60.41  12.80 1424974 2634.636  1611   10.077 2644.714
  0.00  98.44   0.00  84.72  12.80 1427825 2639.929  1614   10.094 2650.024
  0.00  97.66   0.00  12.32  12.80 1430679 2645.204  1618   10.119 2655.323
  0.00  98.44  12.05  39.31  12.80 1433539 2650.442  1621   10.141 2660.583
 86.81   0.00   0.00  67.40  12.80 1436392 2655.743  1624   10.156 2665.899
 99.22   0.00   0.00  95.25  12.80 1439244 2661.071  1627   10.175 2671.246
 98.44   0.00   0.00  24.63  12.80 1442090 2666.305  1631   10.201 2676.506
  0.00  99.22   0.00  52.86  12.80 1444945 2671.546  1634   10.222 2681.769
 98.44   0.00   0.00  80.38  12.80 1447802 2676.850  1637   10.241 2687.091
  0.00  87.50   0.00   4.22  12.80 1450658 2682.196  1641   10.268 2692.464
 99.22   0.00   0.00  33.22  12.80 1453507 2687.386  1644   10.290 2697.676
```

#### TPS略有下降的原因分析

使用`jvisualvm` Profile方法耗时，`MtContextThreadLocal`Case的热点方法和`ThreadLocal`Case一样。

略有下降可以认为是Full GC更多引起。

实际使用场景中，`MtContextThreadLocal`实例个数非常有限，不会有性能问题。

#### FGC次数增多的原因分析

在`MtContextThreadLocal.holder`中，持有`MtContextThreadLocal`实例的弱引用，减慢实例的回收，导致Full GC增加。

实际使用场景中，`MtContextThreadLocal`实例个数非常有限，不会有性能问题。

### 执行方式

可以通过执行工程下的脚本来运行Case验证：

* 脚本[`run-tps-ThreadLocal.sh`](run-tps-ThreadLocal.sh)运行`ThreadLocal`的测试。  
测试类是[`CreateThreadLocalInstanceTps`](src/test/java/com/alibaba/mtc/perf/tps/CreateThreadLocalInstanceTps.java)。
* [`run-tps-MtContextThreadLocal.sh`](run-tps-MtContextThreadLocal.sh)运行`MtContextThreadLocal`的测试。
测试类是[`CreateMtContextThreadLocalInstanceTps`](src/test/java/com/alibaba/mtc/perf/tps/CreateMtContextThreadLocalInstanceTps.java)。

:question: FAQ
=====================================

* Mac OS X下，使用javaagent，可能会报`JavaLaunchHelper`的出错信息。  
JDK Bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=8021205  
可以换一个版本的JDK。我的开发机上`1.7.0_40`有这个问题，`1.6.0_51`、`1.7.0_45`可以运行。   
\# `1.7.0_45`还是有`JavaLaunchHelper`的出错信息，但不影响运行。

:books: 相关资料
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

Shade插件
----------------------------

* `Maven`的[Shade](http://maven.apache.org/plugins/maven-shade-plugin/)插件
