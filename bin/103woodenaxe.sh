cd ..
woodenaxe_home=`pwd`
#builddir=/Volumes/liujing/mac-share/build
builddir=$woodenaxe_home/../build/jeditplugin
cd $builddir
cp="$woodenaxe_home/lib/*:$builddir/classes"
JAVA_OPT="-ea -d32 -Xms10m -Xmx80m -XX:PermSize=50m "
JAVA_OPT="$JAVA_OPT -Dtempdir=/Users/liujing/myproject/build"
JAVA_OPT="$JAVA_OPT -Dyui=/Users/liujing/myproject/yui"
JAVA_OPT="$JAVA_OPT -Dwebres=/Users/liujing/myproject/jeditplugin/web"
JAVA_OPT="$JAVA_OPT -Djava.util.logging.config.file=$woodenaxe_home/logging.properties"
JAVA_OPT="$JAVA_OPT -Ddbpath=tcp://192.168.0.103/mac_iron_db"
java -agentlib:jdwp=transport=dt_socket,address=localhost:5007,server=y,suspend=n $JAVA_OPT -cp $cp org.liujing.ironsword.CommandLineTool

