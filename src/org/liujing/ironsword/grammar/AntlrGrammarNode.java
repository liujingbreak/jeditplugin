package org.liujing.ironsword.grammar;

import java.util.*;
import java.util.logging.*;
import java.io.*;
import org.antlr.runtime.*;

public class AntlrGrammarNode extends GrammarNode{
    public AntlrGrammarNode(){
        
    }
    
    public AntlrGrammarNode(String type, String name){
        super(type, name);
    }
    
    public void setStart(Token tk){
        CommonToken ct = (CommonToken)tk;
        setStartLine(ct.getLine());
        setStartOffset(ct.getStartIndex());
    }
    
    public void setStop(Token tk){
        CommonToken ct = (CommonToken)tk;
        setEndLine(ct.getLine());
        setEndOffset(ct.getStopIndex()+ 1);
    }
}
