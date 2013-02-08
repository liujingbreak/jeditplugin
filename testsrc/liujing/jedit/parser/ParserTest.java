package liujing.jedit.parser;

import java.util.*;
import java.util.logging.*;
import java.io.*;
import org.junit.*;
import org.liujing.magdown.parser.*;
import org.liujing.parser.*;
import org.antlr.runtime.*;

public class ParserTest{
    private static Logger log = Logger.getLogger(ParserTest.class.getName());

    @Test
    public void test2()throws Exception{

        ANTLRInputStream in = new ANTLRInputStream(
            ParserTest.class.getResourceAsStream("parser-test.txt"));
        JavaLexer lexer = new JavaLexer(in);
        RemovableTokenStream tokens = new RemovableTokenStream(lexer);
        JavaParser p = new JavaParser(tokens);
        //JavaFileAnalysisTool handler = new JavaFileAnalysisTool();
        p.compilationUnit(null);


    }




    public static void main(String[] args) throws Exception {
        //System.out.println("start");
        //new ParserTest().test2();
    }
}
