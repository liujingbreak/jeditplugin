package org.liujing.ironsword.cmd;

import java.util.*;
import java.io.*;
import org.liujing.ironsword.*;

public class DialogCommandAction extends CommandLineTool.AdvancedCMDAction
{
    static boolean showing;
    
    public String name(){
        return "dialog";
    }
    
    public void action(Console con, String[] args)throws Exception{
        ConsolePrintContext.getInstance().exit = false;
        showing = true;
        String input = null;
        while(showing &&  !ConsolePrintContext.getInstance().exit){
            ConsolePrintContext.getInstance().startPrint();
            StringWriter sw = new StringWriter();
            PrintWriter p = new PrintWriter(sw);
            p.println("------------------------------------------------");
            ConsolePrintContext.getInstance().getMain().print(p);
            con.printf(sw.toString());
            //while( input == null)
              input = con.readLine();
            
            ConsolePrintContext.getInstance().getInputMain().handleInput(input);
        }
        
    }
    
    public static void exit(){
        showing = false;
    }
}
