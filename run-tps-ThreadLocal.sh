#!/bin/bash

cd $(dirname $(readlink -f $0))
BASE=`pwd`

. ./common.sh

cleanAndInstall && {
    classpath=`echo target/dependency/*.jar | tr ' ' :`
    
    runCmd java \
        -Xmx384m -Xms384m \
        -cp target/test-classes:target/classes:${classpath} \
        -ea \
        com.alibaba.mtc.perf.tps.CreateThreadLocalInstanceTps
}
