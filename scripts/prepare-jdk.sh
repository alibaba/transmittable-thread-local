#!/bin/bash
# SDKMAN! with Travis
# https://objectcomputing.com/news/2019/01/07/sdkman-travis
set -eEo pipefail

[ -z "${_source_mark_of_prepare_jdk:+dummy}" ] || return 0
export _source_mark_of_prepare_jdk=true


# shellcheck source=common.sh
source "$(dirname "$(readlink -f "$BASH_SOURCE")")/common.sh"


if [ ! -f "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
    [ -d "$HOME/.sdkman" ] && rm -rf "$HOME/.sdkman"
    curl -s get.sdkman.io | bash || exit 2
    echo sdkman_auto_answer=true > "$HOME/.sdkman/etc/config"
fi
# shellcheck disable=SC1090
source "$HOME/.sdkman/bin/sdkman-init.sh"


jdks_install_by_sdkman=(
    7.0.262-zulu
    8.0.252-zulu
    9.0.7-zulu
    10.0.2-zulu
    11.0.7-zulu
    12.0.2-open
    13.0.3-zulu
    14.0.1-zulu
    15.ea.20-open
)

export JDK6_HOME="${JDK6_HOME:-/usr/lib/jvm/java-6-openjdk-amd64}"
export JDK7_HOME="$SDKMAN_CANDIDATES_DIR/java/${jdks_install_by_sdkman[0]}"
export JDK8_HOME="$SDKMAN_CANDIDATES_DIR/java/${jdks_install_by_sdkman[1]}"
export JDK9_HOME="$SDKMAN_CANDIDATES_DIR/java/${jdks_install_by_sdkman[2]}"
export JDK10_HOME="$SDKMAN_CANDIDATES_DIR/java/${jdks_install_by_sdkman[3]}"
export JDK11_HOME="$SDKMAN_CANDIDATES_DIR/java/${jdks_install_by_sdkman[4]}"
export JDK12_HOME="$SDKMAN_CANDIDATES_DIR/java/${jdks_install_by_sdkman[5]}"
export JDK13_HOME="$SDKMAN_CANDIDATES_DIR/java/${jdks_install_by_sdkman[6]}"
export JDK14_HOME="$SDKMAN_CANDIDATES_DIR/java/${jdks_install_by_sdkman[7]}"
export JDK15_HOME="$SDKMAN_CANDIDATES_DIR/java/${jdks_install_by_sdkman[8]}"


switch_to_jdk() {
    local javaHome="JDK${1}_HOME"
    export JAVA_HOME=${!javaHome}

    [ -n "$JAVA_HOME" ] || return 2
}


for _jdk__ in "${jdks_install_by_sdkman[@]}"; do
    if [ ! -d "$SDKMAN_CANDIDATES_DIR/java/$_jdk__" ]; then
        runCmd sdk install java "$_jdk__" || exit 2
    fi
done

ls -la "$SDKMAN_CANDIDATES_DIR/java/"
