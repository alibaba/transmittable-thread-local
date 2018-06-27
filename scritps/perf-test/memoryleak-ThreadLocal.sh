#!/bin/bash

cd "$(dirname "$(readlink -f "$0")")"
source ../common.sh

runCmd "${JAVA_CMD[@]}" -cp "$(getClasspathWithoutTtlJar)" \
    com.alibaba.ttl.perf.memoryleak.NoMemoryLeak_ThreadLocal_NoRemove
