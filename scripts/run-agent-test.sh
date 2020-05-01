#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

export TTL_CI_TEST_MODE=true
source ./common_build.sh

# do heavy operation first, descrease mvn operation count.
runCmd mvnBuildJar

blueEcho 'Run unit test under ttl agent, include check for ExecutorService, ForkJoinPool'
runCmd "${JAVA_CMD[@]}" -cp "$(getClasspathWithoutTtlJar)" \
    "-javaagent:$(getTtlJarPath)=ttl.agent.logger:STDOUT" \
    -Drun-ttl-test-under-agent=true \
    org.junit.runner.JUnitCore $(getJUnitTestCases)

blueEcho 'Run unit test under ttl agent, and turn on the disable inheritable for thread pool enhancement'
runCmd "${JAVA_CMD[@]}" -cp "$(getClasspathWithoutTtlJar)" \
    "-javaagent:$(getTtlJarPath)=ttl.agent.logger:STDOUT,ttl.agent.disable.inheritable.for.thread.pool:true" \
    -Drun-ttl-test-under-agent=true \
    -Drun-ttl-test-under-agent-with-disable-inheritable=true \
    org.junit.runner.JUnitCore $(getJUnitTestCases)

blueEcho 'Run agent check for Timer/TimerTask, default "ttl.agent.enable.timer.task"'
runCmd "${JAVA_CMD[@]}" -cp "$(getClasspathWithoutTtlJar)" \
    "-javaagent:$(getTtlJarPath)=ttl.agent.logger:STDOUT" \
    com.alibaba.ttl.threadpool.agent.check.timer.TimerAgentCheck

blueEcho 'Run agent check for Timer/TimerTask, explicit "ttl.agent.enable.timer.task"'
runCmd "${JAVA_CMD[@]}" -cp "$(getClasspathWithoutTtlJar)" \
    "-javaagent:$(getTtlJarPath)=ttl.agent.logger:STDOUT,ttl.agent.enable.timer.task:true" \
    com.alibaba.ttl.threadpool.agent.check.timer.TimerAgentCheck
