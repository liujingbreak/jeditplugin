package org.liujing.ironsword.cmd;

import org.liujing.ironsword.bean.*;
public interface CmdLinePagingHandler{
    
    public DaoPagination fetchPage(PagingRequest pr);
}
