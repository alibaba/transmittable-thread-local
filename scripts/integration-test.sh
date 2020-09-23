#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

export TTL_CI_TEST_MODE=true

source ./prepare-jdk.sh
source ./common_build.sh "${1:-}"

# default jdk 11, do build and test
switch_to_jdk 11
headInfo "test with Java 11: $JAVA_HOME"
# run junit test in run-agent-test.sh
runCmd ./scripts/run-agent-test.sh "${1:-}"

# test multi-version java home env
# shellcheck disable=SC2154
for jhm_var_name in "${java_home_var_names[@]}"; do
    export JAVA_HOME="${!jhm_var_name}"

    headInfo "test with $jhm_var_name: $JAVA_HOME"
    runCmd ./scripts/run-junit.sh skipClean
    runCmd ./scripts/run-agent-test.sh skipClean
done
