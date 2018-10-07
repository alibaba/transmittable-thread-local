#!/bin/bash

cd "$(dirname "$(readlink -f "$0")")"
source ./common.sh

# Run unit test under ttl agent, include check for ExecutorService, ForkJoinPool
runCmd "${JAVA_CMD[@]}" -cp "$(getClasspathWithoutTtlJar)" \
    "-javaagent:$(getTtlJarPath)=ttl.agent.logger:STDOUT" \
    -Drun-ttl-test-under-agent=true \
    org.junit.runner.JUnitCore $(junit_test_case | grep -vE '\.TtlAgentTest$')

# Run agent check for Timer/TimerTask
runCmd "${JAVA_CMD[@]}" -cp "$(getClasspathWithoutTtlJar)" \
    "-javaagent:$(getTtlJarPath)=ttl.agent.logger:STDOUT,ttl.agent.enable.timer.task:true" \
    com.alibaba.ttl.threadpool.agent.check.timer.TimerAgentCheck
