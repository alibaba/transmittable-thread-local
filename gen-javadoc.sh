#!/bin/bash

PROG=`basename $0`
cd $(dirname $(readlink -f $0))
BASE=`pwd`

. common.sh


# java local setting: http://www.oracle.com/technetwork/articles/javase/locale-140624.html#using
# locale: zh_CN en_US: more info: http://www.oracle.com/technetwork/java/javase/locales-137662.html

# mvn javadoc plugin doc: http://maven.apache.org/plugins/maven-javadoc-plugin/javadoc-mojo.html
# -DjavadocExecutable=/Library/Java/JavaVirtualMachines/jdk1.7.0_45.jdk/Contents/Home/bin/javadoc

# javadoc: http://docs.oracle.com/javase/6/docs/api
# -link http://docs.oracle.com/javase/6/docs/api
# -splitindex 

rm -rf target/apidocs &&
runCmd \
javadoc -J-Duser.language=en -J-Duser.country=US \
-locale en_US \
-encoding utf8 -charset utf8 \
-windowtitle "MTC $version" \
-protected -use -author -version \
-link http://docs.oracle.com/javase/6/docs/api \
-d target/apidocs \
-sourcepath src/main/java/ \
-subpackages com
