package org.liujing.ironsword.cmd.woodenaxe;

import java.util.*;
import java.util.logging.*;
import java.io.*;
import org.liujing.ironsword.cmd.*;
import org.liujing.ironsword.ctl.*;
import org.liujing.ironsword.bean.*;
import org.liujing.ironsword.dao.*;

public class ProjectRemoveFolderDlg extends ProjectAddFolderDialog{
    public ProjectRemoveFolderDlg(ProjectController ctl){
        super(ctl);
    }
    
    protected String labelText(){
        return " ## select to unlink a folder from this project ##";
    }
    
    @Override
    public DaoPagination fetchPage(PagingRequest pr){
        return ctl.listFolders(model.getId().intValue(), new DaoPagination<RootFolder>(pr) );
    }
    
    protected void onSelectFolder(RootFolder rf){
        ctl.unlinkFolder(model.getId().intValue(), rf.getId().intValue());
        
    }
}
