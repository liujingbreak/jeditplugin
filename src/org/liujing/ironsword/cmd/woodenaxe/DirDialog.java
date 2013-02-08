package org.liujing.ironsword.cmd.woodenaxe;

import java.util.*;
import java.util.logging.*;
import java.io.*;
import org.liujing.ironsword.cmd.*;
import org.liujing.ironsword.dao.*;
import org.liujing.ironsword.ctl.*;
import org.liujing.ironsword.bean.*;
/**
Events:
  ConsoleEvent.OK_TYPE on directory is changed/updated
*/
public class DirDialog extends ConsoleAlert{
    static private Logger log = Logger.getLogger(DirDialog.class.getName());
    private FileScanController scanCtl;
    private Number rootFolderId;
    private RootFolder rootFolder;
    private boolean recursively;
    private String path = "";
    public final static int BACK_EVENT = 10;
    ConsoleLabel titleLabel;
    ResultPageView list;
    ConsoleAlert resultAlert;
    ConsoleAlert deleteAlert = new ConsoleAlert("[not implemented] Are you sure to delete whole folder ?");
    private DirUpdatePanel updateDirPanel;
    private ConsoleChoiceItem recursiveOpt;
    private FileActionPanel fileActionP;
    
    private static DirDialog INSTANCE;
    
    /**
    notice: this method is not thread safe
    */
    public static DirDialog getSharedInstance(){
        if(INSTANCE == null)
            INSTANCE = new DirDialog();
        return INSTANCE;
    }
    
    public DirDialog(){
        scanCtl = new FileScanController();
        updateDirPanel = new DirUpdatePanel(scanCtl);
        updateDirPanel.addEventHandler(ConsoleEvent.OK_TYPE, new ConsoleEventHandler()
            {
                public void handleEvent(ConsoleEvent evt){
                    
                    hide();
                    //fireEvent(evt);
                    fireEvent(new ConsoleEvent(DirDialog.this, ConsoleEvent.OK_TYPE, evt.getInput()));
                }
            });
        
        titleLabel = new ConsoleLabel("");
        add(titleLabel);
        ConsoleChoiceItem rescan = new ConsoleChoiceItem("scan this path");
        add(rescan);
        resultAlert = new ConsoleAlert();
        rescan.addEventHandler(ConsoleEvent.SELECT_TYPE, new ConsoleEventHandler()
            {
                public void handleEvent(ConsoleEvent evt){
                    ScanResultVO resVo = scanCtl.rescan(rootFolder, path);
                    resultAlert.setText(resVo.toString());
                    resultAlert.popup();
                    list.reset();
                }
            });
        list = new ResultPageView(new CmdLinePagingHandler()
            {
                public DaoPagination fetchPage(PagingRequest pr){
                    if(recursively){
                        return scanCtl.dir(rootFolderId.intValue(), path, pr);
                    }else{
                        return scanCtl.expandFilesFoldersByPath(rootFolder,
                            path, pr);
                    }
                }
            });
        add(list);
        list.addSelEvtHandler(this);
        fileActionP = new FileActionPanel(scanCtl);
        add(fileActionP);
        fileActionP.setVisible(false);
        fileActionP.addEventHandler(FileActionPanel.CLOSE_EVENT, this);
        
        ConsoleChoiceItem uppon = new ConsoleChoiceItem("< Up level folder");
        add(uppon);
        uppon.addEventHandler(ConsoleEvent.SELECT_TYPE, new ConsoleEventHandler()
            {
                public void handleEvent(ConsoleEvent evt){
                    upToEnclosingFolder();
                }
            });
        
        ConsoleChoiceItem exit = new ConsoleChoiceItem("<< Back"); 
        exit.addSelEvtHandler(new ConsoleEventHandler(){
                public void handleEvent(ConsoleEvent evt){
                    hide();
        }});
        add(exit);
        
        recursiveOpt = new ConsoleChoiceItem("Recursively View");
        add(recursiveOpt);
        recursiveOpt.addSelEvtHandler(new ConsoleEventHandler(){
                public void handleEvent(ConsoleEvent evt){
                    setRecursively(!isRecursively());
        }});
        
        ConsoleChoiceItem updateFolder = new ConsoleChoiceItem("Update root folder");
        add(updateFolder);
        updateFolder.addSelEvtHandler(new ConsoleEventHandler(){
                public void handleEvent(ConsoleEvent evt){
                    updateDirPanel.setModel(rootFolder);
                    updateDirPanel.popup();
                }
        });
        ConsoleChoiceItem delFolder = new ConsoleChoiceItem("delete root folder");
        add(delFolder);
        delFolder.addSelEvtHandler(new ConsoleEventHandler(){
                public void handleEvent(ConsoleEvent evt){
                    deleteAlert.popup();
                }
        });
         
    }
    
