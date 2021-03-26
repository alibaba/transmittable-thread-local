#!/bin/bash
set -eEuo pipefail
# adjust current dir to project dir
cd "$(dirname "$(readlink -f "$0")")/.."

TTL_ROOT_PROJECT_DIR="$(dirname "$(readlink -f "../")")"

source "$TTL_ROOT_PROJECT_DIR/scripts/common_build.sh"
source "$TTL_ROOT_PROJECT_DIR/scripts/prepare-jdk.sh"

for jv in 8 11; do
    switch_to_jdk "$jv"

    headInfo "test with JDK $JAVA_HOME"

    MVN_WITH_BASIC_OPTIONS test
    MVN_WITH_BASIC_OPTIONS test -Penable-TtlAgent-forTest
done
