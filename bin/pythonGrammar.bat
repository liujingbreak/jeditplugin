rem put file name
java -ea -agentlib:jdwp=transport=dt_shmem,address=python,server=y,suspend=n -Djava.util.logging.config.file=logging.properties  -Dclassdir=build\classes -Dlibdir=lib -jar loader.jar org.liujing.parser.PythonGrammarRecoganizer %*