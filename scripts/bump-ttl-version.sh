#!/bin/bash
set -eEuo pipefail
# adjust current dir to script dir
cd "$(dirname "$(readlink -f "$0")")"

source common.sh
source common_build.sh

# shellcheck disable=SC2154
[ $# -ne 1 ] && die "need only 1 argument for version!$nl${nl}usage:$nl  $0 4.x.y"
readonly bump_version="$1"

(
    headInfo "bump TTL version of lib to $bump_version"
    cd ..

    MVN_WITH_BASIC_OPTIONS -q \
        org.codehaus.mojo:versions-maven-plugin:2.8.1:set \
        -DgenerateBackupPoms=false \
        -DnewVersion="$bump_version"

    scripts/gen-pom4ide.sh
)

(
    headInfo "bump TTL version of ttl-integrations/sample-ttl-agent-extension-transformlet to $bump_version"
    cd ../ttl-integrations/sample-ttl-agent-extension-transformlet

    logAndRun -s \
        sed -ri 's~(<ttl.version>)(.*)(</ttl.version>)~\1'"$bump_version"'\3~' pom.xml
)
