#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"/..

sed '
    s#<source>1\.6</source>#<source>1.8</source>#
    s#<target>1\.6</target>#<target>1.8</target>#
' pom.xml > pom4ide.xml

echo "diff between pom and pom4ide.xml:"
echo

diff pom.xml pom4ide.xml
