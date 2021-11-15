#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

source ./prepare-jdk.sh
source ./ttl_build.sh


# about codecov: example-java-maven
# https://github.com/codecov/example-java-maven/blob/master/.travis.yml

switch_to_jdk 8
MVN_WITH_BASIC_OPTIONS clean
MVN_WITH_BASIC_OPTIONS -Pgen-code-cov cobertura:cobertura

bash <(curl -s https://codecov.io/bash)
