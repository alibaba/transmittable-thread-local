#!/bin/bash

cd $(dirname $(readlink -f $0))
BASE=`pwd`

. ./common.sh

version=`grep '<version>.*</version>' pom.xml | awk -F'</?version>' 'NR==2{print $2}'`
aid=`grep '<artifactId>.*</artifactId>' pom.xml | awk -F'</?artifactId>' 'NR==2{print $2}'`
classpath=`echo target/dependency/*.jar | tr ' ' :`

runJava java \
    -Xbootclasspath/a:target/$aid-$version.jar:`ls target/dependency/javassist*` \
    -javaagent:target/$aid-$version.jar \
    -cp target/test-classes:$classpath \
    -ea \
    com.alibaba.mtc.threadpool.agent.AgentDemo
