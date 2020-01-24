#!/bin/bash

cd "$(dirname "$(readlink -f "$0")")"

export TTL_CI_TEST_MODE=true
source ./common.sh

# run junit test for core module
# skip unit test for Javassist on command line, because Javassist is repackaged.
runCmd "${JAVA_CMD[@]}" -cp "$(getCoreModuleClasspath)" \
    org.junit.runner.JUnitCore $(getCoreModuleJUnitTestCases | grep -vE '\.JavassistTest$')

# run junit test for kotlin-support module
runCmd "${JAVA_CMD[@]}" -cp "$(getKotlinSupportModuleClasspath)" \
    org.junit.runner.JUnitCore $(getKotlinSupportModuleJUnitTestCases)
