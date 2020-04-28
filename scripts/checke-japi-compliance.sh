#!/bin/bash
# Java API Compliance Checker (JAPICC),
# a tool for checking backward binary and source-level compatibility of a Java library API.
#   https://github.com/lvc/japi-compliance-checker
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

export TTL_CI_TEST_MODE=true
source ./common_build.sh skipClean

ttl_jar_path="$(readlink -f "$(getTtlJarPath)")"
JCC="$(readlink -f "$(which japi-compliance-checker.pl)")"

work_dir="target/japi-compliance-checker"
mkdir -p $work_dir
cd $work_dir
rm -rf compat_reports

for base_version in 2.5.0 2.6.0 2.7.0 2.10.2; do
    url="https://repo1.maven.org/maven2/com/alibaba/transmittable-thread-local/$base_version/transmittable-thread-local-$base_version.jar"
    base_jar="transmittable-thread-local-$base_version.jar"
    if [ ! -f "$base_jar" ]; then
        runCmd wget --quiet "$url"
    fi

    "$JCC" -show-packages -check-annotations -skip-internal-packages '\.(javassist|utils?|internal)\.?' \
        "$base_jar" \
        "$ttl_jar_path" || true
done
