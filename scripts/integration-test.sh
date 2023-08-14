#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

BASH_BUDDY_ROOT="$(readlink -f bash-buddy)"
readonly BASH_BUDDY_ROOT
source "$BASH_BUDDY_ROOT/lib/trap_error_info.sh"
source "$BASH_BUDDY_ROOT/lib/common_utils.sh"
source "$BASH_BUDDY_ROOT/lib/java_build_utils.sh"

################################################################################
# ci build logic
################################################################################

readonly default_build_jdk_version=11
# shellcheck disable=SC2034
readonly JDK_VERSIONS=(
  8
  "$default_build_jdk_version"
  17
  20
  21
)
readonly default_jh_var_name="JAVA${default_build_jdk_version}_HOME"

# Here use `-D performRelease` intendedly to check release operations.
#
# De-activate a maven profile from command line
#   https://stackoverflow.com/questions/25201430
#
# shellcheck disable=SC2034
JVB_MVN_OPTS=(
  "${JVB_DEFAULT_MVN_OPTS[@]}"
  -DperformRelease -P'!gen-sign'
  -Dmaven.plugin.validation=NONE
  ${CI_MORE_MVN_OPTS:+${CI_MORE_MVN_OPTS}}
)

PROJECT_ROOT_DIR="$(readlink -f ..)"
readonly PROJECT_ROOT_DIR
cd "$PROJECT_ROOT_DIR"

########################################
# do build and test by default version jdk
########################################

[ -d "${!default_jh_var_name:-}" ] || cu::die "\$${default_jh_var_name}(${!default_jh_var_name:-}) dir is not existed!"
export JAVA_HOME="${!default_jh_var_name}"

cu::head_line_echo "build and test with Java $default_build_jdk_version: $JAVA_HOME"
jvb::mvn_cmd clean install

########################################
# test by multi-version jdk
########################################

for jdk_version in "${JDK_VERSIONS[@]}"; do
  jh_var_name="JAVA${jdk_version}_HOME"
  [ -d "${!jh_var_name:-}" ] || cu::die "\$${jh_var_name}(${!jh_var_name:-}) dir is not existed!"
  export JAVA_HOME="${!jh_var_name}"

  if [ "$jdk_version" != "$default_build_jdk_version" ]; then
    # skip default jdk, already tested above
    cu::head_line_echo "test with Java $jdk_version: $JAVA_HOME"
    # about CI env var
    #   https://docs.github.com/en/actions/learn-github-actions/variables#default-environment-variables
    if [ "${CI:-}" = true ]; then
      jvb::mvn_cmd jacoco:prepare-agent surefire:test -Denforcer.skip jacoco:report
    else
      jvb::mvn_cmd surefire:test -Denforcer.skip
    fi
  fi

  cu::head_line_echo "test with TTL Agent and Java $jdk_version: $JAVA_HOME"

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
