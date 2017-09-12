#!/bin/bash

cd $(dirname $(readlink -f $0))
BASE=`pwd`

. ./common.sh

cleanInstall && copyDeps && {
    classpath=`echo target/dependency/*.jar | tr ' ' :`
    
    runCmd $JAVA_HOME/bin/java \
        -Xmx384m -Xms384m \
        -cp target/test-classes:target/classes:${classpath} \
        -ea \
        com.alibaba.ttl.perf.memoryleak.NoMemoryLeak_TransmittableThreadLocal_NoRemove
}
