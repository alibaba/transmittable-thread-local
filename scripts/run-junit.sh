#!/bin/bash

cd "$(dirname "$(readlink -f "$0")")"

export TTL_CI_TEST_MODE=true
source ./common.sh

# skip unit test for Javassist on command line, because Javassist is repackaged.
runCmd "${JAVA_CMD[@]}" -cp "$(getClasspath)" \
    org.junit.runner.JUnitCore $(getJUnitTestCases | grep -vE '\.JavassistTest$')
