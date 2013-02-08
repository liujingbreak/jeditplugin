package org.liujing.parser.antlr;

import java.util.*;
import java.util.logging.*;
import java.io.*;

public class ANTLRGrammarInfo{
    protected List<Rule> parserRules = new ArrayList();
    protected List<Rule> lexerRules = new ArrayList();
    protected List<InfoNode> others = new ArrayList();
    
    public ANTLRGrammarInfo(){}
    
    public void addParserRule(String name, int line, int offset, int endOffset){
        parserRules.add(new Rule(name, line, offset, endOffset));
    }
    
    public void addLexerRule(String name, int line, int offset, int endOffset, boolean fragment){
        lexerRules.add(new Rule(name, line, offset, endOffset));
    }
    
    public void addOtherInfo(String name, int line, int offset, int endOffset){
        others.add(new InfoNode(name, line, offset, endOffset));
    }
    
    public static class InfoNode{
        public int line;
        public int offset;
        public int endOffset;
        public String name;
        
        public InfoNode(String name, int line, int offset, int endOffset){
            this.line = line;
            this.offset = offset;
            this.name = name;
            this.endOffset = endOffset;
        }
        
    }
    
    public static class Rule extends InfoNode{
        public Rule(String name, int line, int offset, int endOffset){
            super(name, line, offset, endOffset);
        }
    }
    
    
}
