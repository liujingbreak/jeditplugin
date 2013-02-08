package org.liujing.ironsword.cmd.woodenaxe;

import java.util.*;
import java.util.logging.*;
import java.io.*;
import org.liujing.ironsword.cmd.*;
import org.liujing.ironsword.ctl.*;
import org.liujing.ironsword.bean.*;
import org.liujing.ironsword.dao.*;

/**
*/
public class ProjectAddFolderDialog extends ConsoleAlert implements ConsoleEventHandler,
CmdLinePagingHandler
{
    protected ProjectController ctl;
    protected ProjectDAO model;
    
    private ConsoleChoiceList actions = new ConsoleChoiceList();
    private ConsoleDataPagination unlinkedFolderList;

    
    public ProjectAddFolderDialog(ProjectController ctl){
        this.ctl =ctl;
        add(new ConsoleLabel(labelText())); 
        unlinkedFolderList = new ConsoleDataPagination(this);
        unlinkedFolderList.addSelEvtHandler(this);
        add(unlinkedFolderList);
        add(actions);
        actions.addItem("<< Done");
        actions.addSelEvtHandler(this);
    }
    
    public void setModel(ProjectDAO model){
        this.model = model;
        unlinkedFolderList.reset();
    }
    
    protected String labelText(){
        return " ## select to link a folder to this project ##";
    }
    
    @Override
    public DaoPagination fetchPage(PagingRequest pr){
        return ctl.listUnlinkedFolders(model.getId().intValue(), pr);
    }
    
    public void handleEvent(ConsoleEvent evt){
        if(actions == evt.getSource()){
            int idx = ((Number)evt.getData()).intValue();
            if(idx == 0){
                hide();
                fireCloseEvent(evt.getInput());
            }
        }else if(unlinkedFolderList == evt.getSource()){
            onSelectFolder((RootFolder)evt.getData());
            unlinkedFolderList.reset();
        }
    }
    
    protected void onSelectFolder(RootFolder rf){
        ctl.linkFolder(model.getId().intValue(), rf.getId().intValue());
    }

}
