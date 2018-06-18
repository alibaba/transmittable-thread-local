发布操作列表
===============================

1. 准备发布分支
    1. 如有 降开发版本，注意 修改 新加API的 **_`@since`_** ！！
    2. 从master分支新建发布分支
    3. 在发布分支上，更新版本号及相关信息
        - 更新POM的版本号，去掉`SNAPSHOT`
            - ./scritps/gen-pom4ide.sh 重新生成 pom4ide
        - 更新README
            - 更新badge的引用，由master分支名改成Tag名  
                `sed 's/master/v2.x.x/g' -i README*`
            - 示例Maven依赖的版本
            - 更新JavaDoc链接到固定版本  
                http://alibaba.github.io/transmittable-thread-local/apidocs/2.x.x/index.html
2. 新建并Push Tag，如`v2.x.x`  
    - `git tag -m 'release v2.x.x' v2.x.x`
    - `git push origin v2.x.x`
3. 等待Tag的CI通过 <https://travis-ci.org/alibaba/transmittable-thread-local/builds>
4. 发布版本到Maven中央库  
    `./mvnw clean && ./mvnw deploy -DperformRelease`
5. 更新JavaDoc
    1. 生成JavaDoc，更新到分支gh-pages
        - git checkout gh-pages
        - `mv target/apidocs apidocs/2.x.x`
    2. 修改`index.html`<http://alibaba.github.io/transmittable-thread-local/apidocs>的重定向到最新版本的JavaDoc
6. 编写Release Note： <https://github.com/alibaba/transmittable-thread-local/releases>
7. 升级Master分支的开发版本号
    - ./scritps/gen-pom4ide.sh 重新生成 pom4ide
    - 示例Maven依赖的版本
