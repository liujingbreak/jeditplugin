package org.liujing.ironsword.grammar;

import java.util.*;
import java.util.logging.*;
import java.io.*;

public class GrammarNode{
    private static List<GrammarNode> EMPTY_LIST = new ArrayList();
    protected List<GrammarNode> children = EMPTY_LIST;
    protected GrammarNode parent;
    private String name;
    private String type;
    private int startLine;
    private int startOffset;
    private int endLine;
    private int endOffset;
    protected Set<String> childTypes;
    //protected LinkedHashMap<String, children> children;
    
    
    public GrammarNode(String type, String name){
        this.type = type;
        this.name = name;
    }
    
    public GrammarNode(){
    
    }
    
    public void addChild(GrammarNode chr){
        if(children == EMPTY_LIST){
            children = new ArrayList();
            childTypes = new HashSet();
        }
        children.add(chr);
        chr.parent = this;
        childTypes.add(chr.getType());
    }
    
    public GrammarNode add(GrammarNode chr){
        addChild(chr);
        return this;
    }
    
    public List<GrammarNode> getChildren(){
        return children;
    }
    /** get parent
     @return parent
    */
    public GrammarNode getParent(){
        return parent;
    }

    /** get type
     @return type
    */
    public String getType(){
        return type;
    }

    /** set type
     @param type type
    */
    public void setType(String type){
        this.type = type;
    }

    
    /** get name
     @return name
    */
    public String getName(){
        return name;
    }

    /** set name
     @param name name
    */
    public void setName(String name){
        this.name = name;
    }

    public boolean isType(String type){
        return type == null? getType() == null : type.equals(this.getType());
    }
    
    /** get startLine
     @return startLine
    */
    public int getStartLine(){
        return startLine;
    }

    /** set startLine
     @param startLine startLine
    */
    public void setStartLine(int startLine){
        this.startLine = startLine;
    }

    /** get startOffset
     @return startOffset
    */
    public int getStartOffset(){
        return startOffset;
    }

    /** set startOffset
     @param startOffset startOffset
    */
    public void setStartOffset(int startOffset){
        this.startOffset = startOffset;
    }

    /** get endLine
     @return endLine
    */
    public int getEndLine(){
        return endLine;
    }

    /** set endLine
     @param endLine endLine
    */
    public void setEndLine(int endLine){
        this.endLine = endLine;
    }

    /** get endOffset
     @return endOffset
    */
    public int getEndOffset(){
        return endOffset;
    }

    /** set endOffset
     @param endOffset endOffset
    */
    public void setEndOffset(int endOffset){
        this.endOffset = endOffset;
    }

    public void print(int indents, PrintStream p){
        print(indents, new PrintWriter(new OutputStreamWriter(p)));
    }
    
    public void print(int indents, PrintWriter p){
        for( int i = 0; i < indents; i++)
            p.print("    ");
        p.println(type + ": " + name + " \tL"+ getStartLine() + ",P" + getStartOffset() + " - L" + getEndLine()
            + ",P" + getEndOffset());
    }
    
    public String toString(){
        StringWriter sw = new StringWriter();
        PrintWriter p = new PrintWriter(sw);
        travelTree(0, p);
        return sw.toString();
    }
    
    public void travelTree(int indents, PrintWriter p){
        print(indents, p);
        if(children == null)
            return;
        indents++;
        for(GrammarNode chr : children){
            chr.travelTree(indents, p);
        }
    }
    public void travelTree(int indents, PrintStream p){
        print(indents, p);
        if(children == null)
            return;
        indents++;
        for(GrammarNode chr : children){
            chr.travelTree(indents, p);
        }
    }
}
