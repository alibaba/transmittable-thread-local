#!/bin/bash

cd $(dirname $(readlink -f $0))
BASE=`pwd`

. ./common.sh

classpath=`echo target/dependency/*.jar | tr ' ' :`

runJava java \
-Xmx384m -Xms384m \
-cp target/test-classes:target/classes:${classpath} \
-ea \
com.alibaba.mtc.perf.memoryleak.NoMemoryLeak_MtContextThreadLocal_NoRemove
