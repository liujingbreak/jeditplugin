@echo off
set targetdir=e:\liujing\mac-share
rem cd /D "%targetdir%\build"
set OPT="-ea"
set OPT=%OPT% "-agentlib:jdwp=transport=dt_shmem,address=jsanalyser,server=y,suspend=n"
set OPT=%OPT% "-Djava.util.logging.config.file=%targetdir%\logging.properties"
set OPT=%OPT% "-cp"
set OPT=%OPT% "%targetdir%\build\classes;..\lib\*"
java %OPT% org.liujing.tool.JSAnalyser %*