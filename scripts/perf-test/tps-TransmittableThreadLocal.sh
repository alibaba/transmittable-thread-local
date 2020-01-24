#!/bin/bash

cd "$(dirname "$(readlink -f "$0")")"
source ../common.sh

runCmd "${JAVA_CMD[@]}" -cp "$(getCoreModuleClasspath)" \
    com.alibaba.perf.tps.CreateTransmittableThreadLocalInstanceTps
