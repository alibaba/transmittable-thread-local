#!/bin/bash

cd $(dirname $(readlink -f $0))
BASE=`pwd`

runCmd() {
    echo "$@"
    "$@"
}

mvn clean install -Dmaven.test.skip && mvn test-compile &&
mvn dependency:copy-dependencies -DincludeScope=provided &&
mvn dependency:copy-dependencies -DincludeScope=runtime &&
cd target && {
    classpath=`echo dependency/*.jar | tr ' ' :`

    runCmd java -javaagent:multithread.context-0.9.0-SNAPSHOT.jar \
    -Xbootclasspath/a:$classpath:multithread.context-0.9.0-SNAPSHOT.jar:test-classes/   \
    com.alibaba.mtc.threadpool.agent.AgentDemo
}
