#!/bin/bash

cd $(dirname $(readlink -f $0))
BASE=`pwd`

runCmd() {
    echo "$@"
    "$@"
}

mvn clean install -Dmaven.test.skip && mvn test-compile &&
mvn dependency:copy-dependencies -DincludeScope=provided &&
cd target && {
    classpath=`echo dependency/*.jar | tr ' ' :`

    runCmd java -javaagent:multithread-context-0.9.0-SNAPSHOT.jar \
    -cp $classpath:multithread-context-0.9.0-SNAPSHOT.jar:test-classes/ \
    com.oldratlee.mtc.threadpool.agent.AgentDemo
}
