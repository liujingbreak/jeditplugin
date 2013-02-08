package org.liujing.ironsword.cmd;

import java.util.*;
import java.io.*;

public class ConsoleLabel extends ConsoleWidget{
    private String text;
    public ConsoleLabel(){
        text = "";
    }
    public ConsoleLabel(String text){
        this.text = text;
    }
    /** get text
     @return text
    */
    public String getText(){
        return text;
    }

    /** set text
     @param text text
    */
    public void setText(String text){
        this.text = text;
    }

    @Override
    protected void printContent(PrintWriter p){
        p.print(text);
    }
    
    protected boolean onInput(String input){
        return true;
    }
}
