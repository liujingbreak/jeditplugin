package org.liujing.ironsword.cmd;

import java.util.*;
import java.io.*;
import java.util.logging.*;
import org.liujing.ironsword.bean.*;

public class ConsoleDataPagination extends ConsoleContainerWidget{
    private int pageLimit = 25; // page limit
    protected CmdLinePagingHandler dataSource;
    protected DaoPagination page;
    protected PagingRequest pr = null;
    protected int startChoiceNo = -1;
    protected int endChoiceNo;
    
    public ConsoleDataPagination(){
        pr = new PagingRequest(0, pageLimit);
    }
    
    public ConsoleDataPagination(CmdLinePagingHandler dataSource){
        this();
        this.dataSource = dataSource;
        
    }
    
    public CmdLinePagingHandler getDataSource(){
        return dataSource;
    }

    public void setDataSource(CmdLinePagingHandler dataSource){
        this.dataSource = dataSource;
        reset();
    }

 
    /**
        clean up UI cache, refresh
    */
    public void reset(){
        page = null;
        pr.setOffset(0);
    }
    /** get pageLimit
     @return pageLimit
    */
    public int getPageLimit(){
        return pageLimit;
    }

    /** set pageLimit
     @param pageLimit pageLimit
    */
    public void setPageLimit(int pageLimit){
        this.pageLimit = pageLimit;
    }
    
    public Object getRowData(int idx){
        return page.getRow(idx);
    }


    protected void printContent(PrintWriter p){
        if(page == null && dataSource != null){
          page = dataSource.fetchPage(pr);
        }
        if(page == null){
            startChoiceNo = -1;
            endChoiceNo = -1;
            return;
        }
        for(int i = 0, last = page.getSize(); i< last; i++)
        {
            int cn = ConsolePrintContext.getInstance().nextChoiceNum();
            if(i == 0)
                startChoiceNo = cn;
            p.print("["); p.print(cn);
            p.print("]\t"); 
            p.print(i + pr.getOffset() + 1);
            p.print(". "); 
            p.println(page.getRow(i).toString());
        }
        if(page.getTotal()>= 0){
            p.print("-- total ");
            p.print(page.getTotal());
            p.println("--");
        }
        if(page.hasMore())
            p.println("[Enter] Press Enter to continue...");
        endChoiceNo = ConsolePrintContext.getInstance().currChoiceNum();
    }
    
    protected boolean onInput(String input){
        if(page == null)
            return true;
        if(input.length() == 0 && page.hasMore()){
            pr = page.prepareNextPage();
            page = dataSource.fetchPage(pr);
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
                fireEvent(new ConsoleEvent(this, ConsoleEvent.SELECT_TYPE, input, page.getRow(index)));
                return false;
            }
        }
        return true;
    }
}
