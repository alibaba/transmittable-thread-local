# https://docs.travis-ci.com/user/languages/java/#Using-Java-10-and-later
# https://github.com/sormuras/bach/blob/master/install-jdk.sh
mkdir -p "$HOME/.jdk"
install_jdk_bin="$HOME/.jdk/install-jdk.sh"
if [ ! -f "$install_jdk_bin" ]; then
    wget https://github.com/sormuras/bach/raw/master/install-jdk.sh -O "$install_jdk_bin"
    chmod +x "$install_jdk_bin"
fi

readonly nl=$'\n' # new line
readonly ec=$'\033' # escape char
readonly eend=$'\033[0m' # escape end

colorEcho() {
    local color=$1
    shift

    # if stdout is console, turn on color output.
    [ -t 1 ] && echo "$ec[1;${color}m$@$eend" || echo "$@"
}

blueEcho() {
    colorEcho 36 "$@"
}

runCmd() {
    blueEcho "Run under work directory $PWD :$nl$@"
    time "$@"
}

swith_jdk() {
    (($# != 2)) && {
        echo "Error: $@, only need version and license"
        exit 2
    }
    local version="$1"
    local license="$2"

    case "$license" in
    GPL)
        export JAVA_HOME=$HOME/.jdk/openjdk$version
        ;;
    BCL)
        export JAVA_HOME=$HOME/.jdk/oraclejdk$version
        ;;
    *)
        echo "Error: $@, wrong license!"
        exit 2
        ;;
    esac

    if [ ! -d "$JAVA_HOME" ]; then
        runCmd "$install_jdk_bin" --feature $version --license $license --target "$JAVA_HOME"
    fi
}

switch_to_open_jdk9() {
    swith_jdk 9 GPL
}

switch_to_oracle_jdk11() {
  swith_jdk 11 BCL
}

switch_to_open_jdk10() {
    swith_jdk 10 GPL
}

switch_to_open_jdk11() {
    swith_jdk 11 GPL
}

switch_to_open_jdk12() {
    swith_jdk 12 GPL
}

switch_to_open_jdk13() {
    swith_jdk 13 GPL
}
