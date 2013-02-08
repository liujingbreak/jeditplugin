cd ..
woodenaxe_home=`pwd`
builddir=$woodenaxe_home/../build/jeditplugin
cd $builddir
classpath="$woodenaxe_home/lib/*:$builddir/classes"
opts="-d32 -Djava.util.logging.config.file=$woodenaxe_home/logging.properties"

java -cp $classpath $opts org.liujing.tool.JSAnalyser $*
