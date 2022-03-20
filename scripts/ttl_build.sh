#!/bin/bash
[ -z "${__source_guard_0397A413_E12B_4B3D_B2A6_4E1EED3D5447:+dummy}" ] || return 0
__source_guard_0397A413_E12B_4B3D_B2A6_4E1EED3D5447="$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")"

# shellcheck source=bash-buddy/lib/common_utils.sh
source "$__source_guard_0397A413_E12B_4B3D_B2A6_4E1EED3D5447/bash-buddy/lib/common_utils.sh"
source "$__source_guard_0397A413_E12B_4B3D_B2A6_4E1EED3D5447/common_build.sh"

#################################################################################
# auto adjust pwd to project root dir, and set PROJECT_ROOT_DIR var
#################################################################################
__adjustPwdToProjectRootDir() {
    while true; do
        [ / = "$PWD" ] && cu::die "fail to detect project directory!"

        [ -f pom.xml ] && {
            readonly PROJECT_ROOT_DIR="$PWD"
            cu::yellow_echo "Find project root dir and change dir to $PWD"
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
_version=$(extractFirstElementValueFromPom version "$__source_guard_0397A413_E12B_4B3D_B2A6_4E1EED3D5447/../pom.xml")
_aid=$(extractFirstElementValueFromPom artifactId "$__source_guard_0397A413_E12B_4B3D_B2A6_4E1EED3D5447/../pom.xml")

# set env variable ENABLE_JAVA_RUN_DEBUG to enable java debug mode
readonly -a JAVA_CMD=(
    "$JAVA_HOME/bin/java" -Xmx128m -Xms128m -server -ea -Duser.language=en -Duser.country=US
    ${ENABLE_JAVA_RUN_DEBUG+
        -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005
    }
)

#################################################################################
# maven operation functions
#################################################################################

mvnClean() {
    cu::yellow_echo "clean maven"
    rm -rf target || cu::die "fail to mvn clean!"
}

readonly ttl_jar="target/$_aid-$_version.jar"

mvnBuildJar() {
    if [ ! -e "$ttl_jar" ] || [ "$ttl_jar" -ot src/ ]; then
        # Build jar action should have used package instead of install
        # here use install intentionally to check release operations.
        #
        # De-activate a maven profile from command line
        # https://stackoverflow.com/questions/25201430
        MVN_WITH_BASIC_OPTIONS install -DperformRelease -P '!gen-sign' || cu::die "fail to build jar!"
    fi
}

mvnCompileTest() {
    if [ ! -e "target/test-classes/" ] || [ "target/test-classes/" -ot src/ ]; then
        MVN_WITH_BASIC_OPTIONS test-compile || cu::die "fail to mvn test-compile!"
    fi
}

readonly dependencies_dir="target/dependency"

mvnCopyDependencies() {
    if [ ! -e "$dependencies_dir" ]; then
        # https://maven.apache.org/plugins/maven-dependency-plugin/copy-dependencies-mojo.html
        # exclude repackaged and shaded javassist libs
        MVN_WITH_BASIC_OPTIONS dependency:copy-dependencies -DincludeScope=test -DexcludeArtifactIds=javassist,jsr305,spotbugs-annotations || cu::die "fail to mvn copy-dependencies!"
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
    cu::log_then_run mvnClean
fi
