#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

source ./prepare-jdk.sh
source ./common_build.sh

switch_to_jdk 8
runCmd "${MVN_CMD[@]}" clean
runCmd "${MVN_CMD[@]}" cobertura:cobertura
runCmd codecov
