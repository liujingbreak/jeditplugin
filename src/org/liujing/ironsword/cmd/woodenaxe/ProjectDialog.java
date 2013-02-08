package org.liujing.ironsword.cmd.woodenaxe;

import java.util.*;
import java.util.logging.*;
import java.io.*;
import org.liujing.ironsword.cmd.*;
import org.liujing.ironsword.ctl.*;
import org.liujing.ironsword.bean.*;
import org.liujing.ironsword.dao.*;

public class ProjectDialog extends ConsoleAlert implements ConsoleEventHandler{
    private boolean isNew = true;
    
    protected ConsoleTextInput prjName;
    protected ConsoleTextInput prjDesc;
    
    private ConsoleDataPagination folderList;
    private FindDialog findDialog = new FindDialog();
    
    protected ConsoleChoiceList actions;
    protected ConsoleChoiceList updateActions;
    private ProjectController ctl;
    private ConsoleAlert resultAlert;
    private ProjectDAO model;
    private ConsolePrompt promptDel = new ConsolePrompt("Are you sure to delete project? y/n");
    private ProjectAddFolderDialog folderAddDialog;
    private ProjectRemoveFolderDlg folderRemoveDialog;
    private DirDialog dirDialog;
    private OKCancelDialog snapShotDialog;
    private ConsoleTextInput snapShotDesc;
    
    public ProjectDialog(ProjectController prj){
        ctl = prj;
        add(new ConsoleLabel("## Project ##"));
        prjName = new ConsoleTextInput("Project Name");
        prjDesc = new ConsoleTextInput("Project Description");
        add(prjName);
        add(prjDesc);
        folderList = new ConsoleDataPagination(new CmdLinePagingHandler(){
                public DaoPagination fetchPage(PagingRequest pr){
                    return ctl.listFolders(model.getId().intValue(), new DaoPagination<RootFolder>(pr) );
                }
        });
        add(folderList);
        folderList.addSelEvtHandler(this);
        actions = new ConsoleChoiceList();
        actions.addItem("<< Back");
        actions.addItem("save");
        actions.addSelEvtHandler(this);
        updateActions = new ConsoleChoiceList();
        updateActions.addItem("delete");
        updateActions.addItem("link folder ...");
        updateActions.addItem("unlink folder ...");
        updateActions.addItem("find ...");
        updateActions.addItem("take snapshot");
        updateActions.addSelEvtHandler(this);
        add(actions);
        add(updateActions);
        resultAlert = new ConsoleAlert();
        setNew(true);
        snapShotDialog = new OKCancelDialog("Take a snapshot on project");
        snapShotDesc = new ConsoleTextInput("Input description here");
        snapShotDialog.getContentPanel().add(snapShotDesc);
        snapShotDialog.addEventHandler(OKCancelDialog.OK_EVENT, this);
    }
    
    public void setModel(ProjectDAO bean){
        model = bean;
        setNew(false);
        prjName.setText(model.getName());
        prjDesc.setText(model.getDesc());
        folderList.reset();
    }
    
    public void setNew(boolean isNew){
        this.isNew = isNew;
        updateActions.setVisible(!isNew);
        folderList.setVisible(!isNew);
        if(isNew){
            prjName.setText("");
            prjDesc.setText("");
        }
    }
    
    public boolean isNew(){
        return isNew;
    }
    
    public void handleEvent(ConsoleEvent evt){
        if(evt.getSource() == actions){
            int idx = ((Number)evt.getData()).intValue();
            switch(idx){
            case 0:
                hide();
                break;
            case 1:
                save();
                break;
            default:
                break;
            }
        }else if(evt.getSource() == updateActions ){
            int idx = ((Number)evt.getData()).intValue();
            if(idx == 0){
                delete();
            }else if(idx == 1){
                if(folderAddDialog == null){
                    folderAddDialog = new ProjectAddFolderDialog(ctl);
                    folderAddDialog.addCloseHandler(this);
                }
                folderAddDialog.setModel(model);
                folderAddDialog.popup();
            }else if(idx == 2){
                if(folderRemoveDialog == null){
                    folderRemoveDialog = new ProjectRemoveFolderDlg(ctl);
                    folderRemoveDialog.addCloseHandler(this);
                }
                folderRemoveDialog.setModel(model);
                folderRemoveDialog.popup();
            }else if(idx == 3){
                findDialog.setController(ctl);
                findDialog.setModel(model);
                findDialog.popup();
            }else if(idx == 4){
                snapShotDialog.popup();
            }
        }else if(evt.getSource() == promptDel){
            if("y".equalsIgnoreCase(evt.getInput())){
                ctl.delete(model.getId().intValue());
                hide();
                fireEvent(ConsoleEvent.OK_TYPE, "");
            }
        }else if((folderAddDialog == evt.getSource() || folderRemoveDialog == evt.getSource())&&
            ConsoleAlert.CLOSE_EVENT == evt.getType())
        {
            folderList.reset();
        }else if(evt.getSource() == folderList){
            if(dirDialog == null){
                dirDialog = DirDialog.getSharedInstance();
                dirDialog.addEventHandler(ConsoleEvent.OK_TYPE, this);
            }
            dirDialog.setRootFolder((RootFolder)evt.getData());
            dirDialog.setPath("");
            dirDialog.popup();//todo
        }else if(evt.getSource() == dirDialog){
            if(evt.getType() == ConsoleEvent.OK_TYPE)
                folderList.reset();
        }else if(evt.getSource() == snapShotDialog){
            takeSnapShot();
        }
    }
    
    private SnapShotMonitor snapShotMonitor = new SnapShotMonitor();
    class SnapShotMonitor implements ControllerProgressMonitor{
        public void stateMessage(String msg){
            ConsolePrintContext.getInstance().println(msg);
        }
        public void state(int number){
            ConsolePrintContext.getInstance().println(number+"");
        }
    }
    
    private void takeSnapShot(){
        ctl.takeSnapShot(model.getId().intValue(), snapShotDesc.getText(), snapShotMonitor);
    }
    
    protected void save(){
        if(isNew){
            ctl.addProject(prjName.getText(), prjDesc.getText());
            
        }else{
            model.setName(prjName.getText());
            model.setDesc(prjDesc.getText());
            ctl.updateProject(model);
        }
        resultAlert.setText("Project saved");
        resultAlert.popup();
        resultAlert.addCloseHandler(new ConsoleEventHandler(){
                public void handleEvent(ConsoleEvent evt){
                    hide();
                }
        });
        fireEvent(ConsoleEvent.OK_TYPE, "");
    }
    
    protected void delete(){
        promptDel.popup();
        promptDel.addEventHandler(ConsoleEvent.INPUT_DONE, this);
    }

}
