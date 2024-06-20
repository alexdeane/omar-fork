@echo off

REM convience bat file to build with
REM $Header: /cvsroot/ebxmlrr/omar/build.bat,v 1.5 2006/08/07 18:23:58 selswannes Exp $

set _CLASSPATH=%CLASSPATH%

if "%JAVA_HOME%" == "" goto nojavahome
set JAVACMD=%JAVA_HOME%\bin\java

:nojavahome


rem if %JAVACMD% == "" goto usage:

rem set JAVA_BINDIR=`dirname %JAVACMD%`

rem set JAVA_HOME=%JAVA_BINDIR%\..

goto classpath


:classpath
set cp=.\misc\lib\ant.jar;.\misc\lib\ant-launcher.jar;.\misc\lib\ant-nodeps.jar;.\misc\lib\ant-junit.jar;.\misc\lib\junit.jar;.\misc\lib\xalan.jar;%JAVA_HOME%\lib\tools.jar

if "%CLASSPATH%" == "" goto noclasspath

set CLASSPATH=%CLASSPATH%;%cp%

goto next


:noclasspath

set _CLASSPATH=

set CLASSPATH=%cp%

goto next



:next

"%JAVACMD%" org.apache.tools.ant.Main %1 %2 %3 %4 %5 %6 %7 %8 %9

goto finish

usage:
echo "Cannot find JAVA. Please set your PATH."
exit 1

:finish
:clean

rem clean up classpath after

set CLASSPATH=%_CLASSPATH%

set _CLASSPATH=


