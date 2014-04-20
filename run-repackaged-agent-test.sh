#!/bin/bash

cd $(dirname $(readlink -f $0))
BASE=`pwd`

. ./common.sh

# -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005 \

cleanInstall $1 && copyDeps $1 && {
    classpath=`echo target/dependency/*.jar | tr ' ' :` &&

    ./mtc-repackage-javassist.sh target/dependency/javassist* target/$aid-$version.jar && {
        jar -tf target/dependency/javassist* | grep 'com/alibaba/mtc' -Fq -m1 || {
            redEcho "Fail to repackage javassist jar!"
        }
    } &&

    runCmd java \
        -Xmx128m -Xms128m \
        -Xbootclasspath/a:target/$aid-$version.jar:`ls target/dependency/javassist*` \
        -javaagent:target/$aid-$version.jar \
        -cp target/test-classes:$classpath \
        -ea \
        com.alibaba.mtc.threadpool.agent.AgentCheck
}