    public void handleEvent(ConsoleEvent evt){
        if(evt.getSource() == fileActionP){
            if(evt.getType() == fileActionP.CLOSE_EVENT){
                list.setVisible(true);
                fileActionP.setVisible(false);
            }
        }else{
            if(evt.getData() instanceof FileTree){
                setPath(((FileTree)evt.getData()).getPath());
            }else if(evt.getData() instanceof SrcFile){
                SrcFile sf = (SrcFile)evt.getData();
                list.setVisible(false);
                fileActionP.setVisible(true);
                fileActionP.setFileId(sf.getId().intValue());
                
            }
        }
    }
    
    public void setRootFolder(RootFolder rootFolder){
      this.rootFolderId = rootFolder.getId();
      this.rootFolder = rootFolder;
      list.reset();
      titleLabel.setText("  Folder: "+ rootFolder.getPath() + "\n  Path: "+ path);
    }
    
    /** get recursively
     @return recursively
    */
    public boolean isRecursively(){
        return recursively;
    }

    /** set recursively
     @param recursively recursively
    */
    public void setRecursively(boolean recursively){
        this.recursively = recursively;
        list.reset();
        recursiveOpt.setText("Recursively View: " + (recursively?"On":"Off") );
    }

    /** get path
     @return path
    */
    public String getPath(){
        return path;
    }

    /** set path
     @param path path
    */
    public void setPath(String path){
        this.path = path;
        list.reset();
        list.setVisible(true);
        fileActionP.setVisible(false);
        titleLabel.setText("  Folder: "+ rootFolder.getPath() + "\n  Path: "+ path);
    }

    @Override
    protected boolean onInput(String input){
        return true;
    }
    
    class ResultPageView extends ConsoleDataPagination{
        SubFilesVO vo;
        public ResultPageView(CmdLinePagingHandler dataSource){
            super(dataSource);
        }
        
        public void reset(){
            vo = null;
            super.reset();
        }
        
        protected void printContent(PrintWriter p){
            if(vo == null){
                vo = (SubFilesVO)dataSource.fetchPage(pr);
            }
            startChoiceNo = -1;
            endChoiceNo = 0;
            int index = pr.getOffset() + 1;
            for(DaoPagination obj : vo.getData()){
                if(obj == null) continue;
                for(int i = 0, last = obj.getSize(); i< last; i++)
                {
                    int cn = ConsolePrintContext.getInstance().nextChoiceNum();
                    if(startChoiceNo < 0)
                        startChoiceNo = cn;
                    p.print("["); p.print(cn);
                    p.print("]\t"); 
                    p.print(index++);
                    p.print(".\t"); 
                    p.println(obj.getRow(i).toString());
                }
            }
            
            endChoiceNo = ConsolePrintContext.getInstance().currChoiceNum();
            
            if(vo.getTotal()>= 0){
                p.print("-- total ");
                p.print(vo.getTotal());
                p.println("--");
            }
            if(vo.hasMore())
                p.println("[Enter] Press Enter to continue...");
            
        }
        
        protected boolean onInput(String input){
            if(input.length() == 0 && vo.hasMore()){
                pr = vo.prepareNextPage();
                vo = (SubFilesVO)dataSource.fetchPage(pr);
                return false;
            }else{
                int num = -1;
                try{
                    num = Integer.parseInt(input);
                }catch(NumberFormatException ne){
                    return true;
                }
                if(  num >= startChoiceNo && num <= endChoiceNo){
                    int index = num - startChoiceNo;
                    Object row = null;
                    if(index < vo.getSubSrcFiles().getSize())
                        row = vo.getSubSrcFiles().getRow(index);
                    else{
                        row = vo.getSubFolders().getRow(index - vo.getSubSrcFiles().getSize());
                    }
                    fireEvent(new ConsoleEvent(this, ConsoleEvent.SELECT_TYPE, input, row));
                    return false;
                }
            }
            return true;
        }
    }
    
    private void upToEnclosingFolder(){
        if(getPath().length() == 0){
            hide();
            fireEvent(new ConsoleEvent(this, BACK_EVENT, null));
        }else{
            int slashPos = getPath().lastIndexOf("/");
            if(slashPos >= 0){
                setPath(getPath().substring(0, slashPos));
            }else{
                setPath("");
            }
        }
    }
}
