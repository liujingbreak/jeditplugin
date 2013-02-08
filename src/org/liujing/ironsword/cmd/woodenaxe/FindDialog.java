package org.liujing.ironsword.cmd.woodenaxe;

import java.util.*;
import java.util.logging.*;
import java.io.*;
import org.liujing.ironsword.cmd.*;
import org.liujing.ironsword.ctl.*;
import org.liujing.ironsword.bean.*;
import org.liujing.ironsword.dao.*;

public class FindDialog extends ConsoleAlert{
    protected ProjectController ctl;
    protected FileScanController scanCtl;
    private ConsoleChoiceList menu = new ConsoleChoiceList();
    private ConsoleContainerWidget fileFindPanel;
    private ConsoleTextInput fileNameInput;
    private ConsoleTextInput fileSuffixInput;
    private ConsoleChoiceItem findaction;
    private ConsoleChoiceItem goFind;
    private ConsoleFoundFileView findResult;
    private ConsoleLabel findMatchOptLabel;
    private DirDialog dirDialog = DirDialog.getSharedInstance();
    private FileActionPanel fileActionPanel;
    private ConsoleAlert alert;
    
    protected ProjectDAO projectBean;
    private int findMatchOpt = ProjectDAO.FIND_NAME_CONTAIN;
    private ProjectController.FindField findFieldOpt;
    private SrcFileFindHandler srcFileFinder = new SrcFileFindHandler();
    private FileTreeFindHandler fileTreeFinder = new FileTreeFindHandler();
    
    public FindDialog(){
        scanCtl = new FileScanController();
        setText("## Find ##");
        fileFindPanel = new ConsoleContainerWidget();
        fileFindPanel.setVisible(false);
        initFileFindPanel();
        
        add(menu);
        menu.addItem("<< Back");
        menu.addItem("File name...");
        menu.addItem("File name starts with...");
        menu.addItem("File name contains...");
        menu.addItem("directory name...");
        menu.addItem("directory name contains...");
        menu.addItem("path...");
        menu.addItem("path contains...");
        menu.addItem("Package...");
        menu.addSelEvtHandler(this);
        add(fileFindPanel);
        findResult = new ConsoleFoundFileView();
        add(findResult);
        findResult.setVisible(false);
        findResult.addSelEvtHandler(this);
        
        fileActionPanel = new FileActionPanel(scanCtl);
        add(fileActionPanel);
        fileActionPanel.setVisible(false);
        fileActionPanel.addEventHandler(fileActionPanel.CLOSE_EVENT, this);
        alert = new ConsoleAlert();
    }
    
    private void initFileFindPanel(){
        fileFindPanel.add(new ConsoleLabel("~~~~~~~~~~ Find condition ~~~~~~~~~~"));
        fileNameInput = new ConsoleTextInput("Search For");
        fileSuffixInput = new ConsoleTextInput("File Suffix", "*");
        fileFindPanel.add(fileNameInput);
        fileFindPanel.add(fileSuffixInput);
        findMatchOptLabel = new ConsoleLabel();
        fileFindPanel.add(findMatchOptLabel);
        goFind = new ConsoleChoiceItem("Find >>");
        
        fileFindPanel.add(goFind);
        fileFindPanel.add(new ConsoleLabel("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"));
        
        fileFindPanel.setVisible(false);
        goFind.addSelEvtHandler(this);
    }
    
    public void setController(ProjectController c){
        ctl = c;
    }
    
    public void setModel(ProjectDAO projectBean){
        this.projectBean = projectBean;
    }
    
