package org.liujing.parser;

import java.util.*;
import java.util.logging.*;
import java.io.*;
import java.util.regex.*;
import javax.swing.tree.*;
import org.liujing.jedit.parser.*;
import org.liujing.parser.*;
import org.antlr.runtime.*;

public class AntlrGrammarRecoganizer{
    public static void main(String[] args)throws Exception{
        
        FileInputStream fileIn = new FileInputStream(args[0]);
        ANTLRInputStream in = new ANTLRInputStream(fileIn);
        test(in);
    }
    
    public static void test(ANTLRStringStream in)throws Exception{
        Antlr3Lexer lexer = new Antlr3Lexer(in);
        RemovableTokenStream tokens = new RemovableTokenStream(lexer);
        Antlr3Parser p = new Antlr3Parser(tokens);
        p.parseGrammar();
        
    }
    
    
}
