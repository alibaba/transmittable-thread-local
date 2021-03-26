#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

source ../ttl_build.sh

logAndRun "${JAVA_CMD[@]}" -cp "$(getClasspath)" \
    com.alibaba.perf.memoryleak.NoMemoryLeak_TransmittableThreadLocal_NoRemove
