#!/bin/bash

uuid=`date +%s`_${RANDOM}_$$
tmpDir=/tmp/${uuid}
mkdir -p $tmpDir

jarjarFile=/tmp/jarjar-1.4.jar

cleanupWhenExit() {
    rm -rf $tmpDir &> /dev/null
}
trap "cleanupWhenExit" EXIT

[ ! -e $jarjarFile ] && {
    wget https://jarjar.googlecode.com/files/jarjar-1.4.jar -O $jarjarFile || {
        echo "Fail to download jarjar!"
        exit 1
    }
}

for file; do
    echo "repackaging $file ..."
    bname=$(basename $file) &&
    java -jar $jarjarFile process <(echo 'rule javassist.** com.alibaba.mtc.javassist.@1') $file $tmpDir/$bname &&
    mv $tmpDir/$bname $file || {
        echo "Fail to repackage $file!"
        exit 2
    }
done