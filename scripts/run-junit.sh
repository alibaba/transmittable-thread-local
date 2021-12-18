#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

export TTL_CI_TEST_MODE=true
source ./ttl_build.sh

# skip unit test for Javassist on command line, because Javassist is repackaged.
# skip unit test for TransmittableThreadLocal_withInit_Test for java 6
# shellcheck disable=SC2046
logAndRun "${JAVA_CMD[@]}" -cp "$(getClasspath)" \
    org.junit.runner.JUnitCore $(getJUnitTestCases | grep -vE '\.JavassistTest$|\.TtlTransformletHelperTest$|\.TransmittableThreadLocal_withInit_Null_Test$')
