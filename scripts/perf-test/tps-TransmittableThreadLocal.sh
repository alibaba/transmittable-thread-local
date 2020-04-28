#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

source ../common_build.sh

runCmd "${JAVA_CMD[@]}" -cp "$(getClasspath)" \
    com.alibaba.perf.tps.CreateTransmittableThreadLocalInstanceTps
