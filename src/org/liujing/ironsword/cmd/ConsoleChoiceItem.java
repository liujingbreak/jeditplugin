package org.liujing.ironsword.cmd;

import java.util.*;
import java.io.*;

public class ConsoleChoiceItem extends ConsoleLabel{
    protected String inputKey = null;
    public ConsoleChoiceItem(String text){
        super(text);
    }
    
    protected void printContent(PrintWriter p){
        inputKey = String.valueOf(ConsolePrintContext.getInstance().nextChoiceNum());
        p.print("["); p.print(inputKey); p.print( "] "); p.print(getText());
    }
    
    public String getInputKey(){
        return inputKey;
    }
    
    protected boolean onInput(String input){
        if(input.equals(inputKey)){
            fireEvent(ConsoleEvent.SELECT_TYPE, input);
            return false;
        }
        return super.onInput(input);
    }
}
