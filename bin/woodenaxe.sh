cd ..
woodenaxe_home=`pwd`
builddir=$HOME/myproject/build/jeditplugin
cd $builddir
cp="$woodenaxe_home/lib/*:$builddir/classes"
JAVA_OPT="-ea -d32 -Xms10m -Xmx80m -XX:PermSize=50m "
JAVA_OPT="$JAVA_OPT -Dtempdir=/Users/liujing/myproject/build"
JAVA_OPT="$JAVA_OPT -Dyui=/Users/liujing/myproject/yui3.8.1"
JAVA_OPT="$JAVA_OPT -Dwebres=/Volumes/udisk8G/jeditplugin/web"
JAVA_OPT="$JAVA_OPT -Djava.util.logging.config.file=$woodenaxe_home/logging.properties"
#JAVA_OPT="$JAVA_OPT -Ddbpath=file:/Users/liujing/myproject/build/iron_db"
#JAVA_OPT="$JAVA_OPT -Ddbpath=file:/Volumes/ssd/mac_iron_db"
JAVA_OPT="$JAVA_OPT -Ddbpath=file:/Volumes/udisk8G/build/iron_db"
#JAVA_OPT="$JAVA_OPT -DresList=/Users/liujing/myproject/doc-list.txt"
java -agentlib:jdwp=transport=dt_socket,address=localhost:5010,server=y,suspend=n $JAVA_OPT -cp $cp org.liujing.ironsword.CommandLineTool

