package org.liujing.ironsword.cmd.woodenaxe;

import java.util.*;
import java.util.logging.*;
import java.io.*;
import org.liujing.ironsword.cmd.*;
import org.liujing.ironsword.ctl.*;
import org.liujing.ironsword.dao.RootFolder;
/**
    fill in all root directory's attribute
*/
public class DirUpdatePanel extends ConsoleAlert{
    protected ConsoleTextInput dirPath;
    protected ConsoleTextInput dirIncludes;
    protected ConsoleTextInput dirExcludes;
    protected ConsoleChoiceList actions = new ConsoleChoiceList();
    private int dirId = -1;
    private ConsoleWidget popupFrom = null;
    protected FileScanController control;
    protected RootFolder model = null;
    
    public DirUpdatePanel(FileScanController ctl){
        dirPath = new ConsoleTextInput("Path","");
        dirIncludes = new ConsoleTextInput("Include pattern","");
        dirExcludes = new ConsoleTextInput("Exclude pattern","");
        add(dirPath);
        add(dirIncludes);
        add(dirExcludes);
        
        actions.addItem("OK");
        actions.addItem("Cancel");
        actions.addSelEvtHandler(new ConsoleEventHandler(){
                public void handleEvent(ConsoleEvent evt){
                    int idx = ((Integer)evt.getData()).intValue();
                    if(idx == 0){
                        save();
                        hide();
                        fireEvent(ConsoleEvent.OK_TYPE, evt.getInput());
                        
                    }else{
                        hide();
                        fireEvent(ConsoleEvent.CANCEL_TYPE, evt.getInput());
                    }
                    
                }
        });
        add(actions);
        this.control = ctl;
    }
    
    protected void save(){
        control.updateRootFolder(model.getId().intValue(), dirPath.getText(),
            dirIncludes.getText(), dirExcludes.getText());
    }
    
    public void setModel(RootFolder rf){
        this.model = rf;
        dirPath.setText(rf.getPath());
        StringBuilder sb = new StringBuilder();
        List<String> includeList = rf.getIncludes();
        int i=0;
        for(String include : includeList){
            if(i>0)
                sb.append(";");
            sb.append(include);
            i++;
        }
        dirIncludes.setText(sb.toString());
        
    }
    
    
}
