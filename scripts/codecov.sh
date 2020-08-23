#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

source ./prepare-jdk.sh
source ./common_build.sh

switch_to_jdk 8
runCmd ./mvnw clean -V
runCmd ./mvnw cobertura:cobertura
runCmd codecov
