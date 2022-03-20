#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

source ../bash-buddy/lib/trap_error_info.sh
source ../bash-buddy/lib/common_utils.sh

source ../ttl_build.sh

cu::log_then_run "${JAVA_CMD[@]}" -cp "$(getClasspath)" \
    com.alibaba.perf.memoryleak.NoMemoryLeak_TransmittableThreadLocal_NoRemove
