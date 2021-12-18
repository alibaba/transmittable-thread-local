#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

export TTL_CI_TEST_MODE=true
source ./ttl_build.sh

# do heavy operation first, decrease mvn operation count.
logAndRun mvnBuildJar

blueEcho 'Run unit test under ttl agent, include check for ExecutorService, ForkJoinPool'
logAndRun "${JAVA_CMD[@]}" -cp "$(getClasspathWithoutTtlJar)" \
    "-javaagent:$(getTtlJarPath)=ttl.agent.logger:STDOUT" \
    -Drun-ttl-test-under-agent=true \
    org.junit.runner.JUnitCore $(getJUnitTestCases | grep -vE '\.TransmittableThreadLocal_withInit_Null_Test$')

blueEcho 'Run unit test under ttl agent, and turn on the disable inheritable for thread pool enhancement'
logAndRun "${JAVA_CMD[@]}" -cp "$(getClasspathWithoutTtlJar)" \
    "-javaagent:$(getTtlJarPath)=ttl.agent.logger:STDOUT,ttl.agent.disable.inheritable.for.thread.pool:true" \
    -Drun-ttl-test-under-agent=true \
    -Drun-ttl-test-under-agent-with-disable-inheritable=true \
    org.junit.runner.JUnitCore $(getJUnitTestCases | grep -vE '\.TransmittableThreadLocal_withInit_Null_Test$')

blueEcho 'Run agent check for Timer/TimerTask, default "ttl.agent.enable.timer.task"'
logAndRun "${JAVA_CMD[@]}" -cp "$(getClasspathWithoutTtlJar)" \
    "-javaagent:$(getTtlJarPath)=ttl.agent.logger:STDOUT" \
    com.alibaba.test.ttl.threadpool.agent.check.timer.TimerAgentCheck

blueEcho 'Run agent check for Timer/TimerTask, explicit "ttl.agent.enable.timer.task"'
logAndRun "${JAVA_CMD[@]}" -cp "$(getClasspathWithoutTtlJar)" \
    "-javaagent:$(getTtlJarPath)=ttl.agent.logger:STDOUT,ttl.agent.enable.timer.task:true" \
    com.alibaba.test.ttl.threadpool.agent.check.timer.TimerAgentCheck
