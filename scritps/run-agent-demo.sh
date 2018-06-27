#!/bin/bash

cd "$(dirname "$(readlink -f "$0")")"
source ./common.sh

runCmd "${JAVA_CMD[@]}" -cp "$(getClasspathWithoutTtlJar)" \
    "-Xbootclasspath/a:$(getTtlJarPath)" "-javaagent:$(getTtlJarPath)" \
    com.alibaba.demo.agent.AgentDemo
