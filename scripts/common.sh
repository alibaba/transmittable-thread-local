#!/bin/bash

set -o pipefail
set -e

# https://stackoverflow.com/questions/64786/error-handling-in-bash
error() {
    local parent_lineno="$1"
    local message="$2"
    local code="${3:-1}"
    if [[ -n "$message" ]] ; then
        redEcho "Error on or near line $(caller): ${message}; exiting with status ${code}"
    else
        redEcho "Error on or near line $(caller); exiting with status ${code}"
    fi
    exit "${code}"
}
trap 'error ${LINENO}' ERR

################################################################################
# util functions
################################################################################

# NOTE: $'foo' is the escape sequence syntax of bash
readonly nl=$'\n' # escape end
readonly ec=$'\033' # escape char
readonly eend=$'\033[0m' # escape end

colorEcho() {
    local color=$1
    shift

    # if stdout is console, turn on color output.
    [ -t 1 ] && echo "$ec[1;${color}m$@$eend" || echo "$@"
}

redEcho() {
     colorEcho 31 "$@"
}

yellowEcho() {
    colorEcho 33 "$@"
}

runCmd() {
    colorEcho "36" "Run under work directory $PWD :$nl$@"
    "$@"
}

die() {
    redEcho "Error: $@" 1>&2
    exit 1
}

headInfo() {
    colorEcho "0;34;46" ================================================================================
    echo "$@"
    colorEcho "0;34;46" ================================================================================
    echo
}

#################################################################################
# auto adjust pwd to project root dir, and set PROJECT_ROOT_DIR var
#################################################################################
adjustPwdToProjectRootDir() {
    while true; do
        [ / = "$PWD" ] && die "fail to detect project directory!"

        [ -f pom.xml ] && {
            readonly PROJECT_ROOT_DIR="$PWD"
            yellowEcho "Find project root dir: $PWD"
            break
        }
        cd ..
    done
}

adjustPwdToProjectRootDir

#################################################################################
# project common info
#################################################################################

readonly version=`grep '<version>.*</version>' pom.xml | awk -F'</?version>' 'NR==1{print $2}'`
readonly aid=`grep '<artifactId>.*</artifactId>' pom.xml | awk -F'</?artifactId>' 'NR==1{print $2}'`

# set env variable TTL_DEBUG_ENABLE to enable java debug mode
readonly -a JAVA_CMD=(
    "$JAVA_HOME/bin/java" -Xmx128m -Xms128m -ea -Duser.language=en -Duser.country=US
    ${ENABLE_JAVA_RUN_DEBUG+
        -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005
    }
)
readonly -a MVN_CMD=(
    ./mvnw -V
)

#################################################################################
# maven operation functions
#################################################################################

mvnClean() {
    runCmd "${MVN_CMD[@]}" clean || die "fail to mvn clean!"
}

mvnBuildJar() {
    runCmd "${MVN_CMD[@]}" install -Pgen-src+doc -Pgen-git-properties -Dmaven.test.skip || die "fail to build jar!"
}

mvnCompileTest() {
    runCmd "${MVN_CMD[@]}" test-compile || die "fail to mvn test-compile!"
}

readonly dependencies_dir="target/dependency"

mvnCopyDependencies() {
    runCmd "${MVN_CMD[@]}" dependency:copy-dependencies -DincludeScope=test || die "fail to mvn copy-dependencies!"

    # remove repackaged and shaded javassist lib
    rm "$dependencies_dir"/javassist-* "$dependencies_dir"/jsr305-*
}

getClasspathOfDependencies() {
    [ -e "$dependencies_dir" ] || mvnCopyDependencies 1>&2

    echo "$dependencies_dir"/*.jar | tr ' ' :
}

getClasspathWithoutTtlJar() {
    [ ! -e "target/test-classes/"  -o  "target/test-classes/" -ot src/  ] &&
        mvnCompileTest 1>&2

    echo "target/test-classes:$(getClasspathOfDependencies)"
}

getTtlJarPath() {
    local -r ttl_jar="target/$aid-$version.jar"

    [ ! -e "$ttl_jar"  -o  "$ttl_jar" -ot src/ ] &&
        mvnBuildJar 1>&2

    echo "$ttl_jar"
}

getClasspath() {
    echo "$(getTtlJarPath):$(getClasspathWithoutTtlJar)"
}

#################################################################################
# maven actions
#################################################################################

if [ "$1" != "skipClean" ]; then 
    mvnClean
fi
