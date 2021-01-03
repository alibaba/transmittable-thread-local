#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

source ./prepare-jdk.sh
source ./common_build.sh


# about codecov: example-java-maven
# https://github.com/codecov/example-java-maven/blob/master/.travis.yml

switch_to_jdk 8
runCmd "${MVN_CMD[@]}" clean
runCmd "${MVN_CMD[@]}" cobertura:cobertura

bash <(curl -s https://codecov.io/bash)
