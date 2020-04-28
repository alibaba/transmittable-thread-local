#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"
readonly BASE="$(pwd)"
. ./common_build.sh

update_version=false
deploy_maven=false
create_tag=false
deploy_java_doc=false

while getopts "djotv" arg; do
    case $arg in
        v)
            update_version=true
            ;;
        t)
            create_tag=true
            ;;
        d)
            deploy_maven=true
            ;;
        j)
            deploy_java_doc=true
            ;;
        ?) # UNKNOWN option!
            echo "UNKNOWN option: $arg!"
            exit 1
            ;;
    esac
done
shift $((OPTIND-1))



echo "Current version is: $version"
new_version="$(echo $version | sed 's/-SNAPSHOT//')"
echo "Use new version: $new_version"


$update_version && {
    echo '================================================================================'
    echo "Update version!"
    echo '================================================================================'

    # update pom version
    echo "update pom version..."
    find -name pom.xml | xargs sed "s/$version/$new_version/" -i
    # update version in docs
    echo "update badges link in docs..."
    find -name '*.md' -a -not -name 'release-action-list.md' | xargs sed "s/master/v$new_version/g" -i
    echo "update maven dependency version in docs..."
    sed "s#<version>.*</version>#<version>$new_version</version>#" -i *.md

    $create_tag && {
        git add -A
        git commit -m "release v$new_version"
        git tag -a "v$new_version" -m "release v$new_version" -f
        git push origin "v$new_version" -f
    }
}

$deploy_maven && {
    echo '================================================================================'
    echo 'deploy to maven center repo...'
    echo '================================================================================'
    ./mvnw deploy -DperformRelease=true
}


$deploy_java_doc && {
    ./mvnw install -Dmaven.test.skip=true -PsrcDoc

    git checkout gh-pages
    mv target/apidocs "apidocs/$new_version"
    sed "s#\".*/index.html\"#\"$new_version/index.html\"#" -i apidocs/index.html
    
    git add -A
    git commit -m "add javadoc for $new_version"
    git push
}
