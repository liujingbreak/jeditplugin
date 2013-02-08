package org.liujing.ironsword.cmd;

import java.util.*;
import java.io.*;
import org.liujing.ironsword.cmd.woodenaxe.*;

public class StartDialog extends ConsoleContainerWidget{
    ConsoleAlert alert = new ConsoleAlert();
    DirMaintainance dirm = new DirMaintainance();
    ProjectMaintainance prjm = new ProjectMaintainance();
    private ConsoleAlert snapshotDialog;
    private SnapshotList snapshotList;
    
    
    public StartDialog(){
        ConsoleChoiceList list = new ConsoleChoiceList("Action Menu");
        add(list);
        list.setIndent(1);
        list.addEventHandler(ConsoleEvent.SELECT_TYPE, this);
        list.addItem("Directory Maintainance");
        list.addItem("Project Maintainance");
        list.addItem("Snapshots");
        list.addItem("exit");
        dirm.setVisible(false);
        dirm.setIndent(3);
        add(dirm);
        initSnapshotDialog();
    }
    
    public void handleEvent(ConsoleEvent evt){
        if(evt.getType() == ConsoleEvent.SELECT_TYPE){
            int idx = ((Number)evt.getData()).intValue();
            alert.setText("You selected: " + idx);
            if(idx == 3){
                ConsolePrintContext.getInstance().exit();
            }else if(idx == 0){
                dirm.setVisible(true);
            }else if(idx == 1){
                prjm.popup();
            }else if(idx == 2){
                snapshotList.refresh();
                snapshotDialog.popup();
            }
        }
    }
    
    private void initSnapshotDialog(){
        snapshotDialog = new ConsoleAlert();
        snapshotList = new SnapshotList();
        snapshotDialog.add(snapshotList);
        
    }
    
    @Override
    protected void print(PrintWriter pw){
        
        super.print(pw);
        pw.println();
        pw.println();
        pw.print( "input the No. of selected item:");
        
    }
}
