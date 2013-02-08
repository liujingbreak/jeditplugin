package org.liujing.ironsword.cmd.woodenaxe;

import java.util.*;
import java.util.logging.*;
import java.io.*;
import org.liujing.ironsword.cmd.*;
import org.liujing.ironsword.ctl.*;
import org.liujing.ironsword.bean.*;
import org.liujing.ironsword.dao.*;

public class DirMaintainance extends ConsoleContainerWidget{
    static private Logger log = Logger.getLogger(DirMaintainance.class.getName());
    static FileScanController scanCtl = new FileScanController("Dialog01");
    private ConsoleDataPagination rootList;
    private boolean recursivelyOpt = false;
    private ConsoleChoiceItem viewOpt;
    private DirDialog dirDialog;
    private DirUpdatePanel addNewDir = new DirCreatePanel(scanCtl);
    
    private ConsoleAlert savedAlert = new ConsoleAlert("edit saved");
    
    public DirMaintainance(){
        rootList = new ConsoleDataPagination(new CmdLinePagingHandler(){
            public DaoPagination fetchPage(PagingRequest pr){
                return scanCtl.dirRootFolders(pr);
            }
        }
            );
        rootList.addEventHandler(ConsoleEvent.SELECT_TYPE,new ConsoleEventHandler()
          {
              public  void handleEvent(ConsoleEvent evt){
                RootFolder folder = (RootFolder)evt.getData();
                dirDialog.setRootFolder(folder);
                dirDialog.setPath("");
                dirDialog.popup();
              }
          });
        
        add(rootList);
        dirDialog = DirDialog.getSharedInstance();
        dirDialog.setRecursively(recursivelyOpt);
        
        ConsoleChoiceList maintActions = new ConsoleChoiceList();
        add(maintActions);
        maintActions.addItem("Add directory...");
        maintActions.addSelEvtHandler(new ConsoleEventHandler(){
        	public void handleEvent(ConsoleEvent evt){
        		int item = ((Integer)evt.getData()).intValue();
        		if(item == 0){
        		    addNewDir.popup();
        		}
           }
        		
        });
        ConsoleEventHandler refreshHandler = new ConsoleEventHandler(){
                public void handleEvent(ConsoleEvent evt){
                    savedAlert.popup();
                    refresh();
                }
        };
        addNewDir.addEventHandler(ConsoleEvent.OK_TYPE, refreshHandler);
        dirDialog.addEventHandler(ConsoleEvent.OK_TYPE, refreshHandler);
    }
    
    public void refresh(){
        rootList.reset();
    }
    
    
    protected boolean onInput(String input){
        return super.onInput(input);
    }
}
