#!/bin/bash
set -eEuo pipefail
# adjust current dir to project dir
cd "$(dirname "$(readlink -f "$0")")/.."

TTL_ROOT_PROJECT_DIR="$(dirname "$(readlink -f "../")")"

source "$TTL_ROOT_PROJECT_DIR/scripts/common_build.sh"

ttl_version=$(extractFirstElementValueFromPom version "../../pom.xml")

readonly ttl_agent_path="$TTL_ROOT_PROJECT_DIR/target/transmittable-thread-local-$ttl_version.jar"

mvn_ttl_lib() {
    (
        cd "$TTL_ROOT_PROJECT_DIR"
        MVN_WITH_BASIC_OPTIONS -q -Dmaven.test.skip "$@"
    )
}

if [ "${1:-}" != "skipClean" ]; then
    mvn_ttl_lib clean package

    # compile sample-ttl-agent-extension-transformlet
    MVN_WITH_BASIC_OPTIONS -q clean compile
else
    if [ ! -f "$ttl_agent_path" ]; then
        mvn_ttl_lib package
    fi

    # compile sample-ttl-agent-extension-transformlet
    MVN_WITH_BASIC_OPTIONS -q compile
fi

readonly ttl_agent_options="-javaagent:$ttl_agent_path=ttl.agent.logger:STDOUT,ttl.agent.log.class.transform:true"

readonly main_class=com.alibaba.ttl.agent.extension_transformlet.sample.biz.SampleMain

logAndRun "$JAVA_HOME/bin/java" -Duser.language=en -Duser.country=US \
    "${ttl_agent_options}" \
    -cp target/classes $main_class
