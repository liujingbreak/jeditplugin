package org.liujing.parser;

import java.util.*;
import java.util.logging.*;
import java.io.*;
import java.util.regex.*;
import javax.swing.tree.*;
import org.liujing.jedit.parser.*;
import org.liujing.ironsword.grammar.*;
import org.liujing.parser.*;
import org.antlr.runtime.*;

public class PythonGrammarRecoganizer{
    public static void main(String[] args)throws Exception{
        
        FileInputStream fileIn = new FileInputStream(args[0]);
        ANTLRInputStream in = new ANTLRInputStream(fileIn);
        PythonLexer lexer = new PythonLexer(in);
        PythonParser parser = new PythonParser(new RemovableTokenStream(lexer) );
        
        
        if(args.length > 1 && args[1].equalsIgnoreCase("lexer")){
        
            for(Token tk = lexer.nextToken(); tk== null || tk.getType() != Token.EOF; tk = lexer.nextToken()){
                if(tk != null)
                    System.out.println("line: "+ tk.getLine() + "  text:" + tk.getText());
                else
                    System.out.print("<null> ");
            }
        }else{
            AntlrGrammarHandler h = new AntlrGrammarHandler();
            parser.setHandler(h);
            parser.file_input();
            h.printTree();
        }
    }
    
    
}
