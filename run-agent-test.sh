#!/bin/bash

cd $(dirname $(readlink -f $0))
BASE=`pwd`

. ./common.sh

# -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005 \

cleanInstall $1 && copyDeps && {
    classpath=`echo target/dependency/*.jar | tr ' ' :`

    runCmd $JAVA_HOME/bin/java \
        -Xmx128m -Xms128m \
        -Xbootclasspath/a:target/$aid-$version.jar \
        -javaagent:target/$aid-$version.jar \
        -cp target/test-classes:$classpath \
        -ea \
        com.alibaba.ttl.threadpool.agent.check.AgentCheckMain
}
