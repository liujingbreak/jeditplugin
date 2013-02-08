package org.liujing.ironsword.cmd.woodenaxe;

import java.util.*;
import java.util.logging.*;
import java.io.*;
import org.liujing.ironsword.cmd.*;
import org.liujing.ironsword.dao.*;
import org.liujing.ironsword.ctl.*;
import org.liujing.ironsword.bean.*;
import org.liujing.ironsword.lang.*;

/**
    Events:
        CLOSE - panel is intented to be closed
*/
public class FileActionPanel extends ConsoleContainerWidget implements ConsoleEventHandler{
    protected FileScanController ctl;
    protected SrcFile file;
    private ConsoleLabel title;
    private ConsoleChoiceList actionMenu;
    private ConsoleLabel dirContent;
    protected JavaModelBuilder javaBuilder;
    public static int CLOSE_EVENT = 9;
    
    public FileActionPanel(FileScanController controller){
        ctl = controller;
        title = new ConsoleLabel();
        add(title);
        
        actionMenu = new ConsoleChoiceList();
        add(actionMenu);
        actionMenu.addItem("Open");
        actionMenu.addItem("Dir");
        actionMenu.addItem("Doc");
        actionMenu.addSelEvtHandler(this);
        
        dirContent = new ConsoleLabel();
        dirContent.setVisible(false);
        add(dirContent);
        
        add(new ConsoleLabel("(Press Enter to continue)\n"));
    }
    
    public void setFileId(int srcFileId){
        SrcFile sf = ctl.getSrcFile(srcFileId);
        this.file = sf;
        reinit();
    }
    
    private void reinit(){
        title.setText("# File "+ file.fullpath());
    }
    
    public void handleEvent(ConsoleEvent evt){
        if(evt.getSource() == actionMenu){
            int idx = ((Number)evt.getData()).intValue();
            if(idx == 0){
                //open file
                ctl.openRemoteJEdit(file);
            }else if(idx == 1){
                dir();
            }
        }
    }
    
    protected void dir(){
        if("java".equals(file.getSrcType())){
            if(javaBuilder == null)
                javaBuilder = new JavaModelBuilder();
            File f = new File(file.fullpath());
            if(!f.exists()){
                dirContent.setText("File does not exists!");
                return;
            }
            CommonLanguageModel lang = javaBuilder.build(f);
            dirContent.setText(lang.toString());
            dirContent.setVisible(true);
        }
    }
    
    protected boolean onInput(String input){
        if(input.trim().length() == 0){
            dirContent.setVisible(false);
            fireEvent(CLOSE_EVENT, input);
        }
        return true;
    }
    
}
