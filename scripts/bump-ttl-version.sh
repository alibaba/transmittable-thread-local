#!/bin/bash
set -eEuo pipefail
# adjust current dir to script dir
cd "$(dirname "$(readlink -f "$0")")"

source bash-buddy/lib/trap_error_info.sh
source bash-buddy/lib/common_utils.sh

source common_build.sh

# shellcheck disable=SC2154
[ $# -ne 1 ] && cu::die "need only 1 argument for version!$nl${nl}usage:$nl  $0 4.x.y"
readonly bump_version="$1"

cu::head_line_echo "bump TTL version of lib to $bump_version"
cd ..

MVN_WITH_BASIC_OPTIONS -q \
  org.codehaus.mojo:versions-maven-plugin:2.8.1:set \
  -DgenerateBackupPoms=false \
  -DnewVersion="$bump_version"

scripts/gen-pom4ide.sh
