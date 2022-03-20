#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

source bash-buddy/lib/trap_error_info.sh
source bash-buddy/lib/common_utils.sh

readonly default_build_jdk_version=11
# shellcheck disable=SC2034
readonly PREPARE_JDKS_INSTALL_BY_SDKMAN=(
  6
  8
  "$default_build_jdk_version"
  17
)

source bash-buddy/lib/prepare_jdks.sh
source ./ttl_build.sh "${1:-}"

# using default jdk, do build and test
prepare_jdks::switch_to_jdk $default_build_jdk_version
cu::head_line_echo "test with Java $default_build_jdk_version: $JAVA_HOME"
# run junit test in run-agent-test.sh
cu::log_then_run ./scripts/run-agent-test.sh skipClean

# test multi-version java home env
for jdk in "${PREPARE_JDKS_INSTALL_BY_SDKMAN[@]}"; do
  [ "$jdk" = "$default_build_jdk_version" ] && continue

  prepare_jdks::switch_to_jdk "$jdk"

  cu::head_line_echo "test with Java $jdk: $JAVA_HOME"
  cu::log_then_run ./scripts/run-junit.sh skipClean
  cu::log_then_run ./scripts/run-agent-test.sh skipClean
done
