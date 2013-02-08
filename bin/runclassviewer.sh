OPT="-d32 -ea -agentlib:jdwp=transport=dt_socket,address=localhost:5009,server=y,suspend=n"
OPT="$OPT -Djava.util.logging.config.file=logging.properties"
cd ..
woodenaxe_home=`pwd`
builddir=$woodenaxe_home/../build/jeditplugin
cd $builddir
cp="$woodenaxe_home/lib/*:$builddir/classes"

java $OPT -cp $cp org.liujing.awttools.classview.ClassSearchFrame
