#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

source bash-buddy/lib/trap_error_info.sh
source bash-buddy/lib/common_utils.sh

source ./ttl_build.sh

[ -z "${1:-}" ] && cu::die "need class name argument!"
readonly run_class_name="$1"
shift 1

cu::log_then_run "${JAVA_CMD[@]}" -cp "$(getClasspathWithoutTtlJar)" \
    "-javaagent:$(getTtlJarPath)=ttl.agent.logger:STDOUT" \
     "$run_class_name"
