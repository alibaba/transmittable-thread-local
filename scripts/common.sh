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
readonly nl=$'\n' # new line
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

blueEcho() {
    colorEcho 36 "$@"
}

runCmd() {
    blueEcho "Run under work directory $PWD :$nl$@"
    time "$@"
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

# set env variable ENABLE_JAVA_RUN_DEBUG to enable java debug mode
readonly -a JAVA_CMD=(
    "$JAVA_HOME/bin/java" -Xmx128m -Xms128m -server -ea -Duser.language=en -Duser.country=US
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
    rm -rf target || die "fail to mvn clean!"
}

readonly ttl_jar="target/$aid-$version.jar"

mvnBuildJar() {
    if [ ! -e "$ttl_jar"  -o  "$ttl_jar" -ot src/ ]; then
        if [ -n "${TTL_CI_TEST_MODE+YES}" ]; then
            # Build jar action should have used package instead of install
            # here use install intendedly to check release operations.
            #
            # De-activate a maven profile from command line
            # https://stackoverflow.com/questions/25201430
            runCmd "${MVN_CMD[@]}" install -DperformRelease -P '!gen-sign' || die "fail to build jar!"
        else
            runCmd "${MVN_CMD[@]}" package -Dmaven.test.skip=true || die "fail to build jar!"
        fi
    fi
}

mvnCompileTest() {
    if [ ! -e "target/test-classes/"  -o  "target/test-classes/" -ot src/  ]; then
        runCmd "${MVN_CMD[@]}" test-compile || die "fail to mvn test-compile!" || die "fail to compile test!"
    fi
}

readonly dependencies_dir="target/dependency"

mvnCopyDependencies() {
    if [ ! -e "$dependencies_dir" ]; then
        # https://maven.apache.org/plugins/maven-dependency-plugin/copy-dependencies-mojo.html
        # exclude repackaged and shaded javassist libs
        runCmd "${MVN_CMD[@]}" dependency:copy-dependencies -DincludeScope=test -DexcludeArtifactIds=javassist,jsr305,spotbugs-annotations || die "fail to mvn copy-dependencies!"
    fi
}

getClasspathOfDependencies() {
    mvnCopyDependencies 1>&2

    echo "$dependencies_dir"/*.jar | tr ' ' :
}

getClasspathWithoutTtlJar() {
    mvnCompileTest 1>&2

    echo "target/test-classes:$(getClasspathOfDependencies)"
}

getTtlJarPath() {
    mvnBuildJar 1>&2

    echo "$ttl_jar"
}

getClasspath() {
    echo "$(getTtlJarPath):$(getClasspathWithoutTtlJar)"
}

getJUnitTestCases() {
    (
        mvnCompileTest 1>&2

        cd target/test-classes &&
        find . -iname '*Test.class' | sed '
                s%^\./%%
                s/\.class$//
                s%/%.%g
            '
    )
}

#################################################################################
# maven actions
#################################################################################

if [ "$1" != "skipClean" ]; then 
    mvnClean
fi
