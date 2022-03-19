发布操作列表
===============================

1. 准备发布分支
    1. 如`POM`中有降开发版本，注意 修改 新加`API`的 **_`@since`_** ！！
    2. 从`master`分支新建发布分支
    3. 在发布分支上，更新版本号及相关信息
        - 更新`POM`的版本号，去掉`SNAPSHOT`
            - [`scripts/gen-pom4ide.sh`](../scripts/gen-pom4ide.sh) 重新生成 `pom4ide`
        - 更新`README`
            - 更新badge的引用，由master分支名改成Tag名  
                - `sed 's/master/v2.x.y/g' -i README*`
                - `javadoc` badge的JavaDoc链接到固定版本  
                    https://alibaba.github.io/transmittable-thread-local/apidocs/2.x.y/index.html
            - 示例`Maven`依赖的版本
            - 更新`JavaDoc`链接到固定版本
2. 新建并Push Tag，如`v2.x.y`  
    - `git tag -m 'release v2.x.y' v2.x.y`
    - `git push origin v2.x.y`
3. 等待Tag的CI通过 https://ci.appveyor.com/project/oldratlee/transmittable-thread-local/history
4. 执行[`scripts/check-japi-compliance.sh`](../scripts/check-japi-compliance.sh)，检查`API`兼容性
5. 发布版本到`Maven`中央库  
    `./mvnw clean && ./mvnw deploy -DperformRelease`
6. 更新`JavaDoc`
    1. 生成`JavaDoc`，更新到分支`gh-pages`
        - `git checkout gh-pages`
        - `mv target/apidocs apidocs/2.x.y`
    2. 修改`index.html`<https://alibaba.github.io/transmittable-thread-local/apidocs>的重定向到最新版本的`JavaDoc`
7. 编写Release Note： <https://github.com/alibaba/transmittable-thread-local/releases>
8. 升级`Master`分支的开发版本号
    - [`scripts/gen-pom4ide.sh`](../scripts/gen-pom4ide.sh) 重新生成 `pom4ide`
    - 更新 `README`中的示例`Maven`依赖版本
