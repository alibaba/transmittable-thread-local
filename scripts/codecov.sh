#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

source bash-buddy/lib/trap_error_info.sh
source bash-buddy/lib/common_utils.sh
source bash-buddy/lib/prepare_jdks.sh
source bash-buddy/lib/java_build_utils.sh

cd ..

prepare_jdks::switch_to_jdk 8

# about codecov: example-java-maven
# https://github.com/codecov/example-java-maven/blob/master/.travis.yml

if [ "${1:-}" = "-s" ]; then
  jvb::mvn_cmd -Pgen-code-cov jacoco:prepare-agent surefire:test jacoco:report
else
  jvb::mvn_cmd -Pgen-code-cov clean test
fi

bash <(curl -s https://codecov.io/bash)
