package org.liujing.ironsword.cmd.woodenaxe;

import java.util.*;
import java.util.logging.*;
import java.io.*;
import org.liujing.ironsword.cmd.*;
import org.liujing.ironsword.ctl.*;
import org.liujing.ironsword.bean.*;
import org.liujing.ironsword.dao.*;

public class ProjectMaintainance extends ConsoleAlert implements ConsoleEventHandler{
    private ConsoleDataPagination projectList;
    private ConsoleChoiceList projectOperations;
    private ProjectController ctl = new ProjectController();
    public final static int BACK_EVENT = 10;
    protected ProjectDialog prjDialog;
    private ConsoleAlert alert = new ConsoleAlert();
    
    public ProjectMaintainance(){
        add(new ConsoleLabel("## Projects Maintainance ##"));
        projectList = new ConsoleDataPagination(new CmdLinePagingHandler()
            {
                public DaoPagination fetchPage(PagingRequest pr){
                    return ctl.list(pr);
                }
            });
        
        add(projectList);
        projectList.addSelEvtHandler(this);
        
        projectOperations = new ConsoleChoiceList();
        add(projectOperations);
        projectOperations.addItem("<< Back");
        projectOperations.addItem("Create...");
        projectOperations.addSelEvtHandler(this);
        prjDialog = new ProjectDialog(ctl);
        prjDialog.addEventHandler(ConsoleEvent.OK_TYPE, this);
    }
    
    public void handleEvent(ConsoleEvent evt){
        if(evt.getSource() == prjDialog){
            if(evt.getType() == ConsoleEvent.OK_TYPE){
                projectList.reset();
            }
            return;
        }else if(evt.getSource() == projectList){
            prjDialog.setModel((ProjectDAO)evt.getData());
            prjDialog.popup();
        }else{
            int idx = ((Number)evt.getData()).intValue();
            if(idx == 0){
                hide();
                fireEvent(BACK_EVENT, "");
            }else if(idx == 1){
                prjDialog.setNew(true);
                prjDialog.popup();
            }
        }
    }
    
    protected boolean onInput(String input){
        return true;
    }
    
}
