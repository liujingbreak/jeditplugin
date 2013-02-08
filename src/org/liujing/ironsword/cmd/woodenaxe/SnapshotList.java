package org.liujing.ironsword.cmd.woodenaxe;

import java.util.*;
import java.util.logging.*;
import java.io.*;
import org.liujing.ironsword.cmd.*;
import org.liujing.ironsword.ctl.*;
import org.liujing.ironsword.bean.*;
import org.liujing.ironsword.dao.*;

public class SnapshotList extends ConsoleContainerWidget{
    ConsoleDataPagination snaps;
    ConsoleChoiceList actions;
    public SnapshotList(){
        snaps =new ConsoleDataPagination(new CmdLinePagingHandler(){
                public DaoPagination fetchPage (PagingRequest pr){
                    return ProjectController.listSnapShot(pr);
                }
        });
        add(snaps);
    }
    
    public void refresh(){
        snaps.reset();
    }
}
