# :mortar_board: Developer Guide

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->


- [关于`Java Agent`](#%E5%85%B3%E4%BA%8Ejava-agent)
    - [`Java Agent`方式对应用代码无侵入](#java-agent%E6%96%B9%E5%BC%8F%E5%AF%B9%E5%BA%94%E7%94%A8%E4%BB%A3%E7%A0%81%E6%97%A0%E4%BE%B5%E5%85%A5)
    - [如何权衡`Java Agent`方式的失效情况](#%E5%A6%82%E4%BD%95%E6%9D%83%E8%A1%A1java-agent%E6%96%B9%E5%BC%8F%E7%9A%84%E5%A4%B1%E6%95%88%E6%83%85%E5%86%B5)
    - [已有Java Agent中嵌入`TTL Agent`](#%E5%B7%B2%E6%9C%89java-agent%E4%B8%AD%E5%B5%8C%E5%85%A5ttl-agent)
- [Bootstrap上添加通用库的`Jar`的问题及解决方法](#bootstrap%E4%B8%8A%E6%B7%BB%E5%8A%A0%E9%80%9A%E7%94%A8%E5%BA%93%E7%9A%84jar%E7%9A%84%E9%97%AE%E9%A2%98%E5%8F%8A%E8%A7%A3%E5%86%B3%E6%96%B9%E6%B3%95)
- [:books: 相关资料](#books-%E7%9B%B8%E5%85%B3%E8%B5%84%E6%96%99)
    - [Jdk core classes](#jdk-core-classes)
    - [Java Agent](#java-agent)
    - [Javassist](#javassist)
    - [Shade插件](#shade%E6%8F%92%E4%BB%B6)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# 关于`Java Agent`

## `Java Agent`方式对应用代码无侵入

相对修饰`Runnable`或是线程池的方式，`Java Agent`方式为什么是应用代码无侵入的？

<img src="scenario-framework-sdk-arch.png" alt="构架图" width="260" />

按框架图，把前面示例代码操作可以分成下面几部分：

1. 读取信息设置到`TTL`。    
这部分在容器中完成，无需应用参与。
2. 提交`Runnable`到线程池。要有修饰操作`Runnable`（无论是直接修饰`Runnable`还是修饰线程池）。    
这部分操作一定是在用户应用中触发。
3. 读取`TTL`，做业务检查。    
在`SDK`中完成，无需应用参与。

只有第2部分的操作和应用代码相关。

如果不通过`Java Agent`修饰线程池，则修饰操作需要应用代码来完成。

使用`Java Agent`方式，应用无需修改代码，即做到 相对应用代码 透明地完成跨线程池的上下文传递。

## 如何权衡`Java Agent`方式的失效情况

把这些失效情况都解决了是最好的，但复杂化了实现。下面是一些权衡：

- 不推荐使用`Timer`类，推荐用`ScheduledThreadPoolExecutor`。
`ScheduledThreadPoolExecutor`实现更强壮，并且功能更丰富。
如支持配置线程池的大小（`Timer`只有一个线程）；`Timer`在`Runnable`中抛出异常会中止定时执行。
- 覆盖了`execute`、`submit`、`schedule`的问题的权衡是：
业务上没有修改这些方法的需求。并且线程池类提供了`beforeExecute`方法用于插入扩展的逻辑。

## 已有Java Agent中嵌入`TTL Agent`

这样可以减少Java命令上Agent的配置。

在自己的`ClassFileTransformer`中调用`TtlTransformer`，示例代码如下：

```java
public class TransformerAdaptor implements ClassFileTransformer {
    final TtlTransformer ttlTransformer = new TtlTransformer();

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {
        final byte[] transform = ttlTransformer.transform(
            loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
        if (transform != null) {
            return transform;
        }

        // Your transform code ...

        return null;
    }
}
```

注意还是要在`bootclasspath`上，加上`TTL`依赖的2个Jar：

```bash
-Xbootclasspath/a:/path/to/transmittable-thread-local-2.0.0.jar:/path/to/your/agent/jar/files
```

# Bootstrap上添加通用库的`Jar`的问题及解决方法

通过`Java`命令参数`-Xbootclasspath`把库的`Jar`加`Bootstrap` `ClassPath`上。`Bootstrap` `ClassPath`上的`Jar`中类会优先于应用`ClassPath`的`Jar`被加载，并且不能被覆盖。

`TTL`在`Bootstrap` `ClassPath`上添加了`Javassist`的依赖，如果应用中如果使用了`Javassist`，实际上会优先使用`Bootstrap` `ClassPath`上的`Javassist`，即应用不能选择`Javassist`的版本，应用需要的`Javassist`和`MTC`的`Javassist`有兼容性的风险。

可以通过`repackage`（重新命名包名）来解决这个问题。

`Maven`提供了[Shade](http://maven.apache.org/plugins/maven-shade-plugin/)插件，可以完成`repackage`操作，并把`Javassist`的类加到`TTL`的`Jar`中。

这样就不需要依赖外部的`Javassist`依赖，也规避了依赖冲突的问题。

# :books: 相关资料

## Jdk core classes

* [WeakHashMap](http://docs.oracle.com/javase/7/docs/api/java/util/WeakHashMap.html)
* [InheritableThreadLocal](http://docs.oracle.com/javase/7/docs/api/java/lang/InheritableThreadLocal.html)

## Java Agent

* [Java Agent规范](http://docs.oracle.com/javase/7/docs/api/java/lang/instrument/package-summary.html)
* [Java SE 6 新特性: Instrumentation 新功能](http://www.ibm.com/developerworks/cn/java/j-lo-jse61/)
* [Creation, dynamic loading and instrumentation with javaagents](http://dhruba.name/2010/02/07/creation-dynamic-loading-and-instrumentation-with-javaagents/)
* [JavaAgent加载机制分析](http://alipaymiddleware.com/jvm/javaagent%E5%8A%A0%E8%BD%BD%E6%9C%BA%E5%88%B6%E5%88%86%E6%9E%90/)

## Javassist

* [Getting Started with Javassist](http://www.csg.ci.i.u-tokyo.ac.jp/~chiba/javassist/tutorial/tutorial.html)

## Shade插件

* `Maven`的[Shade](http://maven.apache.org/plugins/maven-shade-plugin/)插件
