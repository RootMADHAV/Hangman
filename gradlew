#!/usr/bin/env sh

#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls -ld "$PRG"
    link=$(expr "$PRG" : '.*-> \(.*\)$')
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=$(dirname "$PRG")"/$link"
    fi
done
SAVED="$(cd "$(dirname "$PRG")" && pwd)"
cd "$SAVED" || exit
APP_HOME=$(pwd)
cd - || exit

APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Use the maximum available, or set MAX_FD != maximum.
MAX_FD=maximum

warn () {
    echo "$*" >&2
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "$( uname )" in
  CYGWIN* )
    cygwin=true
    ;;
  Darwin* )
    darwin=true
    ;;
  MSYS* | MINGW* )
    msys=true
    ;;
  NONSTOP* )
    nonstop=true
    ;;
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD=java
    if ! command -v java >/dev/null 2>&1
    then
        die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
fi

# Increase the maximum file descriptors if we can.
if ! "$cygwin" && ! "$darwin" && ! "$nonstop" ; then
    case $MAX_FD in #(
      /*)
        MAX_FD_LIMIT=$(( $(getconf PAGESIZE) * $(getconf _PHYS_PAGES) / (1024 * 1024 * 1024) ))
        ;;
      *)
        MAX_FD_LIMIT=none
        ;;
    esac
    if [ -n "$MAX_FD_LIMIT" ] && [ "$MAX_FD" = maximum ] ; then
        MAX_FD="$MAX_FD_LIMIT"
    fi
    ulimit -n "$MAX_FD" 2>/dev/null || warn "Could not set maximum file descriptor limit: $MAX_FD"
fi

if $cygwin || $msys ; then
    APP_HOME=$( cygpath --path --mixed "$APP_HOME" )
    CLASSPATH=$( cygpath --path --mixed "$CLASSPATH" )

    JAVACMD=$( cygpath --mixed "$JAVACMD" )

    # Now convert the arguments - kludge to limit ourselves to /bin/sh
    for arg do
        if
            case $arg in #(
              -*)   false ;;               # don't mess with options #(
              /?*)  t=${arg#/} t=/${t%%/*} # looks like a POSIX filepath
                    [ -e "$t" ] ;;         # and we have 'test' which works
              *)    false ;;
            esac
        then
            arg=$( cygpath --path --mixed "$arg" )
        fi
        case $arg in #(
          *\ * | *\  * | *\\* ) arg="\"$arg\"" ;;
        esac
        AppArgs="$AppArgs '$arg'"
    done
    eval "set -- $AppArgs"
else
    AppArgs=()
    # add a BEGIN sql to declare and initialize the handle.
    for arg; do
        case $arg in #(
          -*)   false ;;               # don't mess with options #(
          /?*)  t=${arg#/} t=/${t%%/*} # looks like a POSIX filepath
                 [ -e "$t" ] ;;         # and we have 'test' which works
          *)    false ;;
        esac
        case $arg in #(
          *\ * | *\  * | *\\* ) arg="\"$arg\"" ;;
        esac
        AppArgs+=("$arg")
    done
fi

exec "$JAVACMD" $DEFAULT_JVM_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
