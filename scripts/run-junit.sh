#!/bin/bash

cd "$(dirname "$(readlink -f "$0")")"
source ./common.sh

# skip unit test for Javassist on command line, because Javassist is repackaged.
runCmd "${JAVA_CMD[@]}" -cp "$(getClasspath)" \
    org.junit.runner.JUnitCore $(junit_test_case | grep -vE '\.JavassistTest$')
