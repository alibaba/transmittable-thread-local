#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

source ./common_build.sh

runCmd "${JAVA_CMD[@]}" -cp "$(getClasspathWithoutTtlJar)" \
    "-javaagent:$(getTtlJarPath)=ttl.agent.logger:STDOUT" \
    com.alibaba.demo.ttl.agent.AgentDemo
