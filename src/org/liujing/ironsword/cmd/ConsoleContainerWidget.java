package org.liujing.ironsword.cmd;

import java.util.*;
import java.io.*;
import java.util.logging.*;

public class ConsoleContainerWidget extends ConsoleWidget{
    private static Logger log = Logger.getLogger(ConsoleContainerWidget.class.getName());
    protected List<ConsoleWidget> children = new ArrayList(1);
    
    public void add(ConsoleWidget child){
        children.add(child);
        if(child.parent != null)
            child.parent.remove(child);
        child.parent = this;
    }
    
    public void remove(ConsoleWidget child){
        children.remove(child);
    }
    
    protected void printContent(PrintWriter p){
      p.print("");
    }
    
    protected boolean onInput(String input){
        return true;
    }
    
    protected boolean handleInput(String input){
        for(ConsoleWidget child : children){
            if(child.isVisible() && !child.handleInput(input)){
                return false;
            }
        }
        return onInput(input);
    }
    
    @Override
    protected void print(PrintWriter p){
        printContent(p);
        p.println();
        printChildren(p);
    }
    
    protected void printChildren(PrintWriter p){
        
        for(ConsoleWidget child : children){
            if(child.isVisible()){
                //p.println();
                if(child.indent > 0){
                    StringWriter sw = new StringWriter();
                    PrintWriter pc = new PrintWriter(sw);
                    child.print(pc);
                    pc.close();
                    p.print(indentString(child.indent, sw.toString()));
                }else{
                    child.print(p);
                }
                p.println();
            }
        }
    }
    
    
}
