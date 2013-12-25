#!/bin/bash

cd $(dirname $(readlink -f $0))
BASE=`pwd`


redEcho() {
    if [ -c /dev/stdout ] ; then
        # if stdout is console, turn on color output.
        echo -ne "\033[1;31m"
        echo -n "$@"
        echo -e "\033[0m"
    else
        echo "$@"
    fi
}

runCmd() {
    redEcho "$@"
    "$@"
}

mvn clean install -Dmaven.test.skip && mvn test-compile &&
mvn dependency:copy-dependencies -DincludeScope=provided &&
mvn dependency:copy-dependencies -DincludeScope=test &&
cd target && {
    classpath=`echo dependency/*.jar | tr ' ' :`

    runCmd java \
    -Xmx384m -Xms384m \
    -cp test-classes:classes:${classpath} \
    -ea \
    com.alibaba.mtc.perf.tps.CreateMtContextThreadLocalInstanceTps
}
