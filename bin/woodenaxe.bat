@echo off
set targetdir=\liujing\mac-share
set woodenaxe_home=%CD%\..
cd /D %targetdir%
rem cd /D "%targetdir%\build"
set OPT="-ea"
set OPT=%OPT% "-agentlib:jdwp=transport=dt_shmem,address=ironsword,server=y,suspend=n"
set OPT=%OPT% "-Djava.util.logging.config.file=%woodenaxe_home%\logging.properties"
set OPT=%OPT% "-Dtempdir=%targetdir%"
set OPT=%OPT% "-Dyui=\liujing\mac-share\yui3.10"
set OPT=%OPT% "-Dwebres=%woodenaxe_home%\web"
set OPT=%OPT% "-cp"
set OPT=%OPT% "%targetdir%\build\classes;%woodenaxe_home%\lib\*"
set OPT=%OPT% "-Ddbpath=jdbc:h2:tcp://localhost/iron_db;AUTO_RECONNECT=TRUE;TRACE_LEVEL_FILE=2"
rem set OPT=%OPT% "-Ddbpath=tcp://localhost/default_iron_db"
java %OPT% org.liujing.ironsword.CommandLineTool %*
cd /D %woodenaxe_home%\bin
