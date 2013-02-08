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
public class DirCreatePanel extends DirUpdatePanel{
    public DirCreatePanel(FileScanController ctl){
        super(ctl);
    }
    
    @Override
    protected void save(){
        control.addRootFolder(dirPath.getText(),
            dirIncludes.getText(), dirExcludes.getText());
    }
    
}
