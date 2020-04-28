#!/bin/bash
set -eEuo pipefail

[ -z "${1:-}" ] && die "need class name argument!"
readonly run_class_name="$1"
shift 1

cd "$(dirname "$(readlink -f "$0")")"
source ./common_build.sh

runCmd "${JAVA_CMD[@]}" -cp "$(getClasspathWithoutTtlJar)" \
    "-javaagent:$(getTtlJarPath)=ttl.agent.logger:STDOUT" \
     "$run_class_name"
