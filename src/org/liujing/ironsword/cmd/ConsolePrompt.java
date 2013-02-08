package org.liujing.ironsword.cmd;

import java.util.*;
import java.io.*;

/**
    accept any text input
*/
public class ConsolePrompt extends ConsoleAlert{
    
    private String input;
	public ConsolePrompt(){
	    
	}
	
	
    public ConsolePrompt(String label){
        super(label);		
    }
	
    public String getInput(){
        return input;
    }

    public void setInput(String input){
        this.input = input;
    }

    @Override
    protected void print(PrintWriter p){
        int i = 0;
        for(ConsoleWidget child : children){
            if(child.isVisible()){
                if(i > 0)
                    p.println();
                if(child.indent > 0){
                    StringWriter sw = new StringWriter();
                    PrintWriter pc = new PrintWriter(sw);
                    child.print(pc);
                    pc.close();
                    p.print(indentString(child.indent, sw.toString()));
                }else{
                    child.print(p);
                }
                i++;
            }
        }
        p.print(": ");
    }
	
	protected boolean onInput(String input){
	    this.input = input;
	    hide();
        fireEvent(ConsoleEvent.INPUT_DONE, input);        
        return false;
    }
}
