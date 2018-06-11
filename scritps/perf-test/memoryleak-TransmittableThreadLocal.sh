#!/bin/bash

cd "$(dirname "$(readlink -f "$0")")"
source ../common.sh

runCmd $JAVA_CMD -cp $(getClasspath) \
    com.alibaba.ttl.perf.memoryleak.NoMemoryLeak_TransmittableThreadLocal_NoRemove