    public void handleEvent(ConsoleEvent evt){
        
        if(evt.getSource() == menu){
            int idx = ((Number)evt.getData()).intValue();
            if(idx == 0){
                hide();
            }else if(idx == 1){
                showFindPanel();
                findMatchOpt = ProjectDAO.FIND_NAME_IS;
                findMatchOptLabel.setText(" (find file by name)");
                findResult.setDataSource(srcFileFinder);
                findResult.setIndentLevels(3);
            }else if(idx == 2){
                showFindPanel();
                findMatchOpt = ProjectDAO.FIND_NAME_START;
                findMatchOptLabel.setText(" (find file whose name starts with)");
                findResult.setDataSource(srcFileFinder);
                findResult.setIndentLevels(3);
            }else if(idx == 3){
                showFindPanel();
                findMatchOpt = ProjectDAO.FIND_NAME_CONTAIN;
                findMatchOptLabel.setText(" (find file whose name contains)");
                findResult.setDataSource(srcFileFinder);
                findResult.setIndentLevels(3);
            }else if(idx == 4){
                showFindPanel();
                findMatchOpt = ProjectDAO.FIND_NAME_IS;
                findFieldOpt = ProjectController.FindField.NAME;
                findMatchOptLabel.setText(" (find directory by name)");
                findResult.setIndentLevels(2);
                findResult.setDataSource(fileTreeFinder);
            }else if(idx == 5){
                showFindPanel();
                findMatchOpt = ProjectDAO.FIND_NAME_CONTAIN;
                findFieldOpt = ProjectController.FindField.NAME;
                findMatchOptLabel.setText(" (find directory whose name contains)");
                findResult.setDataSource(fileTreeFinder);
                findResult.setIndentLevels(2);
            }else if(idx == 6){
                showFindPanel();
                findMatchOpt = ProjectDAO.FIND_NAME_IS;
                findMatchOptLabel.setText(" (find path)");
                findFieldOpt = ProjectController.FindField.PATH;
                findResult.setDataSource(fileTreeFinder);
                findResult.setIndentLevels(2);
            }else if(idx == 7){
                showFindPanel();
                findMatchOpt = ProjectDAO.FIND_NAME_CONTAIN;
                findFieldOpt = ProjectController.FindField.PATH;
                findMatchOptLabel.setText(" (find partial path)");
                findResult.setDataSource(fileTreeFinder);
                findResult.setIndentLevels(2);
            }
        }else if(evt.getSource() == goFind){
            findResult.setVisible(true);
            findResult.reset();
        }else if(evt.getSource() == findResult){
            List<Object> cells = ((TableRowVO)evt.getData()).getCells();
            showDirDialog(cells);
        }else if(evt.getSource() == fileActionPanel){
            if(evt.getType() == fileActionPanel.CLOSE_EVENT){
                fileActionPanel.setVisible(false);
                findResult.setVisible(true);
            }
        }
    }
    
    private void showDirDialog(List<Object> cells){
      String type = (String)cells.get(0);
      Number id = (Number)cells.get(cells.size()-1);
      if(type.equals("d")){
          FileTree filetree = scanCtl.getFileTree_RootFolder(id.intValue());
          dirDialog.setRootFolder(filetree.getRootFolder());
          dirDialog.setPath(filetree.getPath());
          dirDialog.popup();
      }else if(type.equals("r")){
          RootFolder rf = scanCtl.getRootFolder(id.intValue());
          dirDialog.setRootFolder(rf);
          dirDialog.setPath("");
          dirDialog.popup();
      }else if(type.equals("f")){
          fileActionPanel.setVisible(true);
          fileActionPanel.setFileId(id.intValue());
          findResult.setVisible(false);
      }
    }
    
    private void showFindPanel(){
        fileFindPanel.setVisible(true);
        findResult.setVisible(false);
        fileNameInput.setText("");
    }
    
    protected boolean onInput(String input){
        
        return true;
    }
    
    private class SrcFileFindHandler implements CmdLinePagingHandler{
        public DaoPagination fetchPage(PagingRequest pr){
            //if(fileNameInput.getText().length() == 0)
            //    return;
            return ctl.findSrcFile(projectBean.getId().intValue(), fileNameInput.getText(),
                fileSuffixInput.getText(), findMatchOpt, pr);
        }
    }
    
    private class FileTreeFindHandler implements CmdLinePagingHandler{
        public DaoPagination fetchPage(PagingRequest pr){
            //if(fileNameInput.getText().length() == 0)
            //    return;
            return ctl.findFileTree(projectBean.getId().intValue(), fileNameInput.getText(),
                 findMatchOpt, findFieldOpt, pr);
        }
    }
}
