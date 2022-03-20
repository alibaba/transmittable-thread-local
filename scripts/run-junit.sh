#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

source bash-buddy/lib/trap_error_info.sh
source bash-buddy/lib/common_utils.sh

source ./ttl_build.sh

# skip unit test for related Javassist on command line, because Javassist is repackaged.
# skip unit test for TransmittableThreadLocal_withInit_Test for java 6
# shellcheck disable=SC2046
cu::log_then_run "${JAVA_CMD[@]}" -cp "$(getClasspath)" \
    org.junit.runner.JUnitCore $(getJUnitTestCases | grep -vE '\.JavassistTest$|\.TransmittableThreadLocal_withInit_Null_Test$')
