package org.liujing.ironsword.cmd;

import java.util.*;
import java.io.*;
import java.util.logging.*;

public abstract class ConsoleWidget implements ConsoleEventHandler{
    private static Logger log = Logger.getLogger(ConsoleWidget.class.getName());
    private boolean visible = true;
    protected ConsoleContainerWidget parent;
    protected int indent = 0;
    private Map<Integer, Set<ConsoleEventHandler>> handlerMap = new HashMap();
    public ConsoleWidget(){
        
    }
    /** get indent
     @return indent
    */
    public int getIndent(){
        return indent;
    }

    /** set indent
     @param indent indent
    */
    public void setIndent(int indent){
        this.indent = indent;
    }

    
    protected abstract void printContent(PrintWriter p);
    
    protected abstract boolean onInput(String input);
    
    protected boolean handleInput(String input){
        return onInput(input);
    }
    
    protected void print(PrintWriter p){
        printContent(p);
    }
    
    public void handleEvent(ConsoleEvent evt){
    
    }
    
    public void addEventHandler(int eventType, ConsoleEventHandler h){
        Set<ConsoleEventHandler> handlers = handlerMap.get(eventType);
        if(handlers == null){
            handlers = new HashSet();
            handlerMap.put(eventType, handlers);
        }
        handlers.add(h);
    }
    
    public void addSelEvtHandler( ConsoleEventHandler h){
        addEventHandler(ConsoleEvent.SELECT_TYPE, h);
    }
    
    protected void fireEvent(int type, String input){
        ConsoleEvent evt = new ConsoleEvent(this, type, input);
        fireEvent(evt);
    }
    
    protected void fireEvent(ConsoleEvent evt){
        Set<ConsoleEventHandler> handlers = handlerMap.get(evt.getType());
        if(handlers == null)
            return;
        for(ConsoleEventHandler h: handlers){
            h.handleEvent(evt);
        }
    }
    
    /** get visible
     @return visible
    */
    public boolean isVisible(){
        return visible;
    }

    /** set visible
     @param visible visible
    */
    public void setVisible(boolean visible){
        boolean old = this.visible;
        this.visible = visible;
        //if(this.visible != old)
        //    reprint();
    }
    public static String indentString(int i, String src){
        if(i > 0)
            return _indentString(i, src);
        else
            return src;
    }
    
    private static String _indentString(int space, String src){
        try{
            StringWriter sw = new StringWriter();
            PrintWriter indentp = new PrintWriter(sw);
            for(int i = 0; i< space; i++)
                indentp.write(" ");
            indentp.close();
            String indent = sw.toString();
            
            sw = new StringWriter();
            PrintWriter p = new PrintWriter(sw);
            BufferedReader reader = new BufferedReader(new StringReader(src));
            int chr = reader.read();
            boolean lineStart = true;
            while(chr != -1){
                if(lineStart){
                    p.print(indent);
                    lineStart = false;
                }
                if(chr == '\n')
                    lineStart = true;
                p.print((char)chr);
                chr = reader.read();
            }
            
            p.close();
            return sw.toString();
        }catch(IOException e){
            log.log(Level.SEVERE, "", e);
            return src;
        }
    }

}
