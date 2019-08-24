#!/bin/bash
cd "$(dirname "$(readlink -f "$0")")"

export TTL_CI_TEST_MODE=true
source ./common.sh "$1"

# set multi-version java home env
#   - JAVA6_HOME
#   - JAVA8_HOME
#   - JAVA7_HOME
#   - JAVA9_HOME
#   - JAVA10_HOME
#   - JAVA11_HOME
#   - JAVA12_HOME

# Java 11
if [ -n "$JAVA11_HOME" ]; then
    export JAVA_HOME="${JAVA11_HOME}"
else
    current_java_version=$("${MVN_CMD[@]}" -v | awk -F'[ ,]' '/^Java version/{print $3}')
    if [[ default_java_version != 11.* ]]; then
        echo "Fail to get java 11 home!"
        exit 1
    fi
fi

headInfo "test with Java 11"

runCmd ./scripts/run-agent-test.sh "$1"

# Java 6
if [ -n "$JAVA6_HOME" ]; then
    headInfo "test with Java 6"
    export JAVA_HOME="${JAVA6_HOME}"
    runCmd ./scripts/run-junit.sh skipClean
    runCmd ./scripts/run-agent-test.sh skipClean
else
    headInfo "skip Java 6 test"
fi

# Java 7
if [ -n "$JAVA7_HOME" ]; then
    headInfo "test with Java 7"
    export JAVA_HOME="${JAVA7_HOME}"
    runCmd ./scripts/run-junit.sh skipClean
    runCmd ./scripts/run-agent-test.sh skipClean
else
    headInfo "skip Java 7 test"
fi

# Java 8
if [ -n "$JAVA8_HOME" ]; then
    headInfo "test with Java 8"
    export JAVA_HOME="${JAVA8_HOME}"
    runCmd ./scripts/run-junit.sh skipClean
    runCmd ./scripts/run-agent-test.sh skipClean
else
    headInfo "skip Java 8 test"
fi

# Java 9
if [ -n "$JAVA9_HOME" ]; then
    headInfo "test with Java 9"
    export JAVA_HOME="${JAVA9_HOME}"
    runCmd ./scripts/run-junit.sh skipClean
    runCmd ./scripts/run-agent-test.sh skipClean
else
    headInfo "skip Java 9 test"
fi

# Java 11
if [ -n "$JAVA10_HOME" ]; then
    headInfo "test with Java 10"
    export JAVA_HOME="${JAVA10_HOME}"
    runCmd ./scripts/run-junit.sh skipClean
    runCmd ./scripts/run-agent-test.sh skipClean
else
    headInfo "skip Java 11 test"
fi

# Java 12
if [ -n "$JAVA12_HOME" ]; then
    headInfo "test with Java 12"
    export JAVA_HOME="${JAVA12_HOME}"
    runCmd ./scripts/run-junit.sh skipClean
    runCmd ./scripts/run-agent-test.sh skipClean
else
    headInfo "skip Java 12 test"
fi

# Java 13
if [ -n "$JAVA13_HOME" ]; then
    headInfo "test with Java 13"
    export JAVA_HOME="${JAVA13_HOME}"
    runCmd ./scripts/run-junit.sh skipClean
    runCmd ./scripts/run-agent-test.sh skipClean
else
    headInfo "skip Java 13 test"
fi
