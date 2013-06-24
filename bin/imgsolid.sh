OPT='-agentlib:jdwp=transport=dt_socket,address=localhost:5013,server=y,suspend=n'
java $OPT -cp ../lib/mylib.jar liujing.util.ImageConsolidation $*
