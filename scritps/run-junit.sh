#!/bin/bash

cd "$(dirname "$(readlink -f "$0")")"
source ./common.sh

junit_test_case() {
    (
        cd target/test-classes &&
        find . -iname '*Test.class' | sed '
                s%^\./%%
                s/\.class$//
                s%/%.%g
            '
    )
}

runCmd "${JAVA_CMD[@]}" -cp "$(getClasspath)" \
    org.junit.runner.JUnitCore $(junit_test_case)
