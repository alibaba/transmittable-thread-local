#!/bin/bash

cd "$(dirname "$(readlink -f "$0")")"
source ./common.sh

# Run agent check for ExecutorService, ForkJoinPool
runCmd "${JAVA_CMD[@]}" -cp "$(getClasspathWithoutTtlJar)" \
    "-javaagent:$(getTtlJarPath)=ttl.agent.logger:STDOUT" \
    com.alibaba.ttl.threadpool.agent.check.AgentCheckMain

# Run agent check for Timer/TimerTask
runCmd "${JAVA_CMD[@]}" -cp "$(getClasspathWithoutTtlJar)" \
    "-javaagent:$(getTtlJarPath)=ttl.agent.logger:STDOUT,ttl.agent.enable.timer.task:true" \
    com.alibaba.ttl.threadpool.agent.check.timer.TimerAgentCheck
