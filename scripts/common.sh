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

fatal() {
    redEcho "$@" 1>&2
    exit 1
}

headInfo() {
    colorEcho "0;34;46" ================================================================================
    echo "$@"
    colorEcho "0;34;46" ================================================================================
    echo
}

################################################################################
# auto adjust pwd to project dir, and set project to BASE var
################################################################################
while true; do
    [ -f pom.xml ] && {
        readonly BASE="$PWD"
        yellowEcho "Find project base dir: $PWD"
        break
    }
    [ / = "PWD" ] &&  fatal "fail to detect project directory!"

    cd ..
done

#################################################################################
# project common info
#################################################################################

readonly version=`grep '<version>.*</version>' pom.xml | awk -F'</?version>' 'NR==1{print $2}'`
readonly aid=`grep '<artifactId>.*</artifactId>' pom.xml | awk -F'</?artifactId>' 'NR==1{print $2}'`

readonly -a JAVA_CMD=( "$JAVA_HOME/bin/java" -Xmx128m -Xms128m -ea -Duser.language=en -Duser.country=US )
readonly -a JAVA_DEBUG_OPTS=( -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005 )

#################################################################################
# maven operation functions
#################################################################################

mvnClean() {
    runCmd ./mvnw clean || fatal "fail to mvn clean!"
}

mvnBuildJar() {
    runCmd ./mvnw install -Dmaven.test.skip || fatal "fail to build jar!"
}

mvnCompileTest() {
    runCmd ./mvnw test-compile || fatal "fail to mvn test-compile!"
}

readonly dependencies_dir="target/dependency"

mvnCopyDependencies() {
    runCmd ./mvnw dependency:copy-dependencies -DincludeScope=test || fatal "fail to mvn copy-dependencies!"

    # remove repackaged and shaded javassist lib
    rm $dependencies_dir/javassist-*
}

getClasspathOfDependencies() {
    [ -e "$dependencies_dir" ] || mvnCopyDependencies 1>&2

    echo $dependencies_dir/*.jar | tr ' ' :
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

runCmd ./mvnw --version

if [ "$1" != "skipClean" ]; then 
    mvnClean
fi
