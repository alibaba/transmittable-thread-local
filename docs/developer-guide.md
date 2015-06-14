:mortar_board: Developer Guide
=====================================

`Java Agent`方式对应用代码无侵入
----------------------------

相对修饰`Runnble`或是线程池的方式，`Java Agent`方式为什么是应用代码无侵入的？

<img src="mtc-arch.png" alt="构架图" width="260" />

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
