#!/bin/bash

cd "$(dirname "$(readlink -f "$0")")"
source ./common.sh

blueEcho "Run unit test under ttl agent, include check for ExecutorService, ForkJoinPool"
runCmd "${JAVA_CMD[@]}" -cp "$(getClasspathWithoutTtlJar)" \
    "-javaagent:$(getTtlJarPath)=ttl.agent.logger:STDOUT" \
    -Drun-ttl-test-under-agent=true \
    org.junit.runner.JUnitCore $(junit_test_case | grep -vE '\.TtlAgentTest$|\.JavassistTest$')

blueEcho "Run unit test under ttl agent, and turn on the disable inheritable for thread pool enhancement"
runCmd "${JAVA_CMD[@]}" -cp "$(getClasspathWithoutTtlJar)" \
    "-javaagent:$(getTtlJarPath)=ttl.agent.logger:STDOUT,ttl.agent.disable.inheritable.for.thread.pool:true" \
    -Drun-ttl-test-under-agent=true \
    -Drun-ttl-test-under-agent-with-disable-inheritable=true \
    org.junit.runner.JUnitCore $(junit_test_case | grep -vE '\.TtlAgentTest$|\.JavassistTest$')

blueEcho "Run agent check for Timer/TimerTask"
runCmd "${JAVA_CMD[@]}" -cp "$(getClasspathWithoutTtlJar)" \
    "-javaagent:$(getTtlJarPath)=ttl.agent.logger:STDOUT,ttl.agent.enable.timer.task:true" \
    com.alibaba.ttl.threadpool.agent.check.timer.TimerAgentCheck
