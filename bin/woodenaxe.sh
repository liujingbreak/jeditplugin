cd ..
woodenaxe_home=`pwd`
builddir=/Users/liujing/myproject/build/jeditplugin
cd $builddir
cp="$woodenaxe_home/lib/*:$builddir/classes"
JAVA_OPT="-ea -d32 -Xms10m -Xmx80m -XX:PermSize=50m "
JAVA_OPT="$JAVA_OPT -Dtempdir=/Volumes/liujing/mac-share/build"
JAVA_OPT="$JAVA_OPT -Dyui=/Users/liujing/myproject/yui/yui3.11"
JAVA_OPT="$JAVA_OPT -DangularJS=/Users/liujing/myproject/angularJS/angular-1.2.0rc1"
JAVA_OPT="$JAVA_OPT -Dwebres=$woodenaxe_home/web"
JAVA_OPT="$JAVA_OPT -Djava.util.logging.config.file=$woodenaxe_home/logging.properties"

JAVA_OPT="$JAVA_OPT -Ddbpath=jdbc:h2:/Users/liujing/myproject/build/mac_iron_db"
#JAVA_OPT="$JAVA_OPT -Ddbpath=jdbc:h2:tcp://vaio/iron_db;AUTO_RECONNECT=TRUE;TRACE_LEVEL_FILE=2"

#JAVA_OPT="$JAVA_OPT -DresList=/Users/liujing/myproject/doc-list.txt"
java -agentlib:jdwp=transport=dt_socket,address=localhost:5010,server=y,suspend=n $JAVA_OPT -cp $cp org.liujing.ironsword.CommandLineTool

