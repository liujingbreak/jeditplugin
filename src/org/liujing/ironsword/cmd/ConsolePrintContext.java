package org.liujing.ironsword.cmd;

import java.util.*;
import java.io.*;

public class ConsolePrintContext{
    private static ConsolePrintContext INSTANCE = new ConsolePrintContext();
    
    public boolean exit = false;
    
    public ConsoleWidget printMain;
    public ConsoleWidget inputMain; 
    
    private ConsolePrintContext(){
        printMain = new StartDialog();
        inputMain = printMain;
    }
    
    public static ConsolePrintContext getInstance(){
        return INSTANCE;
    }
    /**
    print immediately
    */
    public void print(String str){
        System.out.print(str);
    }

    public void println(String str){
        System.out.println(str);
    }
    
    protected int choiceCount = 0;
    
    /** get main
     @return main
    */
    public ConsoleWidget getMain(){
        return printMain;
    }

    /** set main
     @param main main
    */
    public void setMain(ConsoleWidget main){
        if(main == null)
            throw new RuntimeException("Main Console is set to null");
        this.printMain = main;
        this.inputMain = main;
    }

    public ConsoleWidget getInputMain(){
        return inputMain;
    }

    public void setInputMain(ConsoleWidget inputMain){
        this.inputMain = inputMain;
    }



    public void startPrint(){
        choiceCount = 0;
    }
    
    public int nextChoiceNum(){
        return ++choiceCount;
    }
    
    public int currChoiceNum(){
        return choiceCount;
    }
    
    public int nextChoiceRange(int count){
        int start = choiceCount + 1;
        choiceCount += count;
        return start;
    }
    
    public void exit(){
        exit = true;
    }
    
    
}
