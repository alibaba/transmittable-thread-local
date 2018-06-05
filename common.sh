#!/bin/bash

redEcho() {
    if [ -c /dev/stdout ] ; then
        # if stdout is console, turn on color output.
        echo -ne "\033[1;31m"
        echo -n "$@"
        echo -e "\033[0m"
    else
        echo "$@"
    fi
}

runCmd() {
    redEcho "$@"
    "$@"
}

cleanInstall() {
    [ "$1" = 'skip'  ] || {
        ./mvnw clean install -Dmaven.test.skip && ./mvnw test-compile
    }
}

copyDeps() {
    [ "$1" = 'skip'  ] || {
        ./mvnw dependency:copy-dependencies -DincludeScope=test
        # remove repackaged and shaded javassist lib
        rm target/dependency/javassist*
    }
}

version=`grep '<version>.*</version>' pom.xml | awk -F'</?version>' 'NR==1{print $2}'`
aid=`grep '<artifactId>.*</artifactId>' pom.xml | awk -F'</?artifactId>' 'NR==1{print $2}'`
