#!/bin/bash
set -eEuo pipefail
# adjust current dir to project dir
cd "$(dirname "$(readlink -f "$0")")/.."

TTL_ROOT_PROJECT_DIR="$(dirname "$(readlink -f "../")")"

source "$TTL_ROOT_PROJECT_DIR/scripts/common_build.sh"

(
    # install TTL lib
    cd $TTL_ROOT_PROJECT_DIR
    MVN_WITH_BASIC_OPTIONS install -Dmaven.test.skip
)

# compile sample-ttl-agent-extension
MVN_WITH_BASIC_OPTIONS -q clean compile

java_opts=(
    #-verbose:class
    -Duser.language=en -Duser.country=US
)

ttl_version=$(extractFirstElementValueFromPom version "../../pom.xml")

readonly ttl_agent_path="$TTL_ROOT_PROJECT_DIR/target/transmittable-thread-local-$ttl_version.jar"

readonly extension_transformlet_list="ttl.agent.extension.transformlet.list:com.alibaba.ttl.agent.extension_transformlet.sample.transformlet.SampleExtensionTransformlet"

readonly agent_option="-javaagent:$ttl_agent_path=${extension_transformlet_list},ttl.agent.logger:STDOUT"

readonly main_class=com.alibaba.ttl.agent.extension_transformlet.sample.biz.SampleMain

logAndRun "$JAVA_HOME/bin/java" "${agent_option}" "${java_opts[@]}" \
    -cp target/classes $main_class
