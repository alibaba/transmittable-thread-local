#!/bin/bash
set -eEuo pipefail
# adjust current dir to script dir
cd "$(dirname "$(readlink -f "$0")")"

# shellcheck disable=SC2154
[ $# -ne 1 ] && cu::die "need only 1 argument for version!$nl${nl}usage:$nl  $0 4.x.y"
readonly bump_version="$1"

cd ..

echo "bump TTL version of lib to $bump_version"
./mvnw versions:set \
  -DgenerateBackupPoms=false \
  -DprocessAllModules=true \
  -DnewVersion="$bump_version"
