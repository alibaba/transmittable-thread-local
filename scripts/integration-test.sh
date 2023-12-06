#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

BASH_BUDDY_ROOT="$(readlink -f bash-buddy)"
readonly BASH_BUDDY_ROOT
source "$BASH_BUDDY_ROOT/lib/trap_error_info.sh"
source "$BASH_BUDDY_ROOT/lib/common_utils.sh"
source "$BASH_BUDDY_ROOT/lib/java_utils.sh"
source "$BASH_BUDDY_ROOT/lib/maven_utils.sh"

################################################################################
# ci build logic
################################################################################

readonly default_build_jdk_version=11
# shellcheck disable=SC2034
readonly JDK_VERSIONS=(
  8
  "$default_build_jdk_version"
  17
  21
)

# here use `install` and `-D performRelease` intended
#   to check release operations.
#
# De-activate a maven profile from command line
#   https://stackoverflow.com/questions/25201430
#
# shellcheck disable=SC2034
readonly MVU_MVN_OPTS=(
  "${MVU_DEFAULT_MVN_OPTS[@]}"
  -DperformRelease -P'!gen-sign'
  # Maven Plugin Validation
  # https://maven.apache.org/guides/plugins/validation/index.html
  -Dmaven.plugin.validation=NONE
  ${CI_MORE_MVN_OPTS:+${CI_MORE_MVN_OPTS}}
)

cd ..

########################################
# build and test by default version jdk
########################################

jvu::switch_to_jdk "$default_build_jdk_version"

cu::head_line_echo "build and test with Java $default_build_jdk_version: $JAVA_HOME"
mvu::mvn_cmd clean install

########################################
# test by multi-version jdk
########################################

# about CI env var
#   https://docs.github.com/en/actions/learn-github-actions/variables#default-environment-variables
if [ "${CI:-}" = true ]; then
  readonly CI_MORE_BEGIN_OPTS=jacoco:prepare-agent CI_MORE_END_OPTS=jacoco:report
fi

for jdk_version in "${JDK_VERSIONS[@]}"; do
  jvu::switch_to_jdk "$jdk_version"

  # just test without build
  if [ "$jdk_version" != "$default_build_jdk_version" ]; then
    # default jdk already tested above
    cu::head_line_echo "test with Java: $JAVA_HOME"
    mvu::mvn_cmd ${CI_MORE_BEGIN_OPTS:-} surefire:test -Denforcer.skip ${CI_MORE_END_OPTS:-}
  fi

  (
    cd ttl2-compatible
    cu::head_line_echo "test with TTL Agent and Java: $JAVA_HOME"

    cu::blue_echo 'Run unit test under ttl agent, include check for ExecutorService, ForkJoinPool, Timer/TimerTask'
    mvu::mvn_cmd ${CI_MORE_BEGIN_OPTS:-} \
      surefire:test -Denforcer.skip \
      -Penable-ttl-agent-for-test \
      -Dttl.agent.extra.d.options='-Drun-ttl-test-under-agent-with-enable-timer-task=true' \
      ${CI_MORE_END_OPTS:-}

    cu::blue_echo 'Run unit test under ttl agent, and turn on the disable inheritable for thread pool enhancement'
    mvu::mvn_cmd ${CI_MORE_BEGIN_OPTS:-} \
      surefire:test -Denforcer.skip \
      -Penable-ttl-agent-for-test \
      -Dttl.agent.extra.args='ttl.agent.disable.inheritable.for.thread.pool:true' \
      -Dttl.agent.extra.d.options='-Drun-ttl-test-under-agent-with-disable-inheritable=true' \
      ${CI_MORE_END_OPTS:-}

    cu::blue_echo 'Run agent check for Timer/TimerTask, explicit "ttl.agent.enable.timer.task"'
    mvu::mvn_cmd ${CI_MORE_BEGIN_OPTS:-} \
      surefire:test -Denforcer.skip \
      -Penable-ttl-agent-for-test \
      -Dttl.agent.extra.args='ttl.agent.enable.timer.task:true' \
      -Dttl.agent.extra.d.options='-Drun-ttl-test-under-agent-with-enable-timer-task=true' \
      ${CI_MORE_END_OPTS:-}
  )
done
