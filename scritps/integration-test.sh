#!/bin/bash
cd "$(dirname "$(readlink -f "$0")")"
source ./common.sh skipClean

# set multi-version java home env
#   - JAVA8_HOME
#   - JAVA6_HOME
#   - JAVA7_HOME
#   - JAVA9_HOME
#   - JAVA10_HOME

# Java 8
if [ -n "$JAVA8_HOME" ]; then
    export JAVA_HOME="${JAVA8_HOME}"
else
    current_java_version=$(./mvn -v | awk -F'[ ,]' '/^Java version/{print $3}')
    if [[ default_java_version != "1.8."* ]]; then
        echo "Fail to get java 8 home!"
        exit 1
    fi
fi

headInfo "test with Java 8"
runCmd ./mvnw clean install --batch-mode
runCmd ./scritps/run-agent-test.sh

# Java 6
if [ -n "$JAVA6_HOME" ]; then
    headInfo "test with Java 6"
    export JAVA_HOME="${JAVA6_HOME}"
    runCmd ./scritps/run-junit.sh skipClean
    runCmd ./scritps/run-agent-test.sh skipClean
else
    headInfo "skip Java 6 test"
fi

# Java 7
if [ -n "$JAVA7_HOME" ]; then
    headInfo "test with Java 7"
    export JAVA_HOME="${JAVA7_HOME}"
    runCmd ./scritps/run-junit.sh skipClean
    runCmd ./scritps/run-agent-test.sh skipClean
else
    headInfo "skip Java 7 test"
fi

# Java 9
if [ -n "$JAVA9_HOME" ]; then
    headInfo "test with Java 9"
    export JAVA_HOME="${JAVA9_HOME}"
    runCmd ./scritps/run-junit.sh skipClean
    runCmd ./scritps/run-agent-test.sh skipClean
else
    headInfo "skip Java 9 test"
fi

# Java 10
if [ -n "$JAVA10_HOME" ]; then
    headInfo "test with Java 10"
    export JAVA_HOME="${JAVA10_HOME}"
    runCmd ./scritps/run-junit.sh skipClean
    runCmd ./scritps/run-agent-test.sh skipClean
else
    headInfo "skip Java 10 test"
fi
