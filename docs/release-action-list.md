发布操作列表
===============================

1. 准备发布分支
    1. 如有 降开发版本，注意 修改 新加API的 **_`@since`_** ！！
    2. 从`master`分支新建发布分支
    3. 在发布分支上，更新版本号及相关信息
        - 更新`POM`的版本号，去掉`SNAPSHOT`
            - `./scripts/gen-pom4ide.sh` 重新生成 `pom4ide`
        - 更新`README`
            - 更新badge的引用，由master分支名改成Tag名  
                - `sed 's/master/v2.x.x/g' -i README*`
                - `javadoc` badge的JavaDoc链接到固定版本  
                    https://alibaba.github.io/transmittable-thread-local/apidocs/2.x.x/index.html
            - 示例`Maven`依赖的版本
            - 更新`JavaDoc`链接到固定版本
2. 新建并Push Tag，如`v2.x.x`  
    - `git tag -m 'release v2.x.x' v2.x.x`
    - `git push origin v2.x.x`
3. 等待Tag的CI通过 <https://travis-ci.org/alibaba/transmittable-thread-local/builds>
3. 执行`scripts/checke-japi-compliance.sh`，检查`API`兼容性
4. 发布版本到`Maven`中央库  
    `./mvnw clean && ./mvnw deploy -DperformRelease`
5. 更新`JavaDoc`
    1. 生成`JavaDoc`，更新到分支`gh-pages`
        - `git checkout gh-pages`
        - `mv target/apidocs apidocs/2.x.x`
    2. 修改`index.html`<http://alibaba.github.io/transmittable-thread-local/apidocs>的重定向到最新版本的`JavaDoc`
6. 编写Release Note： <https://github.com/alibaba/transmittable-thread-local/releases>
7. 升级`Master`分支的开发版本号
    - `./scripts/gen-pom4ide.sh` 重新生成 `pom4ide`
    - 示例`Maven`依赖的版本
