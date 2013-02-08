package org.liujing.ironsword.cmd;

import java.util.*;
import java.io.*;

/**
 Events: CLOSE_EVENT
 */
public class ConsoleAlert extends ConsoleContainerWidget{
    private boolean popuped;
    private ConsoleLabel alertLabel;
    private ConsoleWidget popupFrom = null;
    public static final int CLOSE_EVENT = 9;
    public ConsoleAlert(){
        alertLabel = new ConsoleLabel();
        add(alertLabel);
    }
    
    public ConsoleAlert(String msg){
        this();
        alertLabel.setText(msg);
    }
    
    //@Override
    //protected void print(PrintWriter p){
    //    printChildren(p);
    //    p.println();
    //    p.print("Press [Enter] to continue...");
    //}
    
    public void popup(ConsoleWidget returnTo){
        popupFrom = returnTo;
        ConsolePrintContext.getInstance().setMain(this);
    }
    
    public void popup(){
        popup(ConsolePrintContext.getInstance().getMain());
    }
    
    public void addCloseHandler(ConsoleEventHandler closeHandler){
        addEventHandler(CLOSE_EVENT, closeHandler);
    }
    
    public void fireCloseEvent(String input){
        fireEvent(CLOSE_EVENT, input);
    }
    
    public void setText(String message){
        alertLabel.setText(message);
    }
    
    protected boolean onInput(String input){
        hide(input);
        return false;
    }
    
    public boolean isPopuped(){
        return popuped;
    }
    
    protected void hide(){
        ConsolePrintContext.getInstance().setMain(popupFrom);
        fireEvent(CLOSE_EVENT, "");
    }
    
    protected void hide(String input){
        ConsolePrintContext.getInstance().setMain(popupFrom);
        fireEvent(CLOSE_EVENT, input);
    }
}
