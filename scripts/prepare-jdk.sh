#!/bin/bash
# SDKMAN! with Travis
# https://objectcomputing.com/news/2019/01/07/sdkman-travis
set -eEuo pipefail

[ -z "${__source_guard_37F50E39_A075_4E05_A3A0_8939EF62D836:+dummy}" ] || return 0
__source_guard_37F50E39_A075_4E05_A3A0_8939EF62D836="$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")"

# shellcheck source=common.sh
source "$__source_guard_37F50E39_A075_4E05_A3A0_8939EF62D836/common.sh"

__loadSdkman() {
    local this_time_install_sdk_man=false
    # install sdkman
    if [ ! -f "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
        [ -d "$HOME/.sdkman" ] && rm -rf "$HOME/.sdkman"

        curl -s get.sdkman.io | bash || die "fail to install sdkman"
        {
            echo sdkman_auto_answer=true
            echo sdkman_auto_selfupdate=false
            echo sdkman_disable_auto_upgrade_check=true
        } >>"$HOME/.sdkman/etc/config"

        this_time_install_sdk_man=true
    fi

    logAndRun cat "$HOME/.sdkman/etc/config"

    # shellcheck disable=SC1090
    loose source "$HOME/.sdkman/bin/sdkman-init.sh"

    if "$this_time_install_sdk_man"; then
        loose logAndRun sdk ls java | sed -n '/^ Vendor/,/^===========/p'
    fi
}
__loadSdkman

jdks_install_by_sdkman=(
    6.0.119-zulu
    8.312.07.1-amzn
    11.0.13-ms
    17.0.1-ms

    18.ea.28-open
)
java_home_var_names=()

__setJdkHomeVarsAndInstallJdk() {
    blueEcho "prepared jdks:"
    JDK6_HOME="${JDK6_HOME:-/usr/lib/jvm/java-6-openjdk-amd64}"

    local jdkNameOfSdkman
    for jdkNameOfSdkman in "${jdks_install_by_sdkman[@]}"; do
        local jdkVersion
        jdkVersion=$(echo "$jdkNameOfSdkman" | awk -F'[.]' '{print $1}')

        # jdkHomeVarName like JDK7_HOME, JDK11_HOME
        local jdkHomeVarName="JDK${jdkVersion}_HOME"

        if [ ! -d "${!jdkHomeVarName:-}" ]; then
            local jdkHomePath="$SDKMAN_CANDIDATES_DIR/java/$jdkNameOfSdkman"

            # set JDK7_HOME ~ JDK1x_HOME to global var java_home_var_names
            eval "$jdkHomeVarName='${jdkHomePath}'"

            # install jdk by sdkman
            [ ! -d "$jdkHomePath" ] && {
                set +u
                loose logAndRun sdk install java "$jdkNameOfSdkman" || die "fail to install jdk $jdkNameOfSdkman by sdkman"
                set -u
            }
        fi

        java_home_var_names=(${java_home_var_names[@]:+"${java_home_var_names[@]}"} "$jdkHomeVarName")
        printf '%s :\n\t%s\n\tspecified is %s\n' "$jdkHomeVarName" "${!jdkHomeVarName}" "$jdkNameOfSdkman"
    done

    echo
    blueEcho "ls $SDKMAN_CANDIDATES_DIR/java/ :"
    ls -la "$SDKMAN_CANDIDATES_DIR/java/"
}
__setJdkHomeVarsAndInstallJdk

switch_to_jdk() {
    [ $# == 1 ] || die "${FUNCNAME[0]} need 1 argument! But provided: $*"

    local javaHomeVarName="JDK${1}_HOME"
    export JAVA_HOME="${!javaHomeVarName}"

    [ -n "$JAVA_HOME" ] || die "jdk $1 env not found: $javaHomeVarName"
    [ -e "$JAVA_HOME" ] || die "jdk $1 not existed: $JAVA_HOME"
    [ -d "$JAVA_HOME" ] || die "jdk $1 is not directory: $JAVA_HOME"
}
