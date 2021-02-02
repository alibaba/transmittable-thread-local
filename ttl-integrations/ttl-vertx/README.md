# `TTL Agent`扩展`Transformlet`实现的示例工程

## 扩展`Transformlet`的实现

为了提供`TTL Agent`扩展`Transformlet`，包含2部分：

1. `TTL Agent`扩展`Transformlet`的实现类：[`SampleExtensionTransformlet`](src/main/java/com/alibaba/ttl/agent/extension_transformlet/sample/transformlet/SampleExtensionTransformlet.java)。
    - 这个示例`Transformlet`修改了类[`ToBeTransformedClass`](src/main/java/com/alibaba/ttl/agent/extension_transformlet/sample/biz/ToBeTransformedClass.java)的`toBeTransformedMethod`方法：在修改方法前插入一行代码，修改方法参数值乘以2（`$1 *= 2;`）。
1. `TTL Agent`扩展`Transformlet`的配置文件：[`META-INF/ttl.agent.transformlets`](src/main/resources/META-INF/ttl.agent.transformlets)
    - 配置文件的内容是 扩展`Transformlet`实现类的全类名。  
      在这个示例工程是`com.alibaba.ttl.agent.extension_transformlet.sample.transformlet.SampleExtensionTransformlet`。
    - `TTL Agent`会扫描`Class Path`上的`META-INF/ttl.agent.transformlets`文件，自动发现并启用这些扩展`Transformlet`。  
      即只要将扩展`Transformlet`的依赖`Jar`引入到应用中就会自动生效。
    - 这个扫描并自动加载生效与`JDK`的[`ServiceLoader`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/ServiceLoader.html)一样，只是使用不同的扩展配置文件。

## 扩展`Transformlet`的测试与生效验证

单元测试类 在 [`ToBeTransformedClassTest`](src/test/java/com/alibaba/ttl/agent/extension_transformlet/sample/biz/ToBeTransformedClassTest.java)。

通过运行`Maven`单元测试验证扩展`Transformlet` `SampleExtensionTransformlet`是否生效：


```bash
# sample-ttl-agent-extension-transformlet 工程目录，执行

# 1. 先 mvn install TTL lib
(cd ../.. && mvn install -Dmaven.test.skip)

# 2. 验证 扩展Transformlet SampleExtensionTransformlet 是否生效
mvn test -Penable-TtlAgent-forTest
# 更多输出TTL的Transform类操作的日志
mvn test -Penable-TtlAgent-forTest -Penable-LogTransform-forTest
```

## 运行示例`SampleMain`

可以通过`Java`命令行参数来运行示例`SampleMain`：

```java
java -javaagent:path/to/transmittable-thread-local-2.x.y.jar \
    -cp target/classes \
    com.alibaba.ttl.agent.extension_transformlet.sample.biz.SampleMain
```

通过脚本[`scripts/run.sh`](scripts/run.sh)快速上面命令行的运行。
