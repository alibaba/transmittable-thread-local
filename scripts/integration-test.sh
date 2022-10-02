#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

BASH_BUDDY_ROOT="$(readlink -f bash-buddy)"
readonly BASH_BUDDY_ROOT

source "$BASH_BUDDY_ROOT/lib/trap_error_info.sh"
source "$BASH_BUDDY_ROOT/lib/common_utils.sh"

################################################################################
# prepare
################################################################################

readonly default_build_jdk_version=11
# shellcheck disable=SC2034
readonly PREPARE_JDKS_INSTALL_BY_SDKMAN=(
  8
  "$default_build_jdk_version"
  17
)

source "$BASH_BUDDY_ROOT/lib/prepare_jdks.sh"

source "$BASH_BUDDY_ROOT/lib/java_build_utils.sh"

# here use `install` and `-D performRelease` intended
#   to check release operations.
#
# De-activate a maven profile from command line
#   https://stackoverflow.com/questions/25201430
#
# shellcheck disable=SC2034
JVB_MVN_OPTS=(
  "${JVB_DEFAULT_MVN_OPTS[@]}"
  -DperformRelease -P'!gen-sign'
)

################################################################################
# ci build logic
################################################################################

PROJECT_ROOT_DIR="$(readlink -f ..)"
readonly PROJECT_ROOT_DIR
cd "$PROJECT_ROOT_DIR"

########################################
# do build and test by default version jdk
########################################

prepare_jdks::switch_to_jdk "$default_build_jdk_version"

cu::head_line_echo "build and test with Java: $JAVA_HOME"
jvb::mvn_cmd clean install

########################################
# test by multi-version jdk
########################################
for jdk in "${PREPARE_JDKS_INSTALL_BY_SDKMAN[@]}"; do
  prepare_jdks::switch_to_jdk "$jdk"

  # just test without build

  # default jdk already tested above
  if [ "$jdk" != "$default_build_jdk_version" ]; then
    cu::head_line_echo "test with Java: $JAVA_HOME"
    jvb::mvn_cmd surefire:test -Denforcer.skip
  fi

  cu::head_line_echo "test with TTL Agent and Java: $JAVA_HOME"

  cu::blue_echo 'Run unit test under ttl agent, include check for ExecutorService, ForkJoinPool, Timer/TimerTask'
  jvb::mvn_cmd -Penable-ttl-agent-for-test surefire:test -Denforcer.skip \
    -Dttl.agent.extra.d.options='-Drun-ttl-test-under-agent-with-enable-timer-task=true'

  cu::blue_echo 'Run unit test under ttl agent, and turn on the disable inheritable for thread pool enhancement'
  jvb::mvn_cmd -Penable-ttl-agent-for-test surefire:test -Denforcer.skip \
    -Dttl.agent.extra.args='ttl.agent.disable.inheritable.for.thread.pool:true' \
    -Dttl.agent.extra.d.options='-Drun-ttl-test-under-agent-with-disable-inheritable=true'

  cu::blue_echo 'Run agent check for Timer/TimerTask, explicit "ttl.agent.enable.timer.task"'
  jvb::mvn_cmd -Penable-ttl-agent-for-test surefire:test -Denforcer.skip \
    -Dttl.agent.extra.args='ttl.agent.enable.timer.task:true' \
    -Dttl.agent.extra.d.options='-Drun-ttl-test-under-agent-with-enable-timer-task=true'
done
