@echo off
set targetdir=e:\liujing\mac-share
set woodenaxe_home=%CD%\..
cd /D %targetdir%
rem cd /D "%targetdir%\build"
set OPT="-ea"
set OPT=%OPT% "-agentlib:jdwp=transport=dt_shmem,address=ironsword,server=y,suspend=n"
set OPT=%OPT% "-Djava.util.logging.config.file=%woodenaxe_home%\logging.properties"
set OPT=%OPT% "-Dtempdir=%targetdir%"
set OPT=%OPT% "-Dyui=E:\liujing\myproject\yui3.9.1"
set OPT=%OPT% "-Dwebres=%woodenaxe_home%\web"
set OPT=%OPT% "-cp"
set OPT=%OPT% "%targetdir%\build\classes;%woodenaxe_home%\lib\*"
set OPT=%OPT% "-Ddbpath=file://E:/liujing/mac-share/data/default_iron_db"
rem set OPT=%OPT% "-Ddbpath=tcp://localhost/default_iron_db"
java %OPT% org.liujing.ironsword.CommandLineTool %*
cd /D %woodenaxe_home%\bin
