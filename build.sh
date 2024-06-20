#!/bin/sh
#
# $Header: /cvsroot/ebxmlrr/omar/build.sh,v 1.6 2006/02/24 03:52:34 dougb62 Exp $

set -x

if [ -z "$JAVA_HOME" ]

then

JAVACMD=`which java`

if [ -z "$JAVACMD" ]

then

echo "Cannot find JAVA. Please set your PATH."

exit 1

fi

JAVA_BINDIR=`dirname $JAVACMD`

JAVA_HOME=$JAVA_BINDIR/..

fi



JAVACMD=$JAVA_HOME/bin/java


SEP=":"
cp="./misc/lib/dom.jar$SEP./misc/lib/ant.jar$SEP./misc/lib/ant-launcher.jar$SEP./misc/lib/ant-nodeps.jar$SEP./misc/lib/ant-junit.jar$SEP./misc/lib/junit.jar$SEP./misc/lib/xalan.jar$SEP$JAVA_HOME/lib/tools.jar"



$JAVACMD $ANT_OPTS -classpath $cp$SEP$CLASSPATH org.apache.tools.ant.Main "$@"

set +x
