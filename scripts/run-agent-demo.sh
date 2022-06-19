#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

cd ..

readonly mainClass=${1:-com.alibaba.demo.ttl.agent.AgentDemo}

./mvnw -Penable-ttl-agent-for-test \
  package exec:exec \
  -DskipTests \
  -Dexec.mainClass="$mainClass"
