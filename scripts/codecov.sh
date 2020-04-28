#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

source ./common_build.sh

export JAVA_HOME="$JDK8_HOME"
runCmd ./mvnw clean -V
runCmd ./mvnw cobertura:cobertura
runCmd codecov
