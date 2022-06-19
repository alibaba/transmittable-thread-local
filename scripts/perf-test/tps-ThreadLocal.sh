#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

cd ../..

readonly mainClass=com.alibaba.perf.tps.CreateThreadLocalInstanceTps

./mvnw package exec:exec -DskipTests -Dexec.mainClass="$mainClass"
