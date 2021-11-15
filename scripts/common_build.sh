#!/bin/bash
# NOTE about Bash Traps and Pitfalls:
#
# 1. DO NOT declare var as readonly/local if value is supplied by subshell!!
#    for example:
#       readonly var1=$(echo value1)
#       local var1=$(echo value1)
#
#    readonly declaration make exit code of assignment to be always 0,
#      aka. the exit code of command in subshell is discarded.
#      tested on bash 3.2.57/4.2.46

[ -z "${__source_guard_E2EB46EC_DEB8_4818_8D4E_F425BDF4A275:+dummy}" ] || return 0
__source_guard_E2EB46EC_DEB8_4818_8D4E_F425BDF4A275="$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")"

# shellcheck source=common.sh
source "$__source_guard_E2EB46EC_DEB8_4818_8D4E_F425BDF4A275/common.sh"


################################################################################
# build util functions
################################################################################

__getMvnwExe() {
    local maven_wrapper_name="mvnw"

    local d="$PWD"
    while true; do
        [ "/" = "$d" ] && die "Fail to find $maven_wrapper_name!"
        [ -f "$d/$maven_wrapper_name" ] && break

        d=$(dirname "$d")
    done

    echo "$d/$maven_wrapper_name"
}

__getJavaVersion() {
    "$JAVA_HOME/bin/java" -version 2>&1 | awk -F\" '/ version "/{print $2}'
}

__getMoreMvnOptionsWhenJdk11() {
    local javaVersion
    javaVersion=$(__getJavaVersion)
    if ! versionLessThan $javaVersion 11 && versionLessThan $javaVersion 12; then
        echo -DperformRelease -P'!gen-sign'
    fi
}

readonly -a _MVN_BASIC_OPTIONS=(
    -V --no-transfer-progress
)
_MVN_OPTIONS=(
    "${_MVN_BASIC_OPTIONS[@]}"
    $(__getMoreMvnOptionsWhenJdk11)
)

MVN() {
    logAndRun "$(__getMvnwExe)" "${_MVN_OPTIONS[@]}" "$@"
}

MVN_WITH_BASIC_OPTIONS() {
    logAndRun "$(__getMvnwExe)" "${_MVN_BASIC_OPTIONS[@]}" "$@"
}

extractFirstElementValueFromPom() {
    (($# == 2)) || die "${FUNCNAME[0]} need only 2 arguments, actual arguments: $*"

    local element=$1
    local pom_file=$2
    grep \<"$element"'>.*</'"$element"\> "$pom_file" | awk -F'</?'"$element"\> 'NR==1 {print $2}'
}
