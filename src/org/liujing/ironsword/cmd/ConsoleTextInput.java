package org.liujing.ironsword.cmd;

import java.util.*;
import java.io.*;

public class ConsoleTextInput extends ConsoleChoiceItem{
    private String label;
    private ConsolePrompt prompt;
    
    public ConsoleTextInput(String label){
        super("");
        setLabel(label);
        prompt = new ConsolePrompt("Input "+ label);
        prompt.addEventHandler(ConsoleEvent.INPUT_DONE, new InputDoneHandler());

    }
    
    public ConsoleTextInput(String label, String defaultText){
        super(defaultText);
        setLabel(label);
        prompt = new ConsolePrompt("Input "+ label);
        prompt.addEventHandler(ConsoleEvent.INPUT_DONE, new InputDoneHandler());
    }
    
    public void setLabel(String t){
        label = t;
    }
    
    public String getLabel(){
        return label;
    }
    
    protected void printContent(PrintWriter p){
        inputKey = String.valueOf(ConsolePrintContext.getInstance().nextChoiceNum());
        p.print("["); p.print(inputKey); p.print( "] ");
        if(getLabel() !=null){
            p.print(getLabel());
            p.print(": ");
        }
        if(getText() == null || getText().length() == 0)
            p.print("<empty>");
        else{
            p.print("\"");
            p.print(getText());
            p.print("\"");
        }
    }
    
    protected boolean onInput(String input){
        if(input.equals(inputKey)){
            prompt.popup();
            return false;
        }
        return super.onInput(input);
    }
    
    private class InputDoneHandler implements ConsoleEventHandler{
        public void handleEvent(ConsoleEvent evt){
            setText(evt.getInput());
        }
    }
    
}
