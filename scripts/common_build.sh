#!/bin/bash
[ -z "${_source_mark_of_common_build:+dummy}" ] || return 0
_source_mark_of_common_build=true


# shellcheck source=common.sh
source "$(dirname "$(readlink -f "$BASH_SOURCE")")/common.sh"

#################################################################################
# auto adjust pwd to project root dir, and set PROJECT_ROOT_DIR var
#################################################################################
adjustPwdToProjectRootDir() {
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

adjustPwdToProjectRootDir

#################################################################################
# project common info
#################################################################################

readonly version=$(grep '<version>.*</version>' pom.xml | awk -F'</?version>' 'NR==1{print $2}')
readonly aid=$(grep '<artifactId>.*</artifactId>' pom.xml | awk -F'</?artifactId>' 'NR==1{print $2}')

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

if [ "${1:-}" != "skipClean" ]; then
    mvnClean
fi
