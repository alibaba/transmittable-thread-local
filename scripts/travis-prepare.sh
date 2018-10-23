# https://docs.travis-ci.com/user/languages/java/#Using-Java-10-and-later
# https://github.com/sormuras/bach/blob/master/install-jdk.sh
mkdir -p "$HOME/.jdk"
install_jdk_bin="$HOME/.jdk/install-jdk.sh"
if [ ! -f "$install_jdk_bin" ]; then
    wget https://github.com/sormuras/bach/raw/master/install-jdk.sh -O "$install_jdk_bin"
    chmod +x "$install_jdk_bin"
fi

switch_to_open_jdk9() {
    export JAVA_HOME=$HOME/.jdk/openjdk9
    if [ ! -d "$JAVA_HOME" ]; then
        "$install_jdk_bin" --feature 9 --license GPL --target "$JAVA_HOME"
    fi
}

switch_to_open_jdk10() {
    export JAVA_HOME=$HOME/.jdk/openjdk10
    if [ ! -d "$JAVA_HOME" ]; then
        "$install_jdk_bin" --feature 10 --license GPL --target "$JAVA_HOME"
    fi
}

switch_to_open_jdk11() {
    export JAVA_HOME=$HOME/.jdk/openjdk11
    if [ ! -d "$JAVA_HOME" ]; then
        "$install_jdk_bin" --feature 11 --license GPL --target "$JAVA_HOME"
    fi
}

switch_to_open_jdk12() {
    export JAVA_HOME=$HOME/.jdk/openjdk12
    if [ ! -d "$JAVA_HOME" ]; then
        "$install_jdk_bin" --feature 12 --license GPL --target "$JAVA_HOME"
    fi
}
