#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

source bash-buddy/lib/trap_error_info.sh
source bash-buddy/lib/common_utils.sh
source bash-buddy/lib/prepare_jdks.sh

source ./ttl_build.sh

# about codecov: example-java-maven
# https://github.com/codecov/example-java-maven/blob/master/.travis.yml

prepare_jdks::switch_to_jdk 8
MVN_WITH_BASIC_OPTIONS clean
MVN_WITH_BASIC_OPTIONS -Pgen-code-cov cobertura:cobertura

bash <(curl -s https://codecov.io/bash)
