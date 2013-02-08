package org.liujing.ironsword.cmd;

import java.util.*;
import java.io.*;
/**
Event: ConsoleEvent.SELECT_TYPE
*/
public class ConsoleChoiceList extends ConsoleWidget{
    String title;
    List<String> items = new ArrayList();
    boolean lineWrap = true;
    int startNo = -1;
    public ConsoleChoiceList(){
    }
    
    public ConsoleChoiceList(String title){
        this.title = title;
    }
    
    public void setLineWrap(boolean yes){
        lineWrap = yes;
    }
    
    public void addItem(String text){
        items.add(text);
        
    }
    
    public void setItem(int i, String text){
        items.set(i, text);
    }
    
    protected void printContent(PrintWriter p){
        if(title != null){
            p.print("\t");
            p.println(title);
        }
        int index = ConsolePrintContext.getInstance().nextChoiceRange(items.size());
        startNo = index;
        for(String item : items){
            p.print("[");
            p.print(index++);
            p.print("]\t");
            p.print(item);
            if(lineWrap)
                p.println();
        }
        
    }
    
    protected boolean onInput(String input){
        int i = -99;
        try{
            i = Integer.parseInt(input);
        }catch(NumberFormatException e){
            return true;
        }
        if(  i >= startNo && i < startNo+ items.size()){
            int index = i - startNo;
            fireEvent(new ConsoleEvent(this, ConsoleEvent.SELECT_TYPE, input, index));
            return false;
        }else{
            return true;
        }
    }
}
