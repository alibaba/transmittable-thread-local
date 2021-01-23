# TTL Agent扩展Transformlet的示例工程

- 扩展`Transformlet`实现类：[`SampleExtensionTransformlet`](src/main/java/com/alibaba/ttl/agent/extension_transformlet/sample/transformlet/SampleExtensionTransformlet.java)
    - 修改的类是[`ToBeTransformedClass`](src/main/java/com/alibaba/ttl/agent/extension_transformlet/sample/biz/ToBeTransformedClass.java)
    - 修改内容是 `ToBeTransformedClass`的`toBeTransformedMethod`方法：   
      在方法前插入一行代码，参数值乘以2（`$1 *= 2;`）。
- 单元测试类：[`ToBeTransformedClassTest`](src/test/java/com/alibaba/ttl/agent/extension_transformlet/sample/biz/ToBeTransformedClassTest.java)


# 扩展Transformlet的开启配置

通过TTL Agent参数`ttl.agent.extension.transformlet.list` 开启这个TTL Agent扩展Transformlet（`SampleExtensionTransformlet`），Java命令行参数：

```java
java -javaagent:path/to/transmittable-thread-local-2.x.y.jar=ttl.agent.extension.transformlet.list:com.alibaba.ttl.agent.extension_transformlet.sample.transformlet.SampleExtensionTransformlet \
    -cp target/classes \
    com.alibaba.ttl.agent.extension_transformlet.sample.biz.SampleMain
```

# 通过单元测试验证生效

可以运行单元测试 确认这个TTL Agent扩展Transformlet（`SampleExtensionTransformlet`）生效了：

```bash
# 先 mvn install TTL lib

# 不开启TTL
mvn test

# 开启TTL 与 SampleExtensionTransformlet
mvn test -Penable-TtlAgent-forTest

# 更多输出TTL的Transform类操作的日志
mvn test -Penable-TtlAgentAndLogTransform-forTest

# 更多输出JVM的Class加载日志
mvn test -Penable-TtlAgentAndLogTransform-javaVerboseClass-forTest
```
