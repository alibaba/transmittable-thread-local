#!/bin/bash
cd "$(dirname "$(readlink -f "$0")")"

export TTL_CI_TEST_MODE=true
source ./common.sh "$1"

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

headInfo "test with Java 11: $JAVA_HOME"
runCmd ./scripts/run-agent-test.sh "$1"


java_home_var_names=(
    JAVA6_HOME
    JAVA7_HOME
    JAVA8_HOME

    JAVA9_HOME
    JAVA10_HOME

    JAVA12_HOME
    JAVA13_HOME
    JAVA14_HOME
    JAVA15_HOME
)

# test multi-version java home env
for jhm_var_name in "${java_home_var_names[@]}"; do
   export JAVA_HOME="${!jhm_var_name}"

    if [ -n "$JAVA_HOME" ]; then
        headInfo "test with $jhm_var_name: $JAVA_HOME"
        runCmd ./scripts/run-junit.sh skipClean
        runCmd ./scripts/run-agent-test.sh skipClean
    else
        headInfo "skip $jhm_var_name: $JAVA_HOME"
    fi
done
