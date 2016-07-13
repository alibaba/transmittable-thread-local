#!/bin/bash

cd $(dirname $(readlink -f $0))
BASE=`pwd`

. ./common.sh

cleanInstall && copyDeps && {
    classpath=`echo target/dependency/*.jar | tr ' ' :`

        # -Xnoagent -Djava.compiler=NONE -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=9055 \
    runCmd java \
        -Xmx128m -Xms128m \
        -Xbootclasspath/a:target/$aid-$version.jar \
        -javaagent:target/$aid-$version.jar \
        -cp target/test-classes:$classpath \
        -ea \
        com.alibaba.ttl.threadpool.agent.demo.AgentDemo
}
