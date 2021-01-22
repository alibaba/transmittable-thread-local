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
__source_guard_E2EB46EC_DEB8_4818_8D4E_F425BDF4A275=true

# shellcheck source=common.sh
source "$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")/common.sh"

#################################################################################
# auto adjust pwd to project root dir, and set PROJECT_ROOT_DIR var
#################################################################################
__adjustPwdToProjectRootDir() {
    while true; do
        [ / = "$PWD" ] && die "fail to detect project directory!"

        [ -f pom.xml ] && {
            readonly PROJECT_ROOT_DIR="$PWD"
            yellowEcho "Find project root dir and change dir to $PWD"
            break
        }
        cd ..
    done
}

__adjustPwdToProjectRootDir

#################################################################################
# project common info
#################################################################################

# NOTE: DO NOT declare _version/_aid var as readonly, their value is supplied by subshell.
_version=$(grep '<version>.*</version>' pom.xml | awk -F'</?version>' 'NR==1{print $2}')
_aid=$(grep '<artifactId>.*</artifactId>' pom.xml | awk -F'</?artifactId>' 'NR==1{print $2}')

# set env variable ENABLE_JAVA_RUN_DEBUG to enable java debug mode
readonly -a JAVA_CMD=(
    "$JAVA_HOME/bin/java" -Xmx128m -Xms128m -server -ea -Duser.language=en -Duser.country=US
    ${ENABLE_JAVA_RUN_DEBUG+
        -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005
    }

)

readonly -a MVN_CMD=(
    ./mvnw -V --no-transfer-progress
)

#################################################################################
# maven operation functions
#################################################################################

mvnClean() {
    yellowEcho "clean maven"
    rm -rf target || die "fail to mvn clean!"
}

readonly ttl_jar="target/$_aid-$_version.jar"

mvnBuildJar() {
    if [ ! -e "$ttl_jar" ] || [ "$ttl_jar" -ot src/ ]; then
        if [ -n "${TTL_CI_TEST_MODE+YES}" ]; then
            # Build jar action should have used package instead of install
            # here use install intentionally to check release operations.
            #
            # De-activate a maven profile from command line
            # https://stackoverflow.com/questions/25201430
            logAndRun "${MVN_CMD[@]}" install -DperformRelease -P '!gen-sign' || die "fail to build jar!"
        else
            logAndRun "${MVN_CMD[@]}" package -Dmaven.test.skip=true || die "fail to build jar!"
        fi
    fi
}

mvnCompileTest() {
    if [ ! -e "target/test-classes/" ] || [ "target/test-classes/" -ot src/ ]; then
        logAndRun "${MVN_CMD[@]}" test-compile || die "fail to mvn test-compile!" || die "fail to compile test!"
    fi
}

readonly dependencies_dir="target/dependency"

mvnCopyDependencies() {
    if [ ! -e "$dependencies_dir" ]; then
        # https://maven.apache.org/plugins/maven-dependency-plugin/copy-dependencies-mojo.html
        # exclude repackaged and shaded javassist libs
        logAndRun "${MVN_CMD[@]}" dependency:copy-dependencies -DincludeScope=test -DexcludeArtifactIds=javassist,jsr305,spotbugs-annotations || die "fail to mvn copy-dependencies!"
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

if [ "${1:-}" != "skipClean" ]; then
    logAndRun mvnClean
fi
